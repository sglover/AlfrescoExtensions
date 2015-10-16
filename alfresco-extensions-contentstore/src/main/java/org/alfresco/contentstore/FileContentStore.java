/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.List;

import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.Patch;
import org.alfresco.checksum.PatchDocument;

/**
 * 
 * @author sglover
 *
 */
public class FileContentStore extends AbstractContentStore
{
    public FileContentStore(String contentRoot, ChecksumService checksumService) throws IOException
    {
        super(contentRoot, checksumService);
    }

    public FileContentStore(ChecksumService checksumService) throws IOException
    {
        super(checksumService);
    }

    public FileContentStore(File rootDirectory, ChecksumService checksumService)
    {
        super(rootDirectory, checksumService);
    }

    @SuppressWarnings("unused")
    @Override
    public String applyPatch(PatchDocument patchDocument, String existingContentPath) throws IOException
    {
        File inFile = new File(existingContentPath);
        if(!inFile.exists())
        {
            throw new IllegalArgumentException();
        }

        String newContentUrl = createNewFileStoreUrl();
        File outFile = createNewFile(newContentUrl);
        String outContentPath = outFile.getAbsolutePath();

        int blockSize = patchDocument.getBlockSize();//checksumService.getBlockSize();

        try(FileInputStream fis = new FileInputStream(inFile);
                FileChannel inChannel = fis.getChannel();
                FileOutputStream fos = new FileOutputStream(outFile);
                FileChannel outChannel = fos.getChannel())
        {
            ByteBuffer currentData = ByteBuffer.allocate(1024);
            inChannel.read(currentData);

            int matchIndex = 0;
            List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();

            for(Patch patch : patchDocument.getPatches())
            {
                int lastMatchingBlockIndex = patch.getLastMatchIndex();

                long pos = 0;

                for(;matchIndex < matchedBlocks.size(); matchIndex++)
                {
                    int blockIndex = matchedBlocks.get(matchIndex);
                    pos = blockIndex * blockSize;
                    if(blockIndex > lastMatchingBlockIndex)
                    {
                        break;
                    }
    
                    int chunkSize = -1;
                    if((blockIndex * blockSize) > currentData.limit())
                    {
                        chunkSize = currentData.limit() % blockSize;
                    }
                    else
                    {
                        chunkSize = blockSize;
                    }

                    long bytesWritten = inChannel.transferTo(pos, chunkSize, outChannel);
                    if(bytesWritten != chunkSize)
                    {
                        throw new RuntimeException("Wrote too few bytes");
                    }
                    pos += bytesWritten;
                }

                InputStream stream = patch.getStream();
                ReadableByteChannel c = Channels.newChannel(stream);
                outChannel.transferFrom(c, pos, patch.getSize());
            }
    
            //we're done with all the patches, add the remaining blocks
            for(;matchIndex < matchedBlocks.size(); matchIndex++)
            {
                int blockIndex = matchedBlocks.get(matchIndex);
    
                int chunkSize = -1;
                if((blockIndex * blockSize) > currentData.limit())
                {
                    chunkSize = currentData.limit() % blockSize;
                }
                else
                {
                    chunkSize = blockSize;
                }

                long bytesWritten = inChannel.transferTo(blockIndex * blockSize, chunkSize, outChannel);
            }
        }

        return outContentPath;
    }

    public SeekableByteChannel getContent(String contentPath) throws IOException
    {
        File file = new File(contentPath);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");  // won't create it
        FileChannel channel = randomAccessFile.getChannel();
        return channel;
    }

//    @Override
//    public InputStream getContent(String contentPath) throws IOException
//    {
//        File file = new File(contentPath);
//        InputStream in = new BufferedInputStream(new FileInputStream(file));
//        return in;
//    }
}
