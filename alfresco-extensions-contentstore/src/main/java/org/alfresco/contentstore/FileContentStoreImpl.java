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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.contentstore.dao.NodeInfo;
import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.alfresco.contentstore.patch.PatchService;
import org.apache.commons.io.IOUtils;
import org.sglover.alfrescoextensions.common.MimeType;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.ChecksumService;
import org.sglover.checksum.PatchDocument;
import org.sglover.checksum.PatchDocumentImpl;
import org.sglover.entities.EntitiesService;

/**
 * 
 * @author sglover
 *
 */
public class FileContentStoreImpl extends AbstractContentStore implements FileContentStore
{
    private ContentDAO contentDAO;

    public FileContentStoreImpl(String contentRoot, ChecksumService checksumService, PatchService patchService,
            NodeUsageDAO nodeUsageDAO, EntitiesService entitiesService, ContentDAO contentDAO) throws IOException
    {
        super(contentRoot, checksumService, patchService, nodeUsageDAO, entitiesService, false);
        this.contentDAO = contentDAO;
    }

//    @SuppressWarnings("unused")
//    @Override
//    public Node applyPatch(Node node, PatchDocument patchDocument) throws IOException
//    {
//        NodeInfo nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), true);
//        String existingContentPath = nodeInfo.getContentPath();
//        File inFile = new File(existingContentPath);
//        if(!inFile.exists())
//        {
//            throw new IllegalArgumentException();
//        }
//
//        String newContentUrl = createNewFileStoreUrl();
//        String outContentPath = getFullPath(newContentUrl);
//        File outFile = new File(outContentPath);
//
//        Node newNode = node.newNodeVersion(node.getNodeVersion() + 1);
//        NodeInfo newNodeInfo = new NodeInfo(newNode, outContentPath, nodeInfo.getMimeType(),
//                nodeInfo.getEncoding(), nodeInfo.getSize());
//
//        int blockSize = patchDocument.getBlockSize();
//
//        try(FileInputStream fis = new FileInputStream(inFile);
//                FileChannel inChannel = fis.getChannel();
//                FileOutputStream fos = new FileOutputStream(outFile);
//                FileChannel outChannel = fos.getChannel())
//        {
//            ByteBuffer currentData = ByteBuffer.allocate(1024);
//            inChannel.read(currentData);
//
//            int matchIndex = 0;
//            List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
//
//            for(Patch patch : patchDocument.getPatches())
//            {
//                int lastMatchingBlockIndex = patch.getLastMatchIndex();
//
//                long pos = 0;
//
//                for(;matchIndex < matchedBlocks.size(); matchIndex++)
//                {
//                    int blockIndex = matchedBlocks.get(matchIndex);
//                    pos = blockIndex * blockSize;
//                    if(blockIndex > lastMatchingBlockIndex)
//                    {
//                        break;
//                    }
//    
//                    int chunkSize = -1;
//                    if((blockIndex * blockSize) > currentData.limit())
//                    {
//                        chunkSize = currentData.limit() % blockSize;
//                    }
//                    else
//                    {
//                        chunkSize = blockSize;
//                    }
//
//                    long bytesWritten = inChannel.transferTo(pos, chunkSize, outChannel);
//                    if(bytesWritten != chunkSize)
//                    {
//                        throw new RuntimeException("Wrote too few bytes");
//                    }
//                    pos += bytesWritten;
//                }
//
//                InputStream stream = patch.getStream();
//                ReadableByteChannel c = Channels.newChannel(stream);
//                outChannel.transferFrom(c, pos, patch.getSize());
//            }
//    
//            //we're done with all the patches, add the remaining blocks
//            for(;matchIndex < matchedBlocks.size(); matchIndex++)
//            {
//                int blockIndex = matchedBlocks.get(matchIndex);
//    
//                int chunkSize = -1;
//                if((blockIndex * blockSize) > currentData.limit())
//                {
//                    chunkSize = currentData.limit() % blockSize;
//                }
//                else
//                {
//                    chunkSize = blockSize;
//                }
//
//                long bytesWritten = inChannel.transferTo(blockIndex * blockSize, chunkSize, outChannel);
//            }
//
//            contentDAO.updateNode(newNodeInfo);
//
//            return newNode;
//        }
//    }

