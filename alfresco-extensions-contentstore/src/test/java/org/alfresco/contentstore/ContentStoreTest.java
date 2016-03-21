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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.checksum.Checksum;
import org.alfresco.checksum.ChecksumServiceImpl;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.checksum.Patch;
import org.alfresco.checksum.PatchDocument;
import org.alfresco.checksum.dao.ChecksumDAO;
import org.alfresco.checksum.dao.mongo.MongoChecksumDAO;
import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.alfresco.contentstore.dao.UserContext;
import org.alfresco.contentstore.dao.mongo.MongoNodeUsageDAO;
import org.alfresco.extensions.common.GUID;
import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.MongoDbFactory;
import org.alfresco.extensions.common.Node;
import org.alfresco.extensions.common.identity.ServerIdentity;
import org.alfresco.extensions.common.identity.ServerIdentityImpl;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.BasicUserPrincipal;
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

    private ChecksumServiceImpl checksumService;
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

        File rootDirectory = Files.createTempDir();

        System.out.println("rootDirectory = " + rootDirectory);

        ServerIdentity serverIdentity = new ServerIdentityImpl("localhost", 8080, "test");
        long time = System.currentTimeMillis();

        ChecksumDAO checksumDAO = new MongoChecksumDAO(db, "checksums" + time);
        NodeUsageDAO nodeUsageDAO = new MongoNodeUsageDAO(db, "nodeUsage" + time, serverIdentity);

        this.checksumService = new ChecksumServiceImpl(checksumDAO, 5);

