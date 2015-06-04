/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Random;

import org.alfresco.cacheserver.checksum.ChecksumService;
import org.alfresco.cacheserver.checksum.ChecksumServiceImpl;
import org.alfresco.cacheserver.checksum.DocumentChecksums;
import org.alfresco.cacheserver.checksum.PatchDocument;
import org.alfresco.cacheserver.contentstore.ContentStore;
import org.alfresco.cacheserver.dao.ChecksumDAO;
import org.alfresco.cacheserver.dao.mongo.MongoChecksumDAO;
import org.alfresco.cacheserver.dao.mongo.MongoDbFactory;
import org.alfresco.util.GUID;
import org.gytheio.messaging.MessageProducer;
import org.gytheio.messaging.MessagingException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;
import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

/**
 * 
 * @author sglover
 *
 */
public class ChecksumTest
{
	private static MongodForTestsFactory mongoFactory;

	private ChecksumService checksumService;
	private ContentStore contentStore;

	private Random random = new Random(System.currentTimeMillis());

	@BeforeClass
	public static void beforeClass() throws Exception
	{
        mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
    }
    
	@AfterClass
	public static void afterClass()
	{
        mongoFactory.shutdown();
	}

	@Before
	public void before() throws Exception
	{
        final MongoDbFactory factory = new MongoDbFactory();
        boolean useEmbeddedMongo = ("true".equals(System.getProperty("useEmbeddedMongo")) ? true : false);
        if (useEmbeddedMongo)
        {
            final Mongo mongo = mongoFactory.newMongo();
            factory.setMongo(mongo);
        }
        else
        {
            factory.setMongoURI("mongodb://127.0.0.1:27017");
            factory.setDbName("test");
        }
        final DB db = factory.createInstance();

//        final Mongo mongo = mongoFactory.newMongo();
//        DB db = mongoFactory.newDB(mongo);

        File rootDirectory = Files.createTempDir();

		ChecksumDAO checksumDAO = new MongoChecksumDAO(db, "checksums" + System.currentTimeMillis());
		MessageProducer messageProducer = new MessageProducer()
		{
			@Override
			public void send(Object arg0, String arg1, Map<String, Object> arg2)
			        throws MessagingException
			{
			}
			
			@Override
			public void send(Object arg0, String arg1) throws MessagingException
			{
			}
			
			@Override
			public void send(Object arg0, Map<String, Object> arg1)
			        throws MessagingException
			{
			}
			
			@Override
			public void send(Object arg0) throws MessagingException
			{
			}
		};
		CacheServerIdentity cacheServerIdentity = new CacheServerIdentity()
		{
			
			@Override
			public int getPort()
			{
				return 0;
			}
			
			@Override
			public String getId()
			{
				return GUID.generate();
			}
			
			@Override
			public String getHostname()
			{
				return "localhost";
			}
		};
		this.checksumService = new ChecksumServiceImpl(messageProducer, cacheServerIdentity, checksumDAO, 5);
		this.contentStore = new ContentStore(rootDirectory, checksumService);
	}

	private void assertFileEquals(byte[] bytes, String contentPath) throws IOException
	{
		File file = new File(contentPath);
		if(!file.exists())
		{
			fail();
		}
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		try
		{
			ByteBuffer bb = ByteBuffer.allocate(1024);
			channel.read(bb);
			bb.flip();
			assertEquals(0, bb.compareTo(ByteBuffer.wrap(bytes)));
		}
		finally
		{
			fis.close();
			channel.close();
		}
	}

	@Test
	public void test1() throws Exception
	{
		String content = "Hello world";

		InputStream in = new ByteArrayInputStream(content.getBytes());
		try
		{
			File file = contentStore.write(in, false);
			String contentPath = file.getAbsolutePath();
//			checksumService.extractChecksums("1", "1.0", contentPath);
			DocumentChecksums checksums = checksumService.getChecksums(contentPath);
			System.out.println(checksums);

			String newContent = "Hello there world";
			ByteBuffer data = ByteBuffer.allocate(1024);
			data.put(newContent.getBytes());
			data.flip();
			PatchDocument patchDocument = checksumService.createPatchDocument(checksums, data);
			System.out.print(patchDocument);

//			ByteBuffer currentData = ByteBuffer.allocate(1024);
//			currentData.put(content.getBytes());
			String newContentPath = contentStore.applyPatch(patchDocument, contentPath);
			assertFileEquals(newContent.getBytes(), newContentPath);

//			ByteBuffer bb = contentStore.applyPatch(patchDocument, contentPath);
//			byte[] b = new byte[bb.limit()];
//			bb.get(b);
//			String patchedContent = new String(b);
//			assertEquals(newContent, patchedContent);
		}
		finally
		{
			in.close();
		}
	}

	@Test
	public void test2() throws Exception
	{
		// server
		byte[] content = new byte[1024];
		random.nextBytes(content);

		InputStream in = new ByteArrayInputStream(content);
		File file = contentStore.write(in, false);
		String contentPath = file.getAbsolutePath();
		DocumentChecksums checksums = checksumService.getChecksums(contentPath);
		System.out.println(checksums);

		// client
		byte[] newContent = new byte[1024];
		random.nextBytes(newContent);

		ByteBuffer data = ByteBuffer.allocate(1024);
		data.put(newContent);
		data.flip();
		PatchDocument patchDocument = checksumService.createPatchDocument(checksums, data);

		// client -> server
		String newContentPath = contentStore.applyPatch(patchDocument, contentPath);
		assertFileEquals(newContent, newContentPath);
//		ByteBuffer currentData = ByteBuffer.allocate(1024);
//		currentData.put(content);
//		ByteBuffer bb = contentStore.applyPatch(patchDocument, currentData);
//		byte[] b = new byte[bb.limit()];
//		bb.get(b);
//		assertArrayEquals(newContent, b);
	}
}
