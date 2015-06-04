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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.cacheserver.CacheServer;
import org.alfresco.cacheserver.checksum.Adler32;
import org.alfresco.cacheserver.checksum.Checksum;
import org.alfresco.cacheserver.checksum.ChecksumService;
import org.alfresco.cacheserver.checksum.DocumentChecksums;
import org.alfresco.cacheserver.checksum.Patch;
import org.alfresco.cacheserver.checksum.PatchDocument;
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
	private ChecksumService checksumService;
	private ExecutorService executors = Executors.newFixedThreadPool(10);

	private static File root(String contentRoot) throws IOException
	{
		File file = new File(contentRoot);
		if(!file.exists())
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

	public ContentStore(String contentRoot, ChecksumService checksumService) throws IOException
	{
		this(root(contentRoot), checksumService);
	}

	public ContentStore(ChecksumService checksumService) throws IOException
	{
		this(temporary(), checksumService);
	}

	public ContentStore(File rootDirectory, ChecksumService checksumService)
	{
		this.rootDirectory = rootDirectory;
		this.checksumService = checksumService;
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
            try
            {
            	synchronized(ContentStore.class)
                {
                	ContentStore.class.wait(20L);
                }
            }
            catch(InterruptedException e)
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

//    public File write(InputStream in, String contentUrl) throws IOException
//    {
//		File file = createNewFile(contentUrl);
//		OutputStream out = new FileOutputStream(file);
//		try
//		{
//			copyStreams(in, out);     // both streams are closed
//		}
//		finally
//		{
//			in.close();
//			out.close();
//		}
//
//		return file;
//    }

    public File write(InputStream in) throws IOException
    {
    	return write(in, true);
    }

    public File create() throws IOException
    {
		String contentUrl = createNewFileStoreUrl();
		File file = createNewFile(contentUrl);
		return file;
    }

    public File write(InputStream in, boolean async) throws IOException
    {
		String contentUrl = createNewFileStoreUrl();
		File file = createNewFile(contentUrl);
		String contentPath = file.getAbsolutePath();
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try
		{
			logger.debug("ContentStore writing to " + contentPath);
			copyStreams(in, out);     // both streams are closed
		}
		finally
		{
			in.close();
			out.close();
		}

		if(async)
		{
			extractChecksumsAsync(contentPath);
		}
		else
		{
			extractChecksums(contentPath);
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

	@SuppressWarnings("resource")
    public FileChannel getChannel(String contentPath) throws IOException
	{
		File file = new File(contentPath);
		FileInputStream fin = new FileInputStream(file);
		FileChannel channel = fin.getChannel();
		return channel;
	}

	public String applyPatch(PatchDocument patchDocument, String existingContentPath) throws IOException
	{
//		if(patchDocument.getPatches().size() == 0)
//		{
//			if(currentData.limit() / blockSize == patchDocument.getMatchCount())
//			{
//				for(int i = 1; i <= patchDocument.getMatchCount(); i++)
//				{
//					if(patchDocument.getMatchedBlocks().get(i-1) != i)
//					{
//						break;
//					}
//				}
//				if((i - 1) == matchCount) return data; //exact match
//			}
//		}

//		if(!currentData.isReadOnly())
//		{
//			currentData.flip();
//		}

		File inFile = new File(existingContentPath);
		if(!inFile.exists())
		{
			throw new IllegalArgumentException();
		}

		String newContentUrl = createNewFileStoreUrl();
		File outFile = createNewFile(newContentUrl);
		String outContentPath = outFile.getAbsolutePath();

		int blockSize = checksumService.getBlockSize();

		FileInputStream fis = new FileInputStream(inFile);
		FileChannel inChannel = fis.getChannel();
		FileOutputStream fos = new FileOutputStream(outFile);
		FileChannel outChannel = fos.getChannel();

		try
		{

			ByteBuffer currentData = ByteBuffer.allocate(1024);
			int bytesRead = inChannel.read(currentData);

	//		ByteBuffer result = ByteBuffer.allocate(1024);
			int matchIndex = 0;
			List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
			for(Patch patch : patchDocument.getPatches())
			{
				int lastMatchingBlockIndex = patch.getLastMatchIndex();
				for(;matchIndex < matchedBlocks.size(); matchIndex++)
				{
					int blockIndex = matchedBlocks.get(matchIndex);
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
	
	//				currentData.position(blockIndex * blockSize);
	//				currentData.mark();
	
					long bytesWritten = inChannel.transferTo(blockIndex * blockSize, chunkSize, outChannel);
					if(bytesWritten != chunkSize)
					{
						throw new RuntimeException("Wrote too few bytes");
					}
	
	
	//				for(int k = 0; k < chunkSize; k++)
	//				{
	//					byte b = currentData.get();
	//					result.put(b);
	//				}
	//				currentData.reset();
				}

				ByteBuffer patchBuffer = ByteBuffer.wrap(patch.getBuffer());
				outChannel.write(patchBuffer);

	//			byte[] patchBuffer = patch.getBuffer();
	//			result.put(patchBuffer);
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
				if(bytesWritten != chunkSize)
				{
					throw new RuntimeException("Wrote too few bytes");
				}
	
	//			currentData.position((blockIndex-1) * blockSize);
	//			currentData.mark();
	//			for(int k = 0; k < chunkSize; k++)
	//			{
	//				byte b = currentData.get();
	//				result.put(b);
	//			}
	//			currentData.reset();
		    }
		}
		finally
		{
			inChannel.close();
			outChannel.close();
			fis.close();
			fos.close();
		}

		return outContentPath;
	}

	public DocumentChecksums extractChecksums(final String contentPath)
	{
		try
		{
			DocumentChecksums documentChecksums = null;

			FileChannel fc = getChannel(contentPath);
			int blockSize = checksumService.getBlockSize();

			try
			{
				ByteBuffer data = ByteBuffer.allocate(48);
				int bytesRead = fc.read(data);
				data.flip();
		
				long numBlocks = data.limit()/blockSize + 1;
		
				documentChecksums = new DocumentChecksums(contentPath, blockSize, numBlocks);

				//spin through the data and create checksums for each block
				for(int i=0; i < numBlocks; i++)
				{
					int start = i * blockSize;
					int end = (i * blockSize) + blockSize;
		
					//calculate the adler32 checksum
					Adler32 adlerInfo = checksumService.adler32(start, end - 1, data);
					System.out.println("adler32:" + start + "," + (end - 1) + "," + adlerInfo.toString());
		//			int checksum = adlerInfo.checksum;
		//			offset++;

					//calculate the full md5 checksum
					int chunkLength = blockSize;
					if((start + blockSize) > data.limit())
					{
						chunkLength = data.limit() - start;
					}
		
					byte[] chunk = new byte[chunkLength];
					for(int k = 0; k < chunkLength; k++)
					{
						chunk[k] = data.get(k + start);
					}
					String md5sum = checksumService.md5(chunk);
					Checksum checksum = new Checksum(i, adlerInfo.getHash(), adlerInfo.getChecksum(), md5sum);
					documentChecksums.addChecksum(checksum);
				}
			}
			finally
			{
				if(fc != null)
				{
					fc.close();
				}
			}

			checksumService.saveChecksums(documentChecksums);

			return documentChecksums;
		}
		catch(NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void extractChecksumsAsync(final String contentPath)
	{
		executors.submit(new Runnable()
		{
			@Override
			public void run()
			{
				extractChecksums(contentPath);
			}
		});
	}
}