//        ContentDAO contentDAO = new MongoContentDAO(db, "nodes" + time, serverIdentity);
//        this.contentStore = new FileContentStoreImpl(rootDirectory.getAbsolutePath(), checksumService,
//                nodeUsageDAO, contentDAO);

        CassandraSession cassandraSession = new CassandraSession("localhost", true);
        this.contentStore = new CassandraContentStore(cassandraSession, rootDirectory.getAbsolutePath(), checksumService,
                nodeUsageDAO, 5);
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

        UserContext.setUser(new BasicUserPrincipal("user1"));
        try
        {
            String nodeId = GUID.generate();
            long nodeVersion = 1l;

            Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
            ContentReader contentReader = new StringContentReader(text, null, node);
            ContentWriter contentWriter = contentStore.getWriter(node, MimeType.TEXT);
            try(InputStream in = contentReader.getStream();
                    OutputStream out = contentWriter.getOutputStream())
            {
                IOUtils.copy(in, out);
            }

            try(InputStream in = contentReader.getStream())
            {
                checksumService.extractChecksums(node, in);
            }

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
    
            long start = System.nanoTime();

            Node newNode = contentStore.applyPatch(nodeId, nodeVersion, patchDocument);
            long end = System.nanoTime();
            System.out.println("time = " + (end - start)/1000000 + "ms");

            ContentReader contentReader1 = contentStore.getReader(newNode);

            try(InputStream is = contentReader1.getStream())
            {
                byte[] bytes = new byte[1024];
                is.read(bytes);
                System.out.println(new String(bytes, "UTF-8"));
            }

            try(InputStream in3 = new ByteArrayInputStream(newContent.getBytes());
                    InputStream in4 = contentReader1.getStream())
            {
                assertFileEquals(in3, in4);
            }
        }
        finally
        {
            UserContext.clearUser();
        }
    }

    @Test
    public void test2() throws Exception
    {
        UserContext.setUser(new BasicUserPrincipal("user1"));

        String nodeId = GUID.generate();
        long nodeVersion = 1l;
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
        ContentWriter contentWriter = contentStore.getWriter(node, MimeType.XLSX);

        try(InputStream in = getClass().getClassLoader().getResourceAsStream("test.xlsx");
                OutputStream out = contentWriter.getOutputStream())
        {
            IOUtils.copy(in, out);
            checksumService.extractChecksums(node, in);
        }

        NodeChecksums checksums = checksumService.getChecksums(
                node.getNodeId(), node.getNodeVersion());

        try(InputStream in1 = getClass().getClassLoader().getResourceAsStream("test1.xlsx");
                ReadableByteChannel channel1 = Channels.newChannel(in1))
        {
            PatchDocument patchDocument = checksumService.createPatchDocument(
                    checksums, channel1);

            long start = System.nanoTime();

            contentStore.applyPatch(nodeId, nodeVersion, patchDocument);

            long end = System.nanoTime();
            System.out.println("Patch applied time = " + (end - start)/1000000 + "ms");

            node = node.nodeVersion(node.getNodeVersion() + 1);
            ContentReader reader1 = contentStore.getReader(node);
            try(InputStream in3 = getClass().getClassLoader().getResourceAsStream("test1.xlsx");
                    InputStream in4 = reader1.getStream())
            {
                assertFileEquals(in3, in4);
            }
        }
        finally
        {
            UserContext.clearUser();
        }
    }

    private void applyPatch(File f, PatchDocument patchDocument) throws FileNotFoundException, IOException
    {
        int blockSize = checksumService.getBlockSize();

        try(RandomAccessFile file = new RandomAccessFile(f, "rw");
                FileChannel fc = file.getChannel())
        {
            long start = System.currentTimeMillis();

            for(Patch patch : patchDocument.getPatches())
            {
                int blockIndex = patch.getLastMatchIndex();
                long pos = blockIndex * blockSize;
                MappedByteBuffer mem = fc.map(FileChannel.MapMode.READ_WRITE, pos, patch.getSize());
                ByteBuffer bb = ByteBuffer.wrap(patch.getBuffer());
                mem.put(bb);
            }

            long end = System.currentTimeMillis();
            long time = end - start;

            System.out.println("patch time = " + time);
        }
    }

    private File copy(String resourceName) throws IOException
    {
        File f = TempFileProvider.createTempFile("ContentStoreTest", GUID.generate());
        try(InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName);
                OutputStream out = new FileOutputStream(f))
        {
            IOUtils.copy(in, out);
        }
        return f;
    }

    @Test
    public void test3() throws IOException
    {
        checksumService.setBlockSize(8192);
        contentStore.setBlockSize(8192);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        File f = copy("marbles-uncompressed.tif");
        Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);

        try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif"))
        {
            NodeChecksums checksums = checksumService.getChecksums(node, in);
            System.out.println("checksums = " + checksums);

            try(InputStream in1 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif");
                    ReadableByteChannel channel1 = Channels.newChannel(in1))
            {
                PatchDocument patchDocument = checksumService.createPatchDocument(
                        checksums, channel1);
                System.out.println("patchDocument = " + patchDocument);
                applyPatch(f, patchDocument);
            }
        }

        try(InputStream in3 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif");
                InputStream in4 = new FileInputStream(f))
        {
            assertFileEquals(in3, in4);
        }
    }

    @Test
    public void test4() throws IOException
    {
        checksumService.setBlockSize(4);
        contentStore.setBlockSize(4);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);

        String content = "content";
        String content1 = "content1";

        try(InputStream in = new ByteArrayInputStream(content.getBytes());
                InputStream in1 = new ByteArrayInputStream(content1.getBytes()))
        {
//        try(InputStream in = getClass().getClassLoader().getResourceAsStream("test.xlsx");
//                InputStream in1 = getClass().getClassLoader().getResourceAsStream("test1.xlsx"))
//        {
            NodeChecksums checksums1 = checksumService.getChecksums(node, in);
            Checksum checksum1 = checksums1.getChecksumsByBlock().get(0);
            NodeChecksums checksums2 = checksumService.getChecksums(node, in1);
            Checksum checksum2 = checksums2.getChecksumsByBlock().get(0);
            System.out.println(checksum1);
            System.out.println(checksum2);
        }
    }

    @Test
    public void test5() throws IOException
    {
        try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif");
                InputStream in1 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif"))
        {
            ByteBuffer buf1 = ByteBuffer.allocate(8192);
            ByteBuffer buf2 = ByteBuffer.allocate(8192);
            ReadableByteChannel channel1 = Channels.newChannel(in);
            ReadableByteChannel channel2 = Channels.newChannel(in1);
            int numRead1 = -1;
            int numRead2 = -1;
            int total = 0;
            int same = 0;
//            do
//            {
                numRead1 = channel1.read(buf1);
                numRead2 = channel2.read(buf2);
                buf1.clear();
                buf2.clear();

                numRead1 = channel1.read(buf1);
                numRead2 = channel2.read(buf2);

                buf1.flip();
                buf2.flip();
                if(numRead1 > 0 && numRead1 == numRead2)
                {
                    while(buf1.hasRemaining())
                    {
                        total++;
                        byte b1 = buf1.get();
                        byte b2 = buf2.get();
                        if(b1 == b2)
                        {
                            same++;
                        }
                    }
                    buf1.clear();
                    buf2.clear();
                }
//            }
//            while(numRead1 > 0 && numRead1 == numRead2);

            System.out.println(numRead1 + ", " + numRead2 + ", " + total + ", " + same + ", " + (double)same/total);
        }
    }
}
