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
import java.security.Principal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.Patch;
import org.alfresco.checksum.PatchDocument;
import org.alfresco.contentstore.dao.NodeUsage;
import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.alfresco.contentstore.dao.NodeUsageType;
import org.alfresco.contentstore.dao.UserContext;
import org.alfresco.extensions.common.GUID;
import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.Node;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
// TODO orphaned content - link to ContentDAO?
public abstract class AbstractContentStore implements ContentStore
{
    private static Log logger = LogFactory.getLog(AbstractContentStore.class);

    protected String rootPath;
    protected ChecksumService checksumService;
    protected NodeUsageDAO nodeUsageDAO;
    protected int blockSize;

    public AbstractContentStore(String contentRoot, ChecksumService checksumService,
            NodeUsageDAO nodeUsageDAO, int blockSize) throws IOException
    {
        this.rootPath = contentRoot;
        this.checksumService = checksumService;
        this.nodeUsageDAO = nodeUsageDAO;
        this.blockSize = blockSize;
    }

//    private ContentReference absoluteReference(ContentReference reference)
//    {
//        if (reference != null && reference.getPath() != null)
//        {
//            String path = reference.getPath();
//            if (!path.startsWith(File.separator))
//            {
//                return new ContentReference(reference.getNode(),
//                        absolutePath(path), reference.getMimetype(), reference.getEncoding(), reference.getIndex());
//            }
//        }
//        return reference;
//    }

//    private String absolutePath(String path)
//    {
//        if (!path.startsWith(File.separator))
//        {
//            StringBuilder sb = new StringBuilder(rootPath);
//            sb.append(File.separator);
//            sb.append(path);
//            return sb.toString();
//        }
//        return path;
//    }

//    private static File root(String contentRoot) throws IOException
//    {
//        File file = new File(contentRoot);
//        if (!file.exists())
//        {
//            makeDirectory(file);
//        }
//        return file;
//    }

    public void setBlockSize(int blockSize)
    {
        this.blockSize = blockSize;
    }

    public String getRootPath()
    {
        return rootPath;
    }

//    protected abstract String temporary() throws IOException;

    protected File makeFile(String contentUrl)
    {
        // take just the part after the protocol

        // get the file
        File file = new File(rootPath, contentUrl);

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
            Principal principal = UserContext.getUser();
            String username = principal.getName();
            NodeUsage nodeUsage = new NodeUsage(node.getNodeId(),
                    node.getNodeVersion(), System.currentTimeMillis(), username, NodeUsageType.READ);
            nodeUsageDAO.addUsage(nodeUsage);
        }
    }

    @Override
    public ContentReader getReader(Node node, MimeType mimeType) throws IOException
    {
        try
        {
            return getReaderImpl(node, mimeType);
        }
        finally
        {
            Principal principal = UserContext.getUser();
            String username = principal.getName();
            NodeUsage nodeUsage = new NodeUsage(node.getNodeId(),
                    node.getNodeVersion(), System.currentTimeMillis(), username, NodeUsageType.READ);
            nodeUsageDAO.addUsage(nodeUsage);
        }
    }

    protected abstract ContentWriter getWriterImpl(Node node) throws IOException;
    protected abstract ContentWriter getWriterImpl(Node node, MimeType mimeType) throws IOException;

    @Override
    public ContentWriter getWriter(Node node, MimeType mimeType) throws IOException
    {
        try
        {
            return getWriterImpl(node, mimeType);
        }
        finally
        {
            Principal principal = UserContext.getUser();
            String username = principal.getName();
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

    @SuppressWarnings("unused")
    protected void applyPatch(ReadableByteChannel inChannel, WritableByteChannel outChannel,
            PatchDocument patchDocument) throws IOException
    {
        int blockSize = patchDocument.getBlockSize();

        ByteBuffer currentData = ByteBuffer.allocate(blockSize);

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

                int bytesRead = inChannel.read(currentData);
                currentData.flip();
                int bytesWritten = outChannel.write(currentData);
                if(bytesWritten != bytesRead)
                {
                    throw new RuntimeException("Wrote too few bytes");
                }
                currentData.clear();

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
//                ByteBuffer dst = ByteBuffer.allocate(chunkSize);
//                int bytesWritten = inChannel.read(dst);
//                dst.flip();
//                outChannel.write(dst);
//                if(bytesWritten != chunkSize)
//                {
//                    throw new RuntimeException("Wrote too few bytes");
//                }
                pos += bytesWritten;
            }

            int patchSize = patch.getSize();
            ReadableByteChannel patchChannel = Channels.newChannel(patch.getStream());
            ByteBuffer patchBB = ByteBuffer.allocate(patchSize);
            patchChannel.read(patchBB);
            patchBB.flip();
            outChannel.write(patchBB);
        }

        //we're done with all the patches, add the remaining blocks
        for(;matchIndex < matchedBlocks.size(); matchIndex++)
        {
            int blockIndex = matchedBlocks.get(matchIndex);

            int bytesRead = inChannel.read(currentData);
            currentData.flip();
            int bytesWritten = outChannel.write(currentData);
            if(bytesWritten != bytesRead)
            {
                throw new RuntimeException("Wrote too few bytes");
            }
            currentData.clear();

//            int chunkSize = -1;
//            if((blockIndex * blockSize) > currentData.limit())
//            {
//                chunkSize = currentData.limit() % blockSize;
//            }
//            else
//            {
//                chunkSize = blockSize;
//            }
//
//            ByteBuffer dst = ByteBuffer.allocate(chunkSize);
//            int bytesWritten = inChannel.read(dst);
//            outChannel.write(dst);
        }
    }

//    public void getEntities(final Node node)
//    {
//        EntityTaggerCallback callback = new EntityTaggerCallback()
//        {
//            @Override
//            public void onSuccess(Entities entities)
//            {
//                logger.debug("Got entities for node " + node + ", " + entities);
//                entitiesDAO.addEntities(null, node, entities);
//            }
//
//            @Override
//            public void onFailure(Throwable ex)
//            {
//                logger.error(ex);
//            }
//        };
//        entityExtracter.getEntities(node.getNodeInternalId(), callback);
//    }
}
