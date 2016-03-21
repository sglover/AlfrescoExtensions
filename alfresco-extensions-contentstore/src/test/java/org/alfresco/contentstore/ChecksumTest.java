/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import static org.junit.Assert.assertEquals;
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
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.List;

import org.alfresco.checksum.Checksum;
import org.alfresco.checksum.ChecksumServiceImpl;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.checksum.Patch;
import org.alfresco.checksum.PatchDocument;
import org.alfresco.checksum.dao.ChecksumDAO;
import org.alfresco.checksum.dao.mongo.MongoChecksumDAO;
import org.alfresco.contentstore.dao.UserContext;
import org.alfresco.extensions.common.GUID;
import org.alfresco.extensions.common.MongoDbFactory;
import org.alfresco.extensions.common.Node;
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
public class ChecksumTest
{
    private static MongodForTestsFactory mongoFactory;

    private ChecksumServiceImpl checksumService;

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

        long time = System.currentTimeMillis();

        ChecksumDAO checksumDAO = new MongoChecksumDAO(db, "checksums" + time);
        this.checksumService = new ChecksumServiceImpl(checksumDAO, 8192);
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

    private void assertEqual(InputStream expected, InputStream actual) throws IOException
    {
        ByteBuffer bb1 = ByteBuffer.allocate(1024);
        ByteBuffer bb2 = ByteBuffer.allocate(1024);

        try(ReadableByteChannel expectedChannel = Channels.newChannel(expected);
            ReadableByteChannel actualChannel = Channels.newChannel(actual))
        {
            State state = new State();
            for(;;)
            {
                expectedChannel.read(bb1);
                bb1.flip();

                actualChannel.read(bb2);
                bb2.flip();

                assertEqual(bb1, bb2, state);

                bb1.clear();
                bb2.clear();
            }
        }
    }

    private void assertEqual(ByteBuffer expected, ByteBuffer actual, State state) throws IOException
    {
        int expectedRemaining = expected.remaining();
        int actualRemaining = actual.remaining();

        if(expectedRemaining == actualRemaining && expectedRemaining > 0)
        {
            assertEquals(expected.remaining(), actual.remaining());
            while(expected.hasRemaining())
            {
                byte expectedByte = expected.get();
                byte actualByte = actual.get();
                state.incIdx(1);
                assertEquals("Not equal at " + state, expectedByte, actualByte);
            };
        }
        else
        {
            fail("Not equal at " + state + ", " + expectedRemaining + ", " + actualRemaining);
        }
    }

