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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.checksum.ChecksumService;
import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.contentstore.dao.NodeInfo;
import org.alfresco.contentstore.dao.NodeUsage;
import org.alfresco.contentstore.dao.UserContext;
import org.alfresco.extensions.common.Content;
import org.alfresco.extensions.common.GUID;
import org.alfresco.extensions.common.Node;
import org.alfresco.httpclient.AuthenticationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * @author sglover
 *
 */
// TODO orphaned content - link to ContentDAO?
public abstract class AbstractContentStore implements ContentStore
{
    private static Log logger = LogFactory.getLog(AbstractContentStore.class);

    protected File rootDirectory;
    protected ChecksumService checksumService;

    private static File root(String contentRoot) throws IOException
    {
        File file = new File(contentRoot);
        if (!file.exists())
        {
            makeDirectory(file);
        }
        return file;
    }

    public File getRootDirectory()
    {
        return rootDirectory;
    }

    private static File temporary() throws IOException
    {
        File folder = File.createTempFile("CacheContentStore", "", null);
        folder.delete();
        folder.mkdir();
        return folder;
    }

    public AbstractContentStore(String contentRoot,
            ChecksumService checksumService) throws IOException
    {
        this(root(contentRoot), checksumService);
    }

    public AbstractContentStore(ChecksumService checksumService)
            throws IOException
    {
        this(temporary(), checksumService);
    }

    public AbstractContentStore(File rootDirectory,
            ChecksumService checksumService)
    {
        this.rootDirectory = rootDirectory;
        this.checksumService = checksumService;
    }

    protected File makeFile(String contentUrl)
    {
        // take just the part after the protocol

        // get the file
        File file = new File(rootDirectory, contentUrl);

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

    protected File createNewFile(String newContentUrl) throws IOException
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
        return file;
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

    public File create() throws IOException
    {
        String contentUrl = createNewFileStoreUrl();
        File file = createNewFile(contentUrl);
        return file;
    }

    private void fastCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        while(src.read(buffer) != -1)
        {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }

        buffer.flip();

        while(buffer.hasRemaining())
        {
            dest.write(buffer);
        }
    }

    public File write(Node node, Content content, boolean syncChecksums)
            throws IOException
    {
        String mimeType = content.getMimeType();
        Long size = content.getSize();
        ReadableByteChannel inChannel = content.getChannel();
        String contentUrl = createNewFileStoreUrl();
        File file = createNewFile(contentUrl);
        String contentPath = file.getAbsolutePath();
        FileOutputStream out = new FileOutputStream(file);
        FileChannel outChannel = out.getChannel();
        try
        {
            logger.debug("ContentStore writing to " + contentPath);
            fastCopy(inChannel, outChannel);
        }
        finally
        {
            if (inChannel != null)
            {
                inChannel.close();
            }
            if (out != null)
            {
                out.close();
            }
        }

        NodeInfo nodeInfo = NodeInfo.start(node)
                .setContentPath(contentPath)
                .setMimeType(mimeType)
                .setSize(size);
        contentDAO.updateNode(nodeInfo);

        if (syncChecksums)
        {
            checksumService.extractChecksums(node, contentPath);
        }
        else
        {
            checksumService.extractChecksumsAsync(node, contentPath);
        }

        return file;
    }

    public File write(Node node, Content content) throws IOException
    {
        return write(node, content, true);
    }

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

    @SuppressWarnings("resource")
    public FileChannel getChannel(String contentPath) throws IOException
    {
        File file = new File(contentPath);
        FileInputStream fin = new FileInputStream(file);
        FileChannel channel = fin.getChannel();
        return channel;
    }

    private ContentDAO contentDAO;

    @Override
    public Content getByNodeId(String nodeId, String nodeVersion, boolean isPrimary) throws IOException
    {
        NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion, isPrimary);
        String contentPath = nodeInfo.getContentPath();
    
        SeekableByteChannel channel = getContent(contentPath);
        UserDetails userDetails = UserContext.getUser();
        String username = userDetails.getUsername();
        NodeUsage nodeUsage = new NodeUsage(nodeId, nodeVersion, System.currentTimeMillis(), username);
        contentDAO.addUsage(nodeUsage);
    
        String mimeType = nodeInfo.getMimeType();
        Long size = nodeInfo.getSize();
    
        Content content = new Content(channel, mimeType, size);
        return content;
    }

    @Override
    public Content getTextContent(long nodeId) throws AuthenticationException, IOException
    {
        Content content = null;

        NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, "text/plain");
        if(nodeInfo != null)
        {
            String contentPath = nodeInfo.getContentPath();
            ReadableByteChannel channel = getContent(contentPath);

            content = new Content(channel, "text/plain", -1l);
        }

        return content;
    }

}
