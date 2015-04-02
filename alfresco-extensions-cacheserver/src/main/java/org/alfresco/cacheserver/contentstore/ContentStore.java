/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.contentstore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.cacheserver.CacheServer;
import org.alfresco.cacheserver.entity.GUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
// TODO orphaned content - link to ContentDAO?
public class ContentStore
{
	private static Log logger = LogFactory.getLog(CacheServer.class);

	private File rootDirectory;

	private static File root(String contentRoot) throws IOException
	{
		File file = new File(contentRoot);
		if(!file.exists())
		{
			makeDirectory(file);
		}
		return file;
	}

	private static File temporary() throws IOException
	{
		File folder = File.createTempFile("CacheContentStore", "", null);
		folder.delete();
		folder.mkdir();
		return folder;
	}

	public ContentStore(String contentRoot) throws IOException
	{
		this(root(contentRoot));
	}

	public ContentStore() throws IOException
	{
		this(temporary());
	}

	public ContentStore(File rootDirectory)
	{
		this.rootDirectory = rootDirectory;
	}

    private File makeFile(String contentUrl)
    {
        // take just the part after the protocol

        // get the file
        File file = new File(rootDirectory, contentUrl);
        
        // done
        return file;
    }

//    private Pair<String, Long> contentUrlCRC(String contentUrl)
//    {
//    	return CrcHelper.getStringCrcPair(contentUrl, 12, false, true);
//    }

    /**
     * Synchronized and retrying directory creation.  Repeated attempts will be made to create the
     * directory, subject to a limit on the number of retries.
     * 
     * @param dir               the directory to create
     * @throws IOException      if an IO error occurs
     */
    private static void makeDirectory(File dir) throws IOException
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
            try { ContentStore.class.wait(20L); } catch (InterruptedException e) {}
            // Did it get created in the meantime
            if (dir.exists())
            {
                // Beaten to it while asleep
                return;
            }
        }
        // It still didn't succeed
        throw new IOException("Failed to create directory for file storage: " +  dir);
    }

    /**
     * Creates a new content URL.  This must be supported by all
     * stores that are compatible with Alfresco.
     * 
     * @return Returns a new and unique content URL
     */
    private String createNewFileStoreUrl()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
//        sb.append(FileContentStore.STORE_PROTOCOL)
//          .append(ContentStore.PROTOCOL_DELIMITER)
        sb.append(year).append('/')
          .append(month).append('/')
          .append(day).append('/')
          .append(hour).append('/')
          .append(minute).append('/')
          .append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;
    }

    private File createNewFile(String newContentUrl) throws IOException
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
                    "When specifying a URL for new content, the URL may not be in use already. \n" +
                    "   store: " + this + "\n" +
                    "   new URL: " + newContentUrl);
        }
        
        // done
        return file;
    }

    /**
     * Copy of the the Spring FileCopyUtils, but does not silently absorb IOExceptions
     * when the streams are closed.  We require the stream write to happen successfully.
     * <p/>
     */
    private long copyStreams(InputStream in, OutputStream out) throws IOException
    {
        return IOUtils.copy(in, out);
    }

    public File write(InputStream in, String contentUrl) throws IOException
    {
		File file = createNewFile(contentUrl);
		OutputStream out = new FileOutputStream(file);
		try
		{
			copyStreams(in, out);     // both streams are closed
		}
		finally
		{
			in.close();
			out.close();
		}

		return file;
    }

    public File write(InputStream in) throws IOException
    {
		String contentUrl = createNewFileStoreUrl();
		File file = createNewFile(contentUrl);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try
		{
			copyStreams(in, out);     // both streams are closed
		}
		finally
		{
			in.close();
			out.close();
		}

		return file;
    }

    public void remove(String contentPath)
    {
		File file = new File(contentPath);
		if(file.exists())
		{
			file.delete();
		}
		else
		{
			logger.warn("Content already removed: " + contentPath);
		}
    }

	public InputStream getContent(String contentPath) throws IOException
	{
		File file = new File(contentPath);
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		return in;
	}
}
