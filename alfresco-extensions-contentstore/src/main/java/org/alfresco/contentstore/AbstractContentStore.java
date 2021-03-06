/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.contentstore.dao.NodeUsage;
import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.alfresco.contentstore.dao.NodeUsageType;
import org.alfresco.contentstore.dao.UserContext;
import org.alfresco.contentstore.patch.PatchService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.content.file.TempFileProvider;
import org.sglover.alfrescoextensions.common.GUID;
import org.sglover.alfrescoextensions.common.MimeType;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.ChecksumService;
import org.sglover.checksum.NodeChecksums;
import org.sglover.checksum.Patch;
import org.sglover.checksum.PatchDocument;
import org.sglover.entities.EntitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * @author sglover
 *
 */
// TODO orphaned content - link to ContentDAO?
public abstract class AbstractContentStore implements ContentStore
{
    protected static Log logger = LogFactory.getLog(AbstractContentStore.class);

    protected String contentRoot;

    @Autowired
    protected ChecksumService checksumService;

    @Autowired
    protected NodeUsageDAO nodeUsageDAO;

    @Autowired
    protected PatchService patchService;

    @Autowired
    protected EntitiesService entitiesService;

    protected ExecutorService executor;

    @Value("isAsync")
    protected String isAsync;

    protected boolean async = true;

    public AbstractContentStore()
    {
    }

    public AbstractContentStore(ChecksumService checksumService, PatchService patchService,
            NodeUsageDAO nodeUsageDAO, EntitiesService entitiesService, boolean async) throws IOException
    {
        this.checksumService = checksumService;
        this.patchService = patchService;
        this.nodeUsageDAO = nodeUsageDAO;
        this.async = async;
        this.entitiesService = entitiesService;
    }

    public void init()
    {
        if(isAsync != null && !isAsync.isEmpty())
        {
            this.async = Boolean.valueOf(isAsync);
        }

        this.contentRoot = TempFileProvider.getTempDir("RepoContentStore").getAbsolutePath();
        logger.info("ContentStore root directory " + contentRoot);

        this.executor = Executors.newFixedThreadPool(5);
    }

