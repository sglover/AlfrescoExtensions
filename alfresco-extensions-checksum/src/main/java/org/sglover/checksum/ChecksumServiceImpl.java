/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.alfrescoextensions.common.Hasher;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.dao.ChecksumDAO;

/**
 * 
 * @author sglover
 *
 *         Based on the work here https://github.com/claytongulick/bit-sync
 * 
 */
public class ChecksumServiceImpl implements ChecksumService
{
    private static Log logger = LogFactory.getLog(ChecksumServiceImpl.class);

    private ChecksumDAO checksumDAO;
    private ExecutorService executors = Executors.newFixedThreadPool(10);
    private int blockSize = 1024 * 10;
    private Hasher hasher;

    public ChecksumServiceImpl(ChecksumDAO checksumDAO, int blocksize, Hasher hasher) throws NoSuchAlgorithmException
    {
        this(checksumDAO);
        this.blockSize = blocksize;
        this.hasher = hasher;
    }

    public void setBlockSize(int blockSize)
    {
        this.blockSize = blockSize;
    }

    public ChecksumServiceImpl(ChecksumDAO checksumDAO)
    {
        this.checksumDAO = checksumDAO;
    }

    private ReadableByteChannel getChannel(InputStream in) throws IOException
    {
        ReadableByteChannel channel = Channels.newChannel(in);
        return channel;
    }

//    static class Hasher
//    {
//        private MessageDigest md5;
//
//        Hasher() throws NoSuchAlgorithmException
//        {
//            md5 = MessageDigest.getInstance("MD5");
//        }
//
//        private String getHash(ByteBuffer bytes, int start, int end, MessageDigest digest)
//                throws NoSuchAlgorithmException
//        {
//            int saveLimit = bytes.limit();
//            bytes.limit(end + 1);
//
//            bytes.mark();
//            bytes.position(start);
//
//            digest.reset();
//            digest.update(bytes);
//            byte[] array = digest.digest();
//            StringBuffer sb = new StringBuffer();
//            for (int i = 0; i < array.length; ++i)
//            {
//                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
//                        1, 3));
//            }
//
//            bytes.limit(saveLimit);
//            bytes.reset();
//
//            return sb.toString();
//        }
//
//        public String md5(ByteBuffer bytes, int start, int end) throws NoSuchAlgorithmException
//        {
//            return getHash(bytes, start, end, md5);
//        }
//    }

