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

import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.alfresco.contentstore.dao.UserContext;
import org.alfresco.contentstore.dao.mongo.MongoNodeUsageDAO;
import org.alfresco.contentstore.patch.PatchService;
import org.alfresco.contentstore.patch.PatchServiceImpl;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.BasicUserPrincipal;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sglover.alfrescoextensions.common.CassandraSession;
import org.sglover.alfrescoextensions.common.GUID;
import org.sglover.alfrescoextensions.common.Hasher;
import org.sglover.alfrescoextensions.common.MimeType;
import org.sglover.alfrescoextensions.common.MongoDbFactory;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.alfrescoextensions.common.identity.ServerIdentity;
import org.sglover.alfrescoextensions.common.identity.ServerIdentityImpl;
import org.sglover.checksum.ChecksumServiceImpl;
import org.sglover.checksum.NodeChecksums;
import org.sglover.checksum.Patch;
import org.sglover.checksum.PatchDocument;
import org.sglover.checksum.PatchDocumentImpl;
import org.sglover.checksum.dao.ChecksumDAO;
import org.sglover.checksum.dao.mongo.MongoChecksumDAO;
import org.sglover.entities.EntitiesService;
import org.sglover.entities.EntitiesServiceImpl;
import org.sglover.entities.EntitiesServiceImpl.ExtracterType;
import org.sglover.entities.dao.cassandra.CassandraEntitiesDAO;
import org.sglover.entities.dao.cassandra.CassandraSimilarityDAO;
import org.sglover.nlp.DefaultModelLoader;
import org.sglover.nlp.ModelLoader;

import com.google.common.io.Files;
import com.mongodb.DB;

/**
 * 
 * @author sglover
 *
 */
public class ContentStoreTest
{
    private static MongoDbFactory mongoFactory;

    private ChecksumServiceImpl checksumService;
    private AbstractContentStore contentStore;
    private PatchService patchService;
    private EntitiesService entitiesService;

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        mongoFactory = new MongoDbFactory(true, "", "test", true);
    }

    @AfterClass
    public static void afterClass()
    {
        mongoFactory.shutdown();
    }

    @Before
    public void before() throws Exception
    {
        final DB db = mongoFactory.createInstance();

        File rootDirectory = Files.createTempDir();

        System.out.println("rootDirectory = " + rootDirectory);

        ServerIdentity serverIdentity = new ServerIdentityImpl("localhost", 8080, "test");
        long time = System.currentTimeMillis();

        ChecksumDAO checksumDAO = new MongoChecksumDAO(db, "checksums" + time);
        NodeUsageDAO nodeUsageDAO = new MongoNodeUsageDAO(db, "nodeUsage" + time, serverIdentity);

        Hasher hasher = new Hasher();
        this.checksumService = new ChecksumServiceImpl(checksumDAO, 5, hasher);
        this.patchService = new PatchServiceImpl(checksumService, hasher);

        CassandraSession cassandraSession = new CassandraSession("localhost", "alfresco", false);

        ModelLoader modelLoader = new DefaultModelLoader();
        CassandraEntitiesDAO entitiesDAO = new CassandraEntitiesDAO(cassandraSession);
        CassandraSimilarityDAO similarityDAO = new CassandraSimilarityDAO(cassandraSession);

        this.entitiesService = new EntitiesServiceImpl(
                ExtracterType.StanfordNLP.toString(), modelLoader, entitiesDAO,
                similarityDAO/*, contentStore*/);

        this.contentStore = new CassandraContentStore(cassandraSession, checksumService, patchService,
                rootDirectory.getAbsolutePath(), nodeUsageDAO, entitiesService, false);
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

    private static class State
    {
        private int idx = 0;
        
        public void incIdx(int delta)
        {
            this.idx += delta;
        }

        @Override
        public String toString()
        {
            return "idx=" + idx + "";
        }
    }

    private void assertFileEquals(InputStream expected, InputStream actual, State state) throws IOException
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
                assertTrue("Not equal at " + state, bb1.equals(bb2));
            }
            else
            {
                fail("Not equal at " + state);
            }
        }
    }

    private PatchDocument getPatch(Node node, String content) throws IOException
    {
        PatchDocument patchDocument = new PatchDocumentImpl();

        ByteBuffer data = ByteBuffer.allocate(1024*10);
        data.put(content.getBytes());
        data.flip();

        NodeChecksums checksums = checksumService.getChecksums(node.getNodeId(), node.getNodeVersion());
        patchService.updatePatchDocument(patchDocument, checksums, data);

        return patchDocument;
    }

    private PatchDocument getPatch(Node node, ReadableByteChannel channel) throws IOException
    {
        PatchDocument patchDocument = new PatchDocumentImpl();

        NodeChecksums checksums = checksumService.getChecksums(node.getNodeId(), node.getNodeVersion());
        patchService.updatePatchDocument(patchDocument, checksums, channel);

        return patchDocument;
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
            MimeType mimeType = MimeType.TEXT;
            Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion).mimeType(mimeType);

            ContentReader contentReader = new StringContentReader(text, null, node);
            ContentWriter contentWriter = contentStore.getWriter(node);
            try(InputStream in = contentReader.getStream();
                    OutputStream out = contentWriter.getOutputStream())
            {
                IOUtils.copy(in, out);
            }

            String newContent = "Hello world again";
            PatchDocument patchDocument = getPatch(node, newContent);

            long start = System.nanoTime();
            Node newNode = contentStore.applyPatch(node, patchDocument);
            long end = System.nanoTime();
            System.out.println("time = " + (end - start)/1000000 + "ms");

            ContentReader contentReader1 = contentStore.getReader(newNode);

            try(InputStream is = contentReader1.getStream())
            {
                byte[] bytes = new byte[1024];
                is.read(bytes);
                System.out.println(new String(bytes, "UTF-8"));
            }

            State state = new State();
            try(InputStream in3 = new ByteArrayInputStream(newContent.getBytes());
                    InputStream in4 = contentReader1.getStream())
            {
                assertFileEquals(in3, in4, state);
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

        checksumService.setBlockSize(1024*8); // 8K

        String nodeId = GUID.generate();
        long nodeVersion = 1l;
        MimeType mimeType = MimeType.XLSX;
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion).mimeType(mimeType);

        ContentWriter contentWriter = contentStore.getWriter(node);

        try(InputStream in = getClass().getClassLoader().getResourceAsStream("test.xlsx");
                OutputStream out = contentWriter.getOutputStream())
        {
            IOUtils.copy(in, out);
        }

        try(ReadableByteChannel channel1 = Channels.newChannel(getClass().getClassLoader().getResourceAsStream("test1.xlsx")))
        {
            PatchDocument patchDocument = getPatch(node, channel1);

            long start = System.nanoTime();

            contentStore.applyPatch(node, patchDocument);

            long end = System.nanoTime();
            System.out.println("Patch applied time = " + (end - start)/1000000 + "ms");

            node = node.nodeVersion(node.getNodeVersion() + 1);
            ContentReader reader1 = contentStore.getReader(node);
            State state = new State();
            try(InputStream in3 = getClass().getClassLoader().getResourceAsStream("test1.xlsx");
                    InputStream in4 = reader1.getStream())
            {
                assertFileEquals(in3, in4, state);
            }
        }
        finally
        {
            UserContext.clearUser();
        }
    }

    @Test
    public void test3() throws IOException
    {
        checksumService.setBlockSize(8192);

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
                PatchDocument patchDocument = new PatchDocumentImpl();
                patchService.updatePatchDocument(patchDocument, checksums, channel1);
                System.out.println("patchDocument = " + patchDocument);
                applyPatch(f, patchDocument);
            }
        }

        State state = new State();

        try(InputStream in3 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif");
                InputStream in4 = new FileInputStream(f))
        {
            assertFileEquals(in3, in4, state);
        }
    }