    @SuppressWarnings("unused")
    @Override
    public Node applyPatch(Node node, PatchDocument patchDocument) throws IOException
    {
        NodeInfo nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), true);
        String existingContentPath = nodeInfo.getContentPath();
        File inFile = new File(existingContentPath);
        if(!inFile.exists())
        {
            throw new IllegalArgumentException();
        }

        String newContentUrl = createNewFileStoreUrl();
        String outContentPath = getFullPath(newContentUrl);
        File outFile = new File(outContentPath);

        Node newNode = Node.build().nodeId(node.getNodeId()).nodeVersion(node.getNodeVersion() + 1);
        NodeInfo newNodeInfo = new NodeInfo(newNode, outContentPath, nodeInfo.getMimeType(),
                nodeInfo.getEncoding(), nodeInfo.getSize());

        int blockSize = patchDocument.getBlockSize();

        try(FileInputStream fis = new FileInputStream(inFile);
                FileChannel inChannel = fis.getChannel();
                FileOutputStream fos = new FileOutputStream(outFile);
                FileChannel outChannel = fos.getChannel())
        {
            applyPatch(inChannel, outChannel, patchDocument);

            contentDAO.updateNode(newNodeInfo);

            return newNode;
        }
    }

    @Override
    protected ContentReader getReaderImpl(Node node) throws IOException
    {
        FileContentReader reader = new FileContentReaderImpl(node);
        return reader;
    }

    @Override
    protected ContentReader getReaderImpl(Node node, MimeType mimeType) throws IOException
    {
        FileContentReader reader = new FileContentReaderImpl(node, mimeType);
        return reader;
    }

    @Override
    public ReadableByteChannel getChannel(Node node) throws IOException
    {
        FileContentReader reader = new FileContentReaderImpl(node);
        ReadableByteChannel channel = reader.getChannel();
        return channel;
    }

    private class FileContentReaderImpl extends AbstractContentReader implements FileContentReader
    {
        private NodeInfo nodeInfo;

        FileContentReaderImpl(Node node)
        {
            super(node);
            this.nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), true);
        }

        FileContentReaderImpl(Node node, MimeType mimeType)
        {
            super(node);
            this.nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), mimeType);
        }

        @SuppressWarnings("resource")
        @Override
        public ReadableByteChannel getChannel() throws IOException
        {
            String contentPath = nodeInfo.getContentPath();
            RandomAccessFile randomAccessFile = new RandomAccessFile(contentPath, "r");  // won't create it
            FileChannel channel = randomAccessFile.getChannel();
            return channel;
        }

        @Override
        public MimeType getMimeType()
        {
            return nodeInfo.getMimeType();
        }

        @Override
        public Long getSize()
        {
            String contentPath = nodeInfo.getContentPath();
            try(RandomAccessFile randomAccessFile = new RandomAccessFile(contentPath, "r"))
            {
                return randomAccessFile.length();
            }
            catch(IOException e)
            {
                return -1l;
            }
        }

        @Override
        public ContentStore getStore()
        {
            return FileContentStoreImpl.this;
        }

        @Override
        public InputStream getStream() throws IOException
        {
            String contentPath = nodeInfo.getContentPath();
            InputStream in = new FileInputStream(contentPath);
            return in;
        }

        @Override
        public Reader getReader() throws IOException
        {
            Reader charReader = null;

            if (nodeInfo.getEncoding() == null)
            {
                charReader = new InputStreamReader(getStream());
            }
            else
            {
                charReader = new InputStreamReader(getStream(), nodeInfo.getEncoding());
            }

            return charReader;
        }

        @Override
        public String getPath()
        {
            NodeInfo nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), true);
            String contentPath = nodeInfo.getContentPath();
            return contentPath;
        }
    }

    private class FileContentWriterImpl extends AbstractContentWriter implements FileContentWriter
    {
        private NodeInfo nodeInfo;

        FileContentWriterImpl(Node node)
        {
            super(node);
            this.nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), true);
        }

//        FileContentWriterImpl(Node node, MimeType mimeType)
//        {
//            super(node);
//            this.nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), mimeType);
//        }

        @Override
        public ContentStore getStore()
        {
            return FileContentStoreImpl.this;
        }

        @Override
        public void writeStream(InputStream in) throws IOException
        {
            OutputStream out = getOutputStream();
            IOUtils.copy(in, out);
        }

        @Override
        public OutputStream getOutputStream() throws IOException
        {
            String contentPath = nodeInfo.getContentPath();
            OutputStream out = new FileOutputStream(contentPath);
            return out;
        }

        @Override
        public Writer getWriter() throws IOException
        {
            Writer writer = new OutputStreamWriter(getOutputStream());
            return writer;
        }

        @Override
        public String getPath()
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

//    @Override
//    public boolean exists(String nodeId, long nodeVersion)
//    {
//        NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion, true);
//        return nodeInfo != null;
//    }

    @Override
    public boolean exists(Node node)
    {
        NodeInfo nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getNodeVersion(), node.getMimeType());
        return nodeInfo != null;
    }

    protected String getFullPath(String newContentUrl) throws IOException
    {
        File file = makeFile(newContentUrl);

        // create the directory, if it doesn't exist
        File dir = file.getParentFile();
        if (!dir.exists())
        {
            makeDirectory(dir);
        }

        // create a new, empty file
        boolean created = file.createNewFile();
        if (!created)
        {
            throw new IOException(
                    "When specifying a URL for new content, the URL may not be in use already. \n"
                            + "   store: " + this + "\n" + "   new URL: "
                            + newContentUrl);
        }

        // done
        return file.getAbsolutePath();
    }

    @Override
    public ContentWriter getWriterImpl(Node node) throws IOException
    {
        String contentPath = getFullPath(createNewFileStoreUrl());
        NodeInfo nodeInfo = NodeInfo
                .start(node)
                .setContentPath(contentPath);

        contentDAO.updateNode(nodeInfo);

        FileContentWriter writer = new FileContentWriterImpl(node);
        return writer;
    }

//    @Override
//    public ContentWriter getWriterImpl(Node node, MimeType mimeType) throws IOException
//    {
//        String contentPath = getFullPath(createNewFileStoreUrl());
//        NodeInfo nodeInfo = NodeInfo
//                .start(node)
//                .setContentPath(contentPath);
//
//        contentDAO.updateNode(nodeInfo);
//
//        FileContentWriter writer = new FileContentWriterImpl(node, mimeType);
//        return writer;
//    }

    /**
     * 
     * @return
     */
    @Override
    public FileContentReader getFileContentReader(Node node)
    {
        FileContentReader reader = new FileContentReaderImpl(node);
        return reader;
    }

    @Override
    public FileContentWriter getFileContentWriter(ContentReference reference)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getBlockAsInputStream(Node node, long rangeId, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected PatchDocument getPatchDocument(Node node)
    {
        PatchDocument patchDocument = new PatchDocumentImpl();
        return patchDocument;
    }

}