    @Override
    public NodeChecksums getChecksums(String nodeId, long nodeVersion)
    {
        NodeChecksums checksums = checksumDAO.getChecksums(nodeId, nodeVersion);
        return checksums;
    }

//    private interface Reader
//    {
//        int read(ByteBuffer bb) throws IOException;
//    }
//
//    private class InputStreamAsReader implements Reader
//    {
//        private InputStream in;
//
//        InputStreamAsReader(InputStream in)
//        {
//            this.in = in;
//        }
//
//        @Override
//        public int read(ByteBuffer bb) throws IOException
//        {
//            byte[] bytes = new byte[bb.remaining()];
//            int numRead = in.read(bytes);
//            bb.put(bytes);
//            return numRead;
//        }
//    }

//    private class ReadableByteChannelAsReader implements Reader
//    {
//        private ReadableByteChannel channel;
//
//        ReadableByteChannelAsReader(ReadableByteChannel channel)
//        {
//            this.channel = channel;
//        }
//
//        @Override
//        public int read(ByteBuffer bb) throws IOException
//        {
//            return channel.read(bb);
//        }
//    }

//    @Override
//    public void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, InputStream in) throws IOException
//    {
//        Reader reader = new InputStreamAsReader(in);
//        updatePatchDocument(patchDocument, checksums, reader);
//    }

//    @Override
//    public void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ReadableByteChannel channel) throws IOException
//    {
//        Reader reader = new ReadableByteChannelAsReader(channel);
//        updatePatchDocument(patchDocument, checksums, reader);
//    }
//
//    private void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, Reader reader) throws IOException
//    {
//        ByteBuffer data = ByteBuffer.allocate(blockSize * 20);
//
//        int blockSize = checksums.getBlockSize();
//
//        int i = 0;
//
//        Adler32 adlerInfo = new Adler32(hasher);
//        int lastMatchIndex = 1; // starts at 1
//        ByteBuffer currentPatch = ByteBuffer.allocate(5000000); // TODO
//
//        int x = 0;
//
//        for (;;)
//        {
//            if(x == 0 || i >= data.limit())
//            {
//                data.clear();
//                i = 0;
//                int numRead = reader.read(data);
//                if(numRead <= 0)
//                {
//                    break;
//                }
//                data.flip();
//                x += numRead;
//            }
//
//            int chunkSize = 0;
//            // determine the size of the next data chuck to evaluate. Default to
//            // blockSize, but clamp to end of data
//            if ((i + blockSize) > data.limit())
//            {
//                chunkSize = data.limit() - i;
//                adlerInfo.reset(); // need to reset this because the rolling
//                                  // checksum doesn't work correctly on a final
//                                  // non-aligned block
//            }
//            else
//            {
//                chunkSize = blockSize;
//            }
//
//            int end = i + chunkSize - 1;
//
//            int matchedBlockIndex = adlerInfo.checkMatch(lastMatchIndex, checksums, data, i, end);
//            if (matchedBlockIndex != -1)
//            {
//                try
//                {
//                    String y = hasher.md5(data, i, end);
//                    System.out.println("y = " + y + ", x = " + x + ", i = " + i + ", end = " + end);
//                }
//                catch (NoSuchAlgorithmException e)
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//                // if we have a match, do the following:
//                // 1) add the matched block index to our tracking buffer
//                // 2) check to see if there's a current patch. If so, add it to
//                // the patch document.
//                // 3) jump forward blockSize bytes and continue
//                patchDocument.addMatchedBlock(matchedBlockIndex);
//
//                if (currentPatch.position() > 0)
//                {
//                    // there are outstanding patches, add them to the list
//                    // create the patch and append it to the patches buffer
//                    currentPatch.flip();
//                    int size = currentPatch.limit();
//                    byte[] dst = new byte[size];
//                    currentPatch.get(dst, 0, size);
//                    Patch patch = new Patch(lastMatchIndex, size, dst);
//                    patchDocument.addPatch(patch);
//                    currentPatch.clear();
//                }
//
//                lastMatchIndex = matchedBlockIndex;
//
//                i += chunkSize;
//
//                adlerInfo.reset();
//            }
//            else
//            {
//                // while we don't have a block match, append bytes to the
//                // current patch
//                if(currentPatch.position() >= currentPatch.limit())
//                {
//                    System.out.println("count=" + (x + i));
//                    System.out.println("count1=" + currentPatch.position() + ", " + currentPatch.limit());
////                    System.out.println(matchedBlockIndexes);
////                    System.out.println(patches);
//                }
//                currentPatch.put(data.get(i));
//                i++;
//            }
//        } // end for each byte in the data
//
//        if (currentPatch.position() > 0)
//        {
//            currentPatch.flip();
//            int size = currentPatch.limit();
//            byte[] dst = new byte[size];
//            currentPatch.get(dst, 0, size);
//            Patch patch = new Patch(lastMatchIndex, size, dst);
//            patchDocument.addPatch(patch);
//        }
//    }
//
//    @Override
//    public void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ByteBuffer data)
//    {
//        int blockSize = checksums.getBlockSize();
//
//        patchDocument.setBlockSize(blockSize);
//
//        int i = 0;
//
//        Adler32 adlerInfo = new Adler32(hasher);
//        int lastMatchIndex = 0;
//        ByteBuffer currentPatch = ByteBuffer.allocate(600000); // TODO
//
//        int currentPatchSize = 0;
//
//        for (;;)
//        {
//            int chunkSize = 0;
//            // determine the size of the next data chuck to evaluate. Default to
//            // blockSize, but clamp to end of data
//            if ((i + blockSize) > data.limit())
//            {
//                chunkSize = data.limit() - i;
//                adlerInfo.reset(); // need to reset this because the rolling
//                                  // checksum doesn't work correctly on a final
//                                  // non-aligned block
//            }
//            else
//            {
//                chunkSize = blockSize;
//            }
//
//            int matchedBlock = adlerInfo.checkMatch(lastMatchIndex, checksums, data, i, i + chunkSize - 1);
//            if (matchedBlock != -1)
//            {
//                try
//                {
//                    String y = hasher.md5(data, i, i + chunkSize - 1);
//                    System.out.println("y = " + y);
//                }
//                catch (NoSuchAlgorithmException e)
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                // if we have a match, do the following:
//                // 1) add the matched block index to our tracking buffer
//                // 2) check to see if there's a current patch. If so, add it to
//                // the patch document.
//                // 3) jump forward blockSize bytes and continue
//                patchDocument.addMatchedBlock(matchedBlock);
//
//                if (currentPatchSize > 0)
//                {
//                    // there are outstanding patches, add them to the list
//                    // create the patch and append it to the patches buffer
//                    currentPatch.flip();
//                    int size = currentPatch.limit();
//                    byte[] dst = new byte[size];
//                    currentPatch.get(dst, 0, size);
//                    Patch patch = new Patch(lastMatchIndex, size, dst);
//                    patchDocument.addPatch(patch);
//                }
//
//                lastMatchIndex = matchedBlock;
//
//                i += chunkSize;
//
//                adlerInfo.reset();
//
//                continue;
//            }
//            else
//            {
//                // while we don't have a block match, append bytes to the
//                // current patch
//                logger.debug("limit = " + currentPatch.limit()
//                        + ", position = " + currentPatch.position());
//                currentPatch.put(data.get(i));
//                currentPatchSize++;
//            }
//            if (i >= data.limit() - 1)
//            {
//                break;
//            }
//            i++;
//        } // end for each byte in the data
//
//        if (currentPatchSize > 0)
//        {
//            currentPatch.flip();
//            int size = currentPatch.limit();
//            byte[] dst = new byte[size];
//            currentPatch.get(dst, 0, size);
//            Patch patch = new Patch(lastMatchIndex, size, dst);
//            patchDocument.addPatch(patch);
//        }
//    }