//    @Test
//    public void test4() throws IOException
//    {
//        checksumService.setBlockSize(4);
//
//        UserContext.setUser(new BasicUserPrincipal("user1"));
//
//        Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);
//
//        String content = "content";
//        String content1 = "content1";
//
//        try(InputStream in = new ByteArrayInputStream(content.getBytes());
//                InputStream in1 = new ByteArrayInputStream(content1.getBytes()))
//        {
////        try(InputStream in = getClass().getClassLoader().getResourceAsStream("test.xlsx");
////                InputStream in1 = getClass().getClassLoader().getResourceAsStream("test1.xlsx"))
////        {
//            NodeChecksums checksums1 = checksumService.getChecksums(node, in);
//            Checksum checksum1 = checksums1.getChecksumsByBlock().get(0);
//            NodeChecksums checksums2 = checksumService.getChecksums(node, in1);
//            Checksum checksum2 = checksums2.getChecksumsByBlock().get(0);
//            System.out.println(checksum1);
//            System.out.println(checksum2);
//        }
//    }

//    @Test
//    public void test5() throws IOException
//    {
//        try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif");
//                InputStream in1 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif"))
//        {
//            ByteBuffer buf1 = ByteBuffer.allocate(8192);
//            ByteBuffer buf2 = ByteBuffer.allocate(8192);
//            ReadableByteChannel channel1 = Channels.newChannel(in);
//            ReadableByteChannel channel2 = Channels.newChannel(in1);
//            int numRead1 = -1;
//            int numRead2 = -1;
//            int total = 0;
//            int same = 0;
////            do
////            {
//                numRead1 = channel1.read(buf1);
//                numRead2 = channel2.read(buf2);
//                buf1.clear();
//                buf2.clear();
//
//                numRead1 = channel1.read(buf1);
//                numRead2 = channel2.read(buf2);
//
//                buf1.flip();
//                buf2.flip();
//                if(numRead1 > 0 && numRead1 == numRead2)
//                {
//                    while(buf1.hasRemaining())
//                    {
//                        total++;
//                        byte b1 = buf1.get();
//                        byte b2 = buf2.get();
//                        if(b1 == b2)
//                        {
//                            same++;
//                        }
//                    }
//                    buf1.clear();
//                    buf2.clear();
//                }
////            }
////            while(numRead1 > 0 && numRead1 == numRead2);
//
//            System.out.println(numRead1 + ", " + numRead2 + ", " + total + ", " + same + ", " + (double)same/total);
//        }
//    }
    
    @Test
    public void testEntities() throws Exception
    {
        String text = "Steve Glover was here";

        UserContext.setUser(new BasicUserPrincipal("user1"));
        try
        {
            String nodeId = GUID.generate();
            long nodeVersion = 1l;
            MimeType mimeType = MimeType.TEXT;
            Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion).mimeType(mimeType);

            ContentReader contentReader = new StringContentReader(text, null, node);
            ContentWriter contentWriter = contentStore.getWriter(node);
            try(InputStream in = contentReader.getStream();
                    OutputStream out = contentWriter.getOutputStream())
            {
                IOUtils.copy(in, out);
            }


        }
        finally
        {
            UserContext.clearUser();
        }
    }

    @Test
    public void test4() throws IOException
    {
        class TestData
        {
            long patchTime;
            long nonPatchTime;
        }

        int slowStreamBlockSize = 1024*8;
        int slowStreamDelay = 10;
        TestData testData = new TestData();

        checksumService.setBlockSize(8192);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        String nodeId = GUID.generate();
        long nodeVersion = 1l;
        MimeType mimeType = MimeType.XLSX;
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion).mimeType(mimeType);

        {
            ContentWriter writer = contentStore.getWriter(node);
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif");
                    OutputStream out = writer.getOutputStream())
            {
                IOUtils.copy(in, out);
            }
        }

        // write updated content, should create a patch
        {
            node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion + 1).mimeType(mimeType);
            ContentWriter writer = contentStore.getWriter(node);
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif");
                    OutputStream out = writer.getOutputStream())
            {
                IOUtils.copy(in, out);
            }
        }

        File f = TempFileProvider.createTempFile("ContentStoreTest", GUID.generate());
        try(OutputStream out = new FileOutputStream(f))
        {
            contentStore.writePatchAsProtocolBuffer(node, out);
        }

        File f1 = copy("marbles-uncompressed.tif");

        {
            long start = System.currentTimeMillis();
    
            try(InputStream in = new SlowInputStream(new FileInputStream(f), slowStreamBlockSize, slowStreamDelay))
            {
                PatchDocument patchDocument = patchService.getPatch(in);
    
                System.out.println("patchDocument = " + patchDocument);

                try(FileInputStream fis = new FileInputStream(f1);
                        ReadableByteChannel channel1 = fis.getChannel())
                {
                    applyPatch(f, patchDocument);
                }
            }
    
            long end = System.currentTimeMillis();
    
            testData.patchTime = end - start;
        }

        File f2 = TempFileProvider.createTempFile("ContentStoreTest", GUID.generate());

        {
            long start = System.currentTimeMillis();

            try(InputStream in = new SlowInputStream(getClass().getClassLoader()
                    .getResourceAsStream("marbles-uncompressed1.tif"), slowStreamBlockSize, slowStreamDelay);
                    OutputStream out = new FileOutputStream(f2))
            {
                IOUtils.copy(in, out);
            }
    
            long end = System.currentTimeMillis();

            testData.nonPatchTime = end - start;
        }

        System.out.println("f1 = " + f1.getAbsolutePath());
        System.out.println("f2 = " + f2.getAbsolutePath());
        System.out.println("patch time (ms) = " + testData.patchTime);
        System.out.println("non patch time (ms) = " + testData.nonPatchTime);
        System.out.println(testData.nonPatchTime/testData.patchTime + "x faster");

        State state = new State();
        try(InputStream in1 = new FileInputStream(f1); InputStream in2 = new FileInputStream(f2))
        {
            assertFileEquals(in1, in2, state);
        }
    }

    @Test
    public void test5() throws IOException
    {
        checksumService.setBlockSize(8192);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        String nodeId = GUID.generate();
        long nodeVersion = 1l;
        MimeType mimeType = MimeType.XLSX;
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion).mimeType(mimeType);

        File f = copy("marbles-uncompressed.tif");

        try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif"))
        {
            NodeChecksums checksums = checksumService.getChecksums(node, in);
            System.out.println("checksums = " + checksums);

            try(InputStream in1 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif");
                    ReadableByteChannel channel1 = Channels.newChannel(in1))
            {
                PatchDocument patchDocument = new PatchDocumentImpl();
                patchService.updatePatchDocument(patchDocument, checksums, channel1);
                System.out.println("patchDocument = " + patchDocument);
                applyPatch(f, patchDocument);
            }
        }

        State state = new State();
        try(InputStream in1 = new FileInputStream(f);
                InputStream in2 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif"))
        {
            assertFileEquals(in1, in2, state);
        }
    }
}