    private void applyPatch(File f, PatchDocument patchDocument) throws FileNotFoundException, IOException
    {
        int blockSize = checksumService.getBlockSize();

        try(RandomAccessFile file = new RandomAccessFile(f, "rw");
                FileChannel fc = file.getChannel())
        {
            long start = System.currentTimeMillis();

            int newLimit = -1;

            Iterator<Patch> patchIt = patchDocument.getPatches().iterator();
            while(patchIt.hasNext())
            {
                Patch patch = patchIt.next();
                int lastMatchingBlockIndex = patch.getLastMatchIndex();
//                int lastMatchingBlock = patchDocument.getMatchedBlocks().get(lastMatchingBlockIndex);
                int pos = (lastMatchingBlockIndex + 1) * blockSize;
//                int pos = (lastMatchingBlockIndex + 1) * blockSize;

                MappedByteBuffer mem = fc.map(FileChannel.MapMode.READ_WRITE, pos, patch.getSize());
//                mem.load();
                ByteBuffer bb = ByteBuffer.wrap(patch.getBuffer());
                mem.put(bb);
//                mem.force();

                if(!patchIt.hasNext())
                {
                    int x = pos + patch.getSize();
                    if(x < mem.limit())
                    {
                        newLimit = x;
                    }
                }
            }

            if(newLimit != -1)
            {
                fc.truncate(newLimit);
            }

            long end = System.currentTimeMillis();
            long time = end - start;

            System.out.println("patch time = " + time);
        }
    }

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
        }

        boolean nextBlock() throws IOException
        {
            if(matchIndex >= matchedBlocks.size())
            {
                return false;
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

                return true;
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

        for(Patch patch : patchDocument.getPatches())
        {
            int lastMatchingBlockIndex = patch.getLastMatchIndex();

            while(c.nextBlock())
            {
                int blockIndex = c.getCurrentBlockIndex();
                if(blockIndex > lastMatchingBlockIndex)
                {
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

                    if(patchSize < c.blockSize)
                    {
                        // apply remainder of current block

                        c.currentBlock.position(patchSize);

                        bytesWritten = outChannel.write(c.currentBlock);
                        totalWritten += bytesWritten;
                        if(bytesWritten != c.blockSize - patchSize)
                        {
                            throw new RuntimeException("Wrote too few bytes");
                        }

                        c.currentBlock.position(0);
                    }
//                    else
//                    {
//                        // skip blocks covered by patch
//
//                        while(patchSize > c.blockSize)
//                        {
//                            if(!c.nextBlock())
//                            {
//                                throw new RuntimeException();
//                            }
//
//                            patchSize -= c.blockSize;
//                        };
//
//                        // apply partial block prefix
//
//                        c.currentBlock.position(patchSize);
//
//                        bytesWritten = outChannel.write(c.currentBlock);
//                        totalWritten += bytesWritten;
//                        if(bytesWritten != (c.blockSize - patchSize))
//                        {
//                            throw new RuntimeException("Wrote too few bytes");
//                        }
//
//                        c.currentBlock.position(0);
//                    }

                    break; // out of loop
                }
                else
                {
                    int bytesWritten = outChannel.write(c.currentBlock);
                    totalWritten += bytesWritten;
                    if(bytesWritten != c.blockSize)
                    {
                        throw new RuntimeException("Wrote too few bytes, " + c.blockSize + ", " + bytesWritten);
                    }
                }
            }
        }

        // we're done with all the patches, add the remaining blocks
        while(c.nextBlock())
        {
            int bytesWritten = outChannel.write(c.currentBlock);
            totalWritten += bytesWritten;
            if(bytesWritten != c.blockSize)
            {
                throw new RuntimeException("Wrote too few bytes");
            }
        }

        return totalWritten;
    }

    private void applyPatch(ByteBuffer mem, PatchDocument patchDocument) throws FileNotFoundException, IOException
    {
        int blockSize = checksumService.getBlockSize();

        long start = System.currentTimeMillis();
        int newLimit = -1;

        Iterator<Patch> patchIt = patchDocument.getPatches().iterator();
        while(patchIt.hasNext())
        {
            Patch patch = patchIt.next();
            int lastMatchingBlockIndex = patch.getLastMatchIndex();
//            int lastMatchingBlock = patchDocument.getMatchedBlocks().get(lastMatchingBlockIndex);
            int pos = (lastMatchingBlockIndex + 1) * blockSize;
//            int pos = (lastMatchingBlockIndex + 1) * blockSize;
            mem.position(pos);

            ByteBuffer bb = ByteBuffer.wrap(patch.getBuffer());

            mem.put(bb);

            if(!patchIt.hasNext())
            {
                int x = pos + patch.getSize();
                if(x < mem.limit())
                {
                    newLimit = x;
                }
            }
        }

        mem.position(0);
        if(newLimit != -1)
        {
            mem.limit(newLimit);
        }

        long end = System.currentTimeMillis();
        long time = end - start;

        System.out.println("patch time = " + time);
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

    private ByteBuffer copy(InputStream in) throws IOException
    {
        File f = TempFileProvider.createTempFile("ContentStoreTest", GUID.generate());
        try(OutputStream out = new FileOutputStream(f))
        {
            IOUtils.copy(in, out);
        }
        try(ReadableByteChannel c = Channels.newChannel(new FileInputStream(f)))
        {
            ByteBuffer bb = ByteBuffer.wrap(java.nio.file.Files.readAllBytes(f.toPath()));
            return bb;
        }
    }

    @Test
    public void test0() throws IOException
    {
        checksumService.setBlockSize(5);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);

        try(InputStream in = new ByteArrayInputStream("Hello world".getBytes()))
        {
            NodeChecksums checksums = checksumService.getChecksums(node, in);

            try(InputStream in1 = new ByteArrayInputStream("Hello there world".getBytes());
                    ReadableByteChannel channel1 = Channels.newChannel(in1))
            {
                PatchDocument patchDocument = checksumService.createPatchDocument(
                        checksums, channel1);
                System.out.println("patchDocument = " + patchDocument);
            }
        }
    }

    @Test
    public void test1() throws IOException
    {
        checksumService.setBlockSize(5);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);

        ByteBuffer actual = copy(new ByteArrayInputStream("Hello there world".getBytes()));

        try(InputStream in = new ByteArrayInputStream("Hello there world".getBytes()))
        {
            NodeChecksums checksums = checksumService.getChecksums(node, in);

            try(InputStream in1 = new ByteArrayInputStream("Hello world".getBytes());
                    ReadableByteChannel channel1 = Channels.newChannel(in1))
            {
                PatchDocument patchDocument = checksumService.createPatchDocument(
                        checksums, channel1);
                System.out.println("patchDocument = " + patchDocument);

                applyPatch(actual, patchDocument);
            }
        }

        ByteBuffer expected = ByteBuffer.wrap("Hello world".getBytes());
        State state = new State();
        assertEqual(expected, actual, state);
    }

    @Test
    public void test2() throws IOException
    {
        checksumService.setBlockSize(8192);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        File f = copy("marbles-uncompressed.tif");
        System.out.println("f = " + f);
        Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);

        PatchDocument patchDocument = null;

        try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif"))
        {
            NodeChecksums checksums = checksumService.getChecksums(node, in);

            try(InputStream in1 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif");
                    ReadableByteChannel channel1 = Channels.newChannel(in1))
            {
                patchDocument = checksumService.createPatchDocument(
                        checksums, channel1);
                System.out.println("patchDocument = " + patchDocument);
                applyPatch(f, patchDocument);
            }
        }

        try(InputStream in3 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif");
                InputStream in4 = new FileInputStream(f))
        {
            assertEquals(in3, in4);
        }
    }

    @Test
    public void test3() throws IOException
    {
        checksumService.setBlockSize(8192);

        UserContext.setUser(new BasicUserPrincipal("user1"));

        File f = TempFileProvider.createTempFile("ContentStoreTest", GUID.generate());
//        File f = copy("marbles-uncompressed.tif");
        System.out.println("f = " + f);
        Node node = Node.build().nodeId(GUID.generate()).nodeVersion(1l);

        ClassLoader cl = getClass().getClassLoader();

        try(InputStream in = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed.tif"))
        {
            NodeChecksums checksums = checksumService.getChecksums(node, in);

            try(ReadableByteChannel destChannel = Channels.newChannel(cl.getResourceAsStream("marbles-uncompressed1.tif"));
                    ReadableByteChannel srcChannel = Channels.newChannel(cl.getResourceAsStream("marbles-uncompressed.tif"));
                    FileOutputStream fos = new FileOutputStream(f);
                    WritableByteChannel patchedChannel = fos.getChannel())
            {
                PatchDocument patchDocument = checksumService.createPatchDocument(checksums, destChannel);
                System.out.println("patchDocument = " + patchDocument);
                int totalWritten = applyPatch(srcChannel, patchedChannel, patchDocument);
                System.out.println("totalWritten = " + totalWritten);
            }
        }

        try(InputStream in3 = getClass().getClassLoader().getResourceAsStream("marbles-uncompressed1.tif");
                InputStream in4 = new FileInputStream(f))
        {
            assertEqual(in3, in4);
        }
    }

    @Test
    public void test10() throws IOException
    {
        checksumService.setBlockSize(4);

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
    public void test11() throws IOException
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
            int i = 0;
            do
            {
                total = 0;
                same = 0;

                numRead1 = channel1.read(buf1);
                numRead2 = channel2.read(buf2);
                i += 8192;
//                buf1.clear();
//                buf2.clear();
//
//                numRead1 = channel1.read(buf1);
//                numRead2 = channel2.read(buf2);

                buf1.flip();
                buf2.flip();

                if(numRead1 > 0 && numRead2 > 0)
                {
                    if(numRead1 <= numRead2)
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
                    }
                    else
                    {
                        while(buf2.hasRemaining())
                        {
                            total++;
                            byte b1 = buf1.get();
                            byte b2 = buf2.get();
                            if(b1 == b2)
                            {
                                same++;
                            }
                        }
                    }
                }

                buf1.clear();
                buf2.clear();
            }
            while(numRead1 > 0 && numRead2 > 0 && same < total);
//            while(numRead1 > 0 && numRead1 == numRead2);

            System.out.println(i + ", " + numRead1 + ", " + numRead2 + ", " + total + ", " + same + ", " + (double)same/total);
        }
    }
}