    private void extractChecksumsAsync(final Node node)
    {
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    extractChecksumsImpl(node);
                }
                catch(IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void extractChecksumsImpl(final Node node) throws IOException
    {
        ContentReader reader = getReader(node);

        try(InputStream in = reader.getStream())
        {
            checksumService.extractChecksums(node, in);
        }
    }

    protected void extractChecksums(final Node node) throws IOException
    {
        if(async)
        {
            extractChecksumsAsync(node);
        }
        else
        {
            extractChecksumsImpl(node);
        }
    }

    private void createPatchImpl(Node node) throws IOException
    {
        Node preceding = Node.build()
                .nodeId(node.getNodeId()).nodeVersion(node.getNodeVersion() - 1)
                .mimeType(node.getMimeType());
        if(exists(preceding))
        {
            // previous version
            NodeChecksums nodeChecksums = checksumService.getChecksums(preceding.getNodeId(),
                    preceding.getNodeVersion());
            if (nodeChecksums != null)
            {
                ReadableByteChannel inChannel = getChannel(node);
                PatchDocument patchDocument = getPatchDocument(node);
                patchService.getPatch(patchDocument, nodeChecksums, inChannel);
                patchDocument.commit();
            }
            else
            {
                throw new RuntimeException(
                        "No patches available, no checksums for node " + node.getNodeId()
                                + ", nodeVersion " + (node.getNodeVersion() - 1));
            }
        }
        else
        {
            logger.warn("No patches available, only a single version of the node " + node);
        }
    }

    private void createPatchAsync(final Node node) throws IOException
    {
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    createPatchImpl(node);
                }
                catch(IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    protected void createPatch(final Node node) throws IOException
    {
        if(node.getNodeVersion() > 1)
        {
            if(async)
            {
                createPatchAsync(node);
            }
            else
            {
                createPatchImpl(node);
            }
        }
    }

    protected int getBlockSize()
    {
        return checksumService.getBlockSize();
    }

    public String getRootPath()
    {
        return contentRoot;
    }

    protected File makeFile(String contentUrl)
    {
        // take just the part after the protocol

        // get the file
        File file = new File(contentRoot, contentUrl);

        // done
        return file;
    }

    /**
     * Synchronized and retrying directory creation. Repeated attempts will be
     * made to create the directory, subject to a limit on the number of
     * retries.
     * 
     * @param dir
     *            the directory to create
     * @throws IOException
     *             if an IO error occurs
     */
    protected static void makeDirectory(File dir) throws IOException
    {
        if (dir.exists())
        {
            // Beaten to it during synchronization
            return;
        }
        // 20 attempts with 20 ms wait each time
        for (int i = 0; i < 20; i++)
        {
            boolean created = dir.mkdirs();
            if (created)
            {
                // Successfully created
                return;
            }
            // Wait
            try
            {
                synchronized (AbstractContentStore.class)
                {
                    AbstractContentStore.class.wait(20L);
                }
            }
            catch (InterruptedException e)
            {
                // TODO
            }

            // Did it get created in the meantime
            if (dir.exists())
            {
                // Beaten to it while asleep
                return;
            }
        }
        // It still didn't succeed
        throw new IOException("Failed to create directory for file storage: "
                + dir);
    }

    /**
     * Creates a new content URL. This must be supported by all stores that are
     * compatible with Alfresco.
     * 
     * @return Returns a new and unique content URL
     */
    protected String createNewFileStoreUrl()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        // sb.append(FileContentStore.STORE_PROTOCOL)
        // .append(ContentStore.PROTOCOL_DELIMITER)
        sb.append(year).append('/').append(month).append('/').append(day)
                .append('/').append(hour).append('/').append(minute)
                .append('/').append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;
    }

    /**
     * Copy of the the Spring FileCopyUtils, but does not silently absorb
     * IOExceptions when the streams are closed. We require the stream write to
     * happen successfully.
     * <p/>
     */
    protected long copyStreams(InputStream in, OutputStream out)
            throws IOException
    {
        return IOUtils.copy(in, out);
    }

//    public File create() throws IOException
//    {
//        String contentUrl = createNewFileStoreUrl();
//        File file = createNewFile(contentUrl);
//        return file;
//    }

//    private void fastCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
//        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
//
//        while(src.read(buffer) != -1)
//        {
//            buffer.flip();
//            dest.write(buffer);
//            buffer.compact();
//        }
//
//        buffer.flip();
//
//        while(buffer.hasRemaining())
//        {
//            dest.write(buffer);
//        }
//    }

//    @Override
//    public ContentReference createReference(MimeType mimetype, String encoding)
//    {
//        StringBuilder path = new StringBuilder(20);
//        path.append(UUIDGenerator.getInstance().generateRandomBasedUUID().toString())
//            .append("_" + ContentReference.TOKEN_INDEX);
//        if (mimetype != null)
//        {
//            path.append(".")
//                .append(mimetype.getExt());
//        }
//        
//        return absoluteReference(new ContentReference(path.toString(), mimetype, encoding));
//    }

    //    public File write(Node node, ContentReader content, boolean syncChecksums)
//            throws IOException
//    {
//        MimeType mimeType = content.getMimeType();
//        Long size = content.getSize();
//        ReadableByteChannel inChannel = content.getChannel();
//        String contentUrl = createNewFileStoreUrl();
//        File file = createNewFile(contentUrl);
//        String contentPath = file.getAbsolutePath();
//        FileOutputStream out = new FileOutputStream(file);
//        FileChannel outChannel = out.getChannel();
//        try
//        {
//            logger.debug("ContentStore writing to " + contentPath);
//            fastCopy(inChannel, outChannel);
//        }
//        finally
//        {
//            if (inChannel != null)
//            {
//                inChannel.close();
//            }
//            if (out != null)
//            {
//                out.close();
//            }
//        }
//
//        NodeInfo nodeInfo = NodeInfo.start(node)
//                .setContentPath(contentPath)
//                .setMimeType(mimeType)
//                .setSize(size);
//        contentDAO.updateNode(nodeInfo);
//
//        if (syncChecksums)
//        {
//            checksumService.extractChecksums(node, contentPath);
//        }
//        else
//        {
//            checksumService.extractChecksumsAsync(node, contentPath);
//        }
//
//        return file;
//    }

//    public File write(Node node, ContentReader content) throws IOException
//    {
//        return write(node, content, true);
//    }

    public void remove(String contentPath)
    {
        File file = new File(contentPath);
        if (file.exists())
        {
            file.delete();
        }
        else
        {
            logger.warn("Content already removed: " + contentPath);
        }
    }

//    @SuppressWarnings("resource")
//    @Override
//    public FileChannel getChannel(Node node) throws IOException
//    {
//        
//        String contentPath = null;
//        File file = new File(contentPath);
//        FileInputStream fin = new FileInputStream(file);
//        FileChannel channel = fin.getChannel();
//        return channel;
//    }

    protected abstract ContentReader getReaderImpl(Node node) throws IOException;
    protected abstract ContentReader getReaderImpl(Node node, MimeType mimeType) throws IOException;

    @Override
    public ContentReader getReader(Node node) throws IOException
    {
        try
        {
            return getReaderImpl(node);
        }
        finally
        {
            String username = UserContext.getUser();
            NodeUsage nodeUsage = new NodeUsage(node.getNodeId(),
                    node.getNodeVersion(), System.currentTimeMillis(), username, NodeUsageType.READ);
            nodeUsageDAO.addUsage(nodeUsage);
        }
    }

//    @Override
//    public ContentReader getReader(Node node, MimeType mimeType) throws IOException
//    {
//        try
//        {
//            return getReaderImpl(node, mimeType);
//        }
//        finally
//        {
//            Principal principal = UserContext.getUser();
//            String username = principal.getName();
//            NodeUsage nodeUsage = new NodeUsage(node.getNodeId(),
//                    node.getNodeVersion(), System.currentTimeMillis(), username, NodeUsageType.READ);
//            nodeUsageDAO.addUsage(nodeUsage);
//        }
//    }

    protected abstract ContentWriter getWriterImpl(Node node) throws IOException;
//    protected abstract ContentWriter getWriterImpl(Node node, MimeType mimeType) throws IOException;

    @Override
    public ContentWriter getWriter(Node node) throws IOException
    {
        try
        {
            return getWriterImpl(node);
        }
        finally
        {
            String username = UserContext.getUser();
            NodeUsage nodeUsage = new NodeUsage(node.getNodeId(), node.getNodeVersion(), System.currentTimeMillis(),
                    username, NodeUsageType.WRITE);
            nodeUsageDAO.addUsage(nodeUsage);
        }
    }

//    @Override
//    public ContentReader getTextContent(String nodeId, String nodeVersion) throws AuthenticationException, IOException
//    {
//        ContentReader reader = null;
//
//        NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, "text/plain");
//        if(nodeInfo != null)
//        {
//            String contentPath = nodeInfo.getContentPath();
//            ContentReference ref = new ContentReference(nodeInfo.getNode(), contentPath, MimeType.TEXT);
//            reader = getContent(ref);
//        }
//
//        return reader;
//    }

//    @Override
//    public ContentReader getReader(ContentReference ref) throws IOException
//    {
//        return getContent(ref);
//    }

    private class InChannel
    {
        private ReadableByteChannel inChannel;
        private List<Integer> matchedBlocks;
        private int previousBlockIndex = 0;
        private boolean initialized = false;
        private int matchIndex;
        private int currentBlockIndex;
        private int blockSize;
        private ByteBuffer currentBlock;
        private int bytesRead;

        InChannel(ReadableByteChannel inChannel, List<Integer> matchedBlocks, int blockSize)
        {
            this.inChannel = inChannel;
            this.matchedBlocks = matchedBlocks;
            this.blockSize = blockSize;
            this.currentBlock = ByteBuffer.allocate(blockSize);
            this.currentBlockIndex = 1;
        }

        int nextBlock() throws IOException
        {
            if(matchIndex >= matchedBlocks.size())
            {
                return -1;
            }
            else
            {
                this.currentBlockIndex = matchedBlocks.get(matchIndex++);

                if(!initialized || previousBlockIndex != currentBlockIndex)
                {
                    int delta = currentBlockIndex - previousBlockIndex;
                    for(int i = 0; i < delta; i++)
                    {
                        currentBlock.clear();
                        bytesRead = inChannel.read(currentBlock);
                        currentBlock.flip();
                    }
    
                    previousBlockIndex = currentBlockIndex;
                    if(!initialized)
                    {
                        initialized = true;
                    }
                }
                else
                {
                    currentBlock.position(0);
                }

                return currentBlockIndex;
            }
        }

        ByteBuffer getCurrentBlock()
        {
            return currentBlock;
        }

        public int getCurrentBlockIndex()
        {
            return currentBlockIndex;
        }
    }

    protected int applyPatch(ReadableByteChannel inChannel, WritableByteChannel outChannel,
            PatchDocument patchDocument) throws IOException
    {
        InChannel c = new InChannel(inChannel, patchDocument.getMatchedBlocks(), patchDocument.getBlockSize());

        int totalWritten = 0;

        int blockIndex = -1;

//        int blockIndex = c.nextBlock();
//        if(blockIndex > -1)
//        {
            for(Patch patch : patchDocument.getPatches())
            {
                int lastMatchingBlockIndex = patch.getLastMatchIndex();
    
                blockIndex = c.nextBlock();
                while(blockIndex != -1 && blockIndex <= lastMatchingBlockIndex)
                {
                    int bytesWritten = outChannel.write(c.currentBlock);
                    totalWritten += bytesWritten;
                    if(bytesWritten != c.bytesRead)
                    {
                        throw new RuntimeException("Wrote too few bytes, " + c.blockSize + ", " + bytesWritten);
                    }
    
                    blockIndex = c.nextBlock();
                    if(blockIndex == -1)
                    {
                        break;
                    }
                }
    
                // apply patch
                int patchSize = patch.getSize();
                ReadableByteChannel patchChannel = Channels.newChannel(patch.getStream());
                ByteBuffer patchBB = ByteBuffer.allocate(patchSize);
                int bytesRead = patchChannel.read(patchBB);
                patchBB.flip();
                int bytesWritten = outChannel.write(patchBB);
                totalWritten += bytesWritten;
                if(bytesWritten != bytesRead)
                {
                    throw new RuntimeException("Wrote too few bytes, expected " + bytesRead + ", got " + bytesWritten);
                }
            }

            // we're done with all the patches, add the remaining blocks
            while(blockIndex != -1)
            {
                int bytesWritten = outChannel.write(c.currentBlock);
                totalWritten += bytesWritten;
                if(bytesWritten != c.bytesRead)
                {
                    throw new RuntimeException("Wrote too few bytes");
                }

                blockIndex = c.nextBlock();
            }
//        }

        return totalWritten;
    }

    protected abstract PatchDocument getPatchDocument(Node node);

    @Override
    public PatchDocument getPatch(Node node, InputStream in) throws IOException
    {
        NodeChecksums checksums = checksumService.getChecksums(node.getNodeId(), node.getNodeVersion());

        PatchDocument patchDocument = getPatchDocument(node);
        patchService.updatePatchDocument(patchDocument, checksums, in);

        return patchDocument;
    }

    @Override
    public PatchDocument getPatch(Node node) throws IOException
    {
        PatchDocument patchDocument = getPatchDocument(node);
        return patchDocument;
    }

    @Override
    public void writePatchAsProtocolBuffer(Node node, OutputStream out) throws IOException
    {
        PatchDocument patchDocument = getPatchDocument(node);
        patchService.writePatch(node, patchDocument, out);
    }

//    @SuppressWarnings("unused")
//    protected void applyPatch(ReadableByteChannel inChannel, WritableByteChannel outChannel,
//            PatchDocumentImpl patchDocument) throws IOException
//    {
//        int blockSize = patchDocument.getBlockSize();
//
//        ByteBuffer currentData = ByteBuffer.allocate(blockSize);
//
//        int matchIndex = 0;
//        List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
//
//        for(Patch patch : patchDocument.getPatches())
//        {
//            int lastMatchingBlockIndex = patch.getLastMatchIndex();
//
//            long pos = 0;
//
//            for(;matchIndex < matchedBlocks.size(); matchIndex++)
//            {
//                int blockIndex = matchedBlocks.get(matchIndex);
//                pos = blockIndex * blockSize;
//                if(blockIndex > lastMatchingBlockIndex)
//                {
//                    break;
//                }
//
//                int bytesRead = inChannel.read(currentData);
//                currentData.flip();
//                int bytesWritten = outChannel.write(currentData);
//                if(bytesWritten != bytesRead)
//                {
//                    throw new RuntimeException("Wrote too few bytes");
//                }
//                currentData.clear();
//
////                int chunkSize = -1;
////                if((blockIndex * blockSize) > currentData.limit())
////                {
////                    chunkSize = currentData.limit() % blockSize;
////                }
////                else
////                {
////                    chunkSize = blockSize;
////                }
////
////                ByteBuffer dst = ByteBuffer.allocate(chunkSize);
////                int bytesWritten = inChannel.read(dst);
////                dst.flip();
////                outChannel.write(dst);
////                if(bytesWritten != chunkSize)
////                {
////                    throw new RuntimeException("Wrote too few bytes");
////                }
//                pos += bytesWritten;
//            }
//
//            int patchSize = patch.getSize();
//            ReadableByteChannel patchChannel = Channels.newChannel(patch.getStream());
//            ByteBuffer patchBB = ByteBuffer.allocate(patchSize);
//            patchChannel.read(patchBB);
//            patchBB.flip();
//            outChannel.write(patchBB);
//        }
//
//        //we're done with all the patches, add the remaining blocks
//        for(;matchIndex < matchedBlocks.size(); matchIndex++)
//        {
//            int blockIndex = matchedBlocks.get(matchIndex);
//
//            int bytesRead = inChannel.read(currentData);
//            currentData.flip();
//            int bytesWritten = outChannel.write(currentData);
//            if(bytesWritten != bytesRead)
//            {
//                throw new RuntimeException("Wrote too few bytes");
//            }
//            currentData.clear();
//
////            int chunkSize = -1;
////            if((blockIndex * blockSize) > currentData.limit())
////            {
////                chunkSize = currentData.limit() % blockSize;
////            }
////            else
////            {
////                chunkSize = blockSize;
////            }
////
////            ByteBuffer dst = ByteBuffer.allocate(chunkSize);
////            int bytesWritten = inChannel.read(dst);
////            outChannel.write(dst);
//        }
//    }

    protected void extractEntities(final Node node) throws IOException
    {
        
        if(async)
        {
            extractEntitiesAsync(node);
        }
        else
        {
            extractEntitiesImpl(node);
        }
    }

    protected void extractEntitiesImpl(final Node node) throws IOException
    {
        ContentReader reader = getReader(node);

        try(ReadableByteChannel ch = reader.getChannel())
        {
            entitiesService.getEntities(node, ch);
        }
    }
    
    protected void extractEntitiesAsync(final Node node) throws IOException
    {
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    extractEntitiesImpl(node);
                }
                catch(IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
