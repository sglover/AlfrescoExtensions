/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.ChecksumServiceImpl;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.checksum.PatchDocument;
import org.alfresco.checksum.dao.ChecksumDAO;
import org.alfresco.checksum.dao.mongo.MongoChecksumDAO;
import org.alfresco.extensions.common.Content;
import org.alfresco.extensions.common.GUID;
import org.alfresco.extensions.common.MongoDbFactory;
import org.alfresco.extensions.common.Node;
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
public class ContentStoreTest
{
    private static MongodForTestsFactory mongoFactory;

    private ChecksumService checksumService;
    private AbstractContentStore contentStore;

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
        boolean useEmbeddedMongo = ("true".equals(System
                .getProperty("useEmbeddedMongo")) ? true : false);
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

        // final Mongo mongo = mongoFactory.newMongo();
        // DB db = mongoFactory.newDB(mongo);

        File rootDirectory = Files.createTempDir();

        System.out.println("rootDirectory = " + rootDirectory);

        ChecksumDAO checksumDAO = new MongoChecksumDAO(db, "checksums"
                + System.currentTimeMillis());
        // MessageProducer messageProducer = new MessageProducer()
        // {
        // @Override
        // public void send(Object arg0, String arg1, Map<String, Object> arg2)
        // throws MessagingException
        // {
        // }
        //
        // @Override
        // public void send(Object arg0, String arg1) throws MessagingException
        // {
        // }
        //
        // @Override
        // public void send(Object arg0, Map<String, Object> arg1)
        // throws MessagingException
        // {
        // }
        //
        // @Override
        // public void send(Object arg0) throws MessagingException
        // {
        // }
        // };
        // CacheServerIdentity cacheServerIdentity = new CacheServerIdentity()
        // {
        //
        // @Override
        // public int getPort()
        // {
        // return 0;
        // }
        //
        // @Override
        // public String getId()
        // {
        // return GUID.generate();
        // }
        //
        // @Override
        // public String getHostname()
        // {
        // return "localhost";
        // }
        // };
        this.checksumService = new ChecksumServiceImpl(checksumDAO, 5);
        this.contentStore = new FileContentStore(rootDirectory, checksumService);
    }

    private void assertFileEquals(byte[] bytes, String contentPath)
            throws IOException
    {
        File file = new File(contentPath);
        if (!file.exists())
        {
            fail();
        }

        try(FileInputStream fis = new FileInputStream(file);
                FileChannel channel = fis.getChannel())
        {
            ByteBuffer bb = ByteBuffer.allocate(1024);
            channel.read(bb);
            bb.flip();
            assertEquals(0, bb.compareTo(ByteBuffer.wrap(bytes)));
        }
    }

    private void assertFileEquals(InputStream expected, InputStream actual) throws IOException
    {
        ByteBuffer bb1 = ByteBuffer.allocate(1024);
        ByteBuffer bb2 = ByteBuffer.allocate(1024);
        int count1 = 0;
        int count2 = 0;

        try(ReadableByteChannel channel = Channels.newChannel(expected);
            ReadableByteChannel channel1 = Channels.newChannel(actual))
        {
            int i1 = channel.read(bb1);
            bb1.flip();

            int i2 = channel1.read(bb2);
            bb2.flip();

            if(i1 == i2)
            {
                count1 += i1;
                count2 += i2;
                assertTrue(bb1.equals(bb2));
            }
            else
            {
                fail();
            }
        }
    }

    @Test
    public void test1() throws Exception
    {
        String text = "Hello world";
        long size = text.getBytes().length;
        InputStream in = new ByteArrayInputStream(text.getBytes());
        ReadableByteChannel channel = Channels.newChannel(in);

        try
        {
            Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);
            Content content = new Content(channel, "text/plain", size);
            File file = contentStore.write(node, content);
            String contentPath = file.getAbsolutePath();
            // checksumService.extractChecksums("1", "1.0", contentPath);
            NodeChecksums checksums = checksumService.getChecksums(
                    node.getNodeId(), node.getNodeVersion());
            System.out.println(checksums);

            // String newContent = "Hello there world";
            String newContent = "Hello world again";
            ByteBuffer data = ByteBuffer.allocate(1024);
            data.put(newContent.getBytes());
            data.flip();
            PatchDocument patchDocument = checksumService.createPatchDocument(
                    checksums, data);
            System.out.print(patchDocument);

            // ByteBuffer currentData = ByteBuffer.allocate(1024);
            // currentData.put(content.getBytes());

            long start = System.nanoTime();

            String newContentPath = contentStore.applyPatch(patchDocument,
                    contentPath);
            long end = System.nanoTime();
            System.out.println("time = " + (end - start)/1000000 + "ms");
            System.out.println(newContentPath);
            assertFileEquals(newContent.getBytes(), newContentPath);

            // ByteBuffer bb = contentStore.applyPatch(patchDocument,
            // contentPath);
            // byte[] b = new byte[bb.limit()];
            // bb.get(b);
            // String patchedContent = new String(b);
            // assertEquals(newContent, patchedContent);
        }
        finally
        {
            in.close();
        }
    }

    @Test
    public void test2() throws Exception
    {
        try(InputStream in = getClass().getClassLoader().getResourceAsStream("test.xlsx");
                ReadableByteChannel channel = Channels.newChannel(in);
                InputStream in1 = getClass().getClassLoader().getResourceAsStream("test1.xlsx");
                ReadableByteChannel channel1 = Channels.newChannel(in1))
        {
            Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);
            Content content = new Content(channel, "text/plain", null);
            File file = contentStore.write(node, content);
            String contentPath = file.getAbsolutePath();
            System.out.println("contentPath = " + contentPath);

            NodeChecksums checksums = checksumService.getChecksums(
                    node.getNodeId(), node.getNodeVersion());
            System.out.println(checksums);

            PatchDocument patchDocument = checksumService.createPatchDocument(
                    checksums, channel1);
            System.out.print(patchDocument);

            long start = System.nanoTime();

            String newContentPath = contentStore.applyPatch(patchDocument,
                    contentPath);
            System.out.println("newContentPath = " + newContentPath);

            long end = System.nanoTime();
            System.out.println("time = " + (end - start)/1000000 + "ms");

            try(InputStream in3 = getClass().getClassLoader().getResourceAsStream("test1.xlsx");
                    InputStream in4 = new FileInputStream(newContentPath))
            {
                assertFileEquals(in3, in4);
            }

            // ByteBuffer bb = contentStore.applyPatch(patchDocument,
            // contentPath);
            // byte[] b = new byte[bb.limit()];
            // bb.get(b);
            // String patchedContent = new String(b);
            // assertEquals(newContent, patchedContent);
        }
    }

    // @Test
    // public void test2() throws Exception
    // {
    // // server
    // byte[] content = new byte[1024];
    // random.nextBytes(content);
    //
    // InputStream in = new ByteArrayInputStream(content);
    // File file = contentStore.write(in);
    // String contentPath = file.getAbsolutePath();
    // NodeChecksums checksums = checksumService.getChecksums(contentPath);
    // System.out.println(checksums);
    //
    // // client
    // byte[] newContent = new byte[1024];
    // random.nextBytes(newContent);
    //
    // ByteBuffer data = ByteBuffer.allocate(1024);
    // data.put(newContent);
    // data.flip();
    // PatchDocument patchDocument =
    // checksumService.createPatchDocument(checksums, data);
    //
    // // client -> server
    // String newContentPath = contentStore.applyPatch(patchDocument,
    // contentPath);
    // assertFileEquals(newContent, newContentPath);
    // // ByteBuffer currentData = ByteBuffer.allocate(1024);
    // // currentData.put(content);
    // // ByteBuffer bb = contentStore.applyPatch(patchDocument, currentData);
    // // byte[] b = new byte[bb.limit()];
    // // bb.get(b);
    // // assertArrayEquals(newContent, b);
    // }
}