    @Override
    public int getBlockSize()
    {
        return blockSize;
    }

    @Override
    public void saveChecksums(NodeChecksums checksums)
    {
        checksumDAO.saveChecksums(checksums);
    }

    @Override
    public void extractChecksumsAsync(final Node node, final InputStream in)
    {
        executors.submit(new Runnable()
        {
            @Override
            public void run()
            {
                extractChecksums(node, in);
            }
        });
    }

    @Override
    public NodeChecksums getChecksums(final Node node, final InputStream in)
    {
        final String nodeId = node.getNodeId();
        final Long nodeVersion = node.getNodeVersion();
        final Long nodeInternalId = node.getNodeInternalId();
        final String versionLabel = node.getVersionLabel();
        int x = 0;

        NodeChecksums documentChecksums = new NodeChecksums(nodeId, nodeInternalId,
                nodeVersion, versionLabel, blockSize);

        try(ReadableByteChannel fc = getChannel(in))
        {
            ByteBuffer data = ByteBuffer.allocate(blockSize*20);
            int bytesRead = -1;
            int blockNum = 1; // starts at 1

            do
            {
                bytesRead = fc.read(data);
                if(bytesRead > 0)
                {
                    x += bytesRead;

                    data.flip();

                    long numBlocks = data.limit() / blockSize + (data.limit() % blockSize > 0 ? 1 : 0);
    
                    // spin through the data and create checksums for each block
                    for (int i = 0; i < numBlocks; i++)
                    {
                        int start = i * blockSize;
                        int end = start + blockSize - 1;

                        if (end >= data.limit())
                        {
                            end = data.limit() - 1;
                        }

                        // calculate the adler32 checksum
                        Adler32 adlerInfo = new Adler32(data, start, end, hasher);

                        // calculate the full md5 checksum
                        String md5sum = hasher.md5(data, start, end);
                        Checksum checksum = new Checksum(blockNum, start, end, adlerInfo.getHash(),
                                adlerInfo.getAdler32(), md5sum);
                        if(blockNum < 2)
                        {
                            System.out.println(checksum);
                        }
                        documentChecksums.addChecksum(checksum);

                        blockNum++;
                    }

                    data.clear();
                }
            }
            while(bytesRead > 0);
        }
        catch (NoSuchAlgorithmException | IOException e)
        {
            throw new RuntimeException(e);
        }

        return documentChecksums;
    }

    @Override
    public NodeChecksums extractChecksums(final Node node, final InputStream in)
    {
        NodeChecksums documentChecksums = getChecksums(node, in);
        saveChecksums(documentChecksums);
        return documentChecksums;
    }
}
