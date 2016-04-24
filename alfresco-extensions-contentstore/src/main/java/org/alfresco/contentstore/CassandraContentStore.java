/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.alfresco.contentstore.patch.PatchService;
import org.sglover.alfrescoextensions.common.CassandraSession;
import org.sglover.alfrescoextensions.common.MimeType;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.ChecksumService;
import org.sglover.checksum.Patch;
import org.sglover.checksum.PatchDocument;
import org.sglover.entities.EntitiesService;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * 
 * @author sglover
 *
 */
public class CassandraContentStore extends AbstractContentStore
{
    private final CassandraSession cassandraSession;
    private final PreparedStatement getBlockStatement;
    private final PreparedStatement getNodeMetadataStatement;
    private final PreparedStatement getNodeStatement;
    private final PreparedStatement writeBlockStatement;
    private final PreparedStatement writePatchStatement;
    private final PreparedStatement getPatchesStatement;
    private final PreparedStatement writeMatchedBlockStatement;
    private final PreparedStatement getMatchedBlocksStatement;
    private final Charset charset = Charset.forName("UTF-8");

    public CassandraContentStore(CassandraSession cassandraSession, ChecksumService checksumService,
            PatchService patchService, String contentRoot, NodeUsageDAO nodeUsageDAO,
            EntitiesService entitiesService, boolean async) throws IOException
    {
        super(contentRoot, checksumService, patchService, nodeUsageDAO, entitiesService, async);
        this.cassandraSession = cassandraSession;

        createSchema();

        String keyspace = cassandraSession.getKeyspace();

        this.getBlockStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM " + keyspace + ".content_data where nodeId = ? and nodeVersion = ? and mimetype = ? and rangeId = ?");
        this.getNodeStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM " + keyspace + ".content where nodeId = ? and nodeVersion = ? and mimetype = ?");
        this.getNodeMetadataStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM " + keyspace + ".content_metadata where nodeId = ? and nodeVersion = ? and mimeType = ?");
        this.writeBlockStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".content_data (nodeId, nodeVersion, mimetype, rangeId, data) VALUES(?, ?, ?, ?, ?);");
        this.writePatchStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".patch_data (nodeId, nodeVersion, mimeType, num, last_match_index, size, patch) VALUES (?, ?, ?, ?, ?, ?, ?);");
        this.getPatchesStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT num, last_match_index, size, patch FROM " + keyspace + ".patch_data WHERE nodeId = ? AND nodeVersion = ? and mimeType = ?;");
        this.writeMatchedBlockStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".patch_matched_blocks (nodeId, nodeVersion, mimeType, num, matched_block) VALUES (?, ?, ?, ?, ?);");
        this.getMatchedBlocksStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT num, matched_block FROM " + keyspace + ".patch_matched_blocks WHERE nodeId = ? AND nodeVersion = ? and mimeType = ?;");
    }

    private class CassandraPatchDocument implements PatchDocument
    {
        private boolean isBatch;
        private int blockSize;
        private Node node;
        private BatchStatement batch;
        private int numMatchedBlocks = 0;
        private int numPatches = 0;
        private int matchCount;

        CassandraPatchDocument(Node node, int blockSize, boolean isBatch)
        {
            this.node = node;
            this.blockSize = blockSize;
            this.isBatch = isBatch;
            if(isBatch)
            {
                this.batch = new BatchStatement();
            }
        }

        @Override
        public void addPatch(Patch patch)
        {
            ByteBuffer bb = ByteBuffer.wrap(patch.getBuffer());
            BoundStatement st = writePatchStatement.bind(node.getNodeId(), node.getNodeVersion(),
                    node.getMimeType().getMimetype(), numPatches++, patch.getLastMatchIndex(), 
                    patch.getSize(), bb);

            if(isBatch)
            {
                batch.add(st);
            }
            else
            {
                cassandraSession.getCassandraSession().execute(st);
            }
        }

        @Override
        public void addMatchedBlock(int matchedBlock)
        {
            BoundStatement st = writeMatchedBlockStatement.bind(node.getNodeId(),
                    node.getNodeVersion(), node.getMimeType().getMimetype(), numMatchedBlocks++, matchedBlock);

            if(isBatch)
            {
                batch.add(st);
            }
            else
            {
                cassandraSession.getCassandraSession().execute(st);
            }
        }

        @Override
        public void setBlockSize(int blockSize)
        {
            this.blockSize = blockSize;
        }

        @Override
        public int getBlockSize()
        {
            return blockSize;
        }

        @Override
        public List<Integer> getMatchedBlocks()
        {
            List<Integer> matchedBlocks = new LinkedList<>();

            BoundStatement st = getMatchedBlocksStatement.bind(node.getNodeId(),
                    node.getNodeVersion(), node.getMimeType().getMimetype());
            
            ResultSet rs = cassandraSession.getCassandraSession().execute(st);
            for(Row row : rs)
            {
                matchedBlocks.add(row.getInt("matched_block"));
            }

            this.matchCount = matchedBlocks.size();

            return matchedBlocks;
        }

        @Override
        public List<Patch> getPatches()
        {
            List<Patch> patches = new LinkedList<>();

            BoundStatement st = getPatchesStatement.bind(node.getNodeId(),
                    node.getNodeVersion(), node.getMimeType().getMimetype());

            ResultSet rs = cassandraSession.getCassandraSession().execute(st);
            for(Row row : rs)
            {
                int lastMatchIndex = row.getInt("last_match_index");
                int size = row.getInt("size");
                byte[] bytes = new byte[size];
                row.getBytes("patch").get(bytes);
                Patch patch = new Patch(lastMatchIndex, size, bytes);
                patches.add(patch);
            }

            return patches;
        }

        @Override
        public void commit()
        {
            if(isBatch)
            {
                cassandraSession.getCassandraSession().execute(batch);
            }
        }

        @Override
        public int getMatchCount()
        {
            return matchCount;
        }

        @Override
        public Node getNode()
        {
            return node;
        }
    }

    private void createSchema()
    {
        String keyspace = cassandraSession.getKeyspace();

        KeyspaceMetadata keySpaceMetadata = cassandraSession.getCluster().getMetadata()
                .getKeyspace(keyspace);
        if(keySpaceMetadata == null)
        {
            throw new RuntimeException("No " + keyspace + " keyspace");
        }
        else
        {
            if(keySpaceMetadata.getTable("content_metadata") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".content_metadata (nodeId text, nodeVersion bigint, "
                        + "mimeType text, "
                        + "PRIMARY KEY((nodeId, nodeVersion, mimeType)));");
            }
            if(keySpaceMetadata.getTable("content") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".content (nodeId text, nodeVersion bigint, "
                        + "mimetype text, path text, num_blocks int, size bigint, block_size int, "
                        + "PRIMARY KEY((nodeId, nodeVersion, mimetype)));");
            }
            if(keySpaceMetadata.getTable("content_data") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".content_data (nodeId text, "
                        + "nodeVersion bigint, mimetype text, rangeId bigint, data blob, "
                        + "PRIMARY KEY((nodeId, nodeVersion, mimetype, rangeId)));");
            }
            if(keySpaceMetadata.getTable("patch_data") == null)
            {
                ResultSet rs = cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".patch_data (nodeId text, "
                        + "nodeVersion bigint, mimeType text, num int, last_match_index int, size int, patch blob, "
                        + "PRIMARY KEY((nodeId, nodeVersion, mimetype), num));");
                System.out.println(rs);
            }
            if(keySpaceMetadata.getTable("patch_matched_blocks") == null)
            {
                ResultSet rs = cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".patch_matched_blocks (nodeId text, "
                        + "nodeVersion bigint, mimeType text, num int, matched_block int, "
                        + "PRIMARY KEY((nodeId, nodeVersion, mimetype), num));");
                System.out.println(rs);
            }
        }
    }

    private class CassandraReadingByteChannel implements SeekableByteChannel
    {
        private final Node node;
        private final int numBlocks;
        private final int blockSize;
        private final AtomicBoolean isOpen = new AtomicBoolean(true);
        private final AtomicLong position = new AtomicLong(0);
        private final AtomicLong size = new AtomicLong(0);

        public CassandraReadingByteChannel(Node node)
        {
            this.node = node;

            NodeMetadata nodeMetadata = getNodeMetadata(node);

            ResultSet rs = cassandraSession.getCassandraSession()
                    .execute(getNodeStatement.bind(node.getNodeId(), node.getNodeVersion(),
                            nodeMetadata.getMimeType().getMimetype()));
            Row row = rs.one();
            if(row == null)
            {
                throw new IllegalArgumentException("No such node " + node);
            }
            this.blockSize = row.getInt("block_size");
            this.numBlocks = row.getInt("num_blocks");
            this.size.getAndSet(row.getLong("size"));
//            this.mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimetype"));
        }

        @Override
        public boolean isOpen()
        {
            return isOpen.get();
        }

        @Override
        public void close() throws IOException
        {
            this.isOpen.getAndSet(false);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            long pos = position();
            long size = Math.min(this.size.get(), dst.remaining());

            int remaining = dst.remaining();
            long numBytes = (pos + remaining > this.size.get() ? size - pos : remaining);
            long numBlocks = numBytes / blockSize + (numBytes % blockSize > 0 ? 1 : 0);
            long rangeStart = pos / blockSize;
            long rangeEnd = rangeStart + numBlocks - 1;

            int numRead = -1;
            if(rangeEnd >= rangeStart)
            {
                numRead = 0;
                for (long i = rangeStart; i <= rangeEnd; i++)
                {
                    ByteBuffer bb = null;
                    if(dst.remaining() > blockSize)
                    {
                        bb = getBlock(node, i);
                    }
                    else
                    {
                        bb = getBlock(node, i, dst.remaining());
                    }
    
                    numRead += bb.remaining();
    
                    dst.put(bb);
                }
                
                position.addAndGet(numRead);
            }

            return numRead;
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public long position() throws IOException
        {
            return position.get();
        }

        @Override
        public SeekableByteChannel position(long newPosition)
                throws IOException
        {
            position.getAndSet(newPosition);
            return this;
        }

        @Override
        public long size() throws IOException
        {
            return size.get();
        }

        @Override
        public SeekableByteChannel truncate(long size) throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private class CassandraWritableByteChannel implements WritableByteChannel
    {
        private final Node node;
        private final ByteBuffer bb;
        private final AtomicBoolean isOpen = new AtomicBoolean(true);
        private int currentRangeId = 0;
        private long size = 0;
        private int numBlocks = 0;

        public CassandraWritableByteChannel(Node node)
        {
            this.node = node;
            this.bb = ByteBuffer.allocate(getBlockSize());
        }

        @Override
        public boolean isOpen()
        {
            return isOpen.get();
        }

        @Override
        public void close() throws IOException
        {
            bb.flip();
            writeBlock(node, currentRangeId, bb);
            numBlocks++;
            currentRangeId++;
            bb.clear();

            this.isOpen.getAndSet(false);

            writeNodeMetadata(node);
            writeNodeData(node, numBlocks, size);

            createPatch(node);
            extractChecksums(node);

            if(node.getMimeType().equals(MimeType.TEXT))
            {
                extractEntities(node);
            }
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            int numBytesWritten = 0;

            while(src.hasRemaining())
            {
                if(bb.remaining() == 0)
                {
                    bb.flip();
                    writeBlock(node, currentRangeId, bb);
                    numBlocks++;
                    currentRangeId++;
                    bb.clear();
                }

                byte b = src.get();
                bb.put(b);
                numBytesWritten++;
            }

//            if(bb.remaining() == 0)
//            {
//                bb.flip();
//                writeBlock(node, mimeType, currentRangeId, bb);
//                currentRangeId++;
//                bb.clear();
//            }

            size += numBytesWritten;

            return numBytesWritten;
        }
    }

    private ByteBuffer getBlock(Node node, long rangeId)
    {
        return getBlock(node, rangeId, getBlockSize());
    }

    @Override
    public InputStream getBlockAsInputStream(Node node, long rangeId, int size)
    {
        ByteBuffer bb = getBlock(node, rangeId, size);
        InputStream in = new ByteArrayInputStream(bb.array());
        return in;
    }

    /*
     * Get up to size bytes from the block storage in Cassandra
     */
    private ByteBuffer getBlock(Node node, long rangeId, int size)
    {
        ByteBuffer bb = null;

        String nodeId = node.getNodeId();
        long nodeVersion = node.getNodeVersion();
        MimeType mimeType = node.getMimeType();

        ResultSet rs = cassandraSession.getCassandraSession()
                .execute(getBlockStatement.bind(nodeId, nodeVersion, mimeType.getMimetype(), rangeId));
        Row row = rs.one();
        if(row != null)
        {
            if(size == getBlockSize())
            {
                bb = row.getBytes("data");
                bb.compact();
                bb.flip();
            }
            else
            {
                ByteBuffer bb1 = row.getBytes("data");
                bb1.flip();
                bb = ByteBuffer.allocate(size);
                int i = 0;
                while(bb1.hasRemaining() && i++ < size)
                {
                    bb.put(bb1.get());
                }
                bb.flip();
            }
        }
        else
        {
            System.out.println("Unknown block node=" + node + ", rangeId=" + rangeId);
        }

        return bb;
    }

    private static class NodeMetadata
    {
        private String nodeId;
        private long nodeVersion;
        private MimeType mimeType;

        public NodeMetadata(String nodeId, long nodeVersion,
                MimeType mimeType)
        {
            super();
            this.nodeId = nodeId;
            this.nodeVersion = nodeVersion;
            this.mimeType = mimeType;
        }
        public String getNodeId()
        {
            return nodeId;
        }
        public long getNodeVersion()
        {
            return nodeVersion;
        }
        public MimeType getMimeType()
        {
            return mimeType;
        }

        
    }

    private NodeMetadata getNodeMetadata(Node node)
    {
        NodeMetadata nodeMetadata = null;

        String nodeId = node.getNodeId();
        long nodeVersion = node.getNodeVersion();

        ResultSet rs = cassandraSession.getCassandraSession()
                .execute(getNodeMetadataStatement.bind(nodeId, nodeVersion, node.getMimeType().getMimetype()));
        Row row = rs.one();
        if(row != null)
        {
            MimeType mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimeType"));
            nodeMetadata = new NodeMetadata(nodeId, nodeVersion, mimeType);
        }

        return nodeMetadata;
    }

    private CharBuffer getBlockAsCharBuffer(Node node, MimeType mimeType, long rangeId)
    {
        CharBuffer charBuffer = null;

        String nodeId = node.getNodeId();
        long nodeVersion = node.getNodeVersion();

        ResultSet rs = cassandraSession.getCassandraSession()
                .execute(getBlockStatement.bind(nodeId, nodeVersion, mimeType.getMimetype(), rangeId));
        Row row = rs.one();
        if(row != null)
        {
            ByteBuffer bb = row.getBytes("data");
            bb.flip();
            charBuffer = charset.decode(bb);
        }

        return charBuffer;
    }

    private void writeNodeMetadata(Node node)
    {
        StringBuilder sb = new StringBuilder("INSERT INTO " + cassandraSession.getKeyspace() + ".content_metadata (nodeId, nodeVersion, mimeType) ");
        sb.append(" VALUES('");
        sb.append(node.getNodeId());
        sb.append("', ");
        sb.append(node.getNodeVersion());
        sb.append(", '");
        sb.append(node.getMimeType().getMimetype());
        sb.append("')");

        cassandraSession.getCassandraSession().execute(sb.toString());
    }

    private void writeNodeData(Node node, int numBlocks, long size)
    {
        StringBuilder sb = new StringBuilder("INSERT INTO " + cassandraSession.getKeyspace() + ".content (nodeId, nodeVersion, mimetype, num_blocks, size, block_size) ");
        sb.append(" VALUES('");
        sb.append(node.getNodeId());
        sb.append("', ");
        sb.append(node.getNodeVersion());
        sb.append(", '");
        sb.append(node.getMimeType().getMimetype());
        sb.append("', ");
        sb.append(numBlocks);
        sb.append(", ");
        sb.append(size);
        sb.append(", ");
        sb.append(getBlockSize());
        sb.append(")");

        cassandraSession.getCassandraSession().execute(sb.toString());
    }

    private void writeBlock(Node node, long rangeId, ByteBuffer bb)
    {
        cassandraSession.getCassandraSession()
                .execute(writeBlockStatement.bind(node.getNodeId(), node.getNodeVersion(),
                        node.getMimeType().getMimetype(), rangeId, bb));
    }

    private class CassandraInputStream extends InputStream
    {
        private int currentRangeId = 0;
        private ByteBuffer bb = ByteBuffer.allocate(getBlockSize());
        private final Node node;
        private final MimeType mimeType;

        public CassandraInputStream(Node node)
        {
            this.node = node;

            NodeMetadata nodeMetadata = getNodeMetadata(node);

            ResultSet rs = cassandraSession.getCassandraSession()
                    .execute(getNodeStatement.bind(node.getNodeId(), node.getNodeVersion(),
                            nodeMetadata.getMimeType().getMimetype()));
            Row row = rs.one();
            if(row == null)
            {
                throw new IllegalArgumentException("No such node " + node);
            }
            this.mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimetype"));

            // get first block
            bb = getBlock(node, currentRangeId);
            currentRangeId++;
        }

        @Override
        public int read() throws IOException
        {
            int ret = -1;

            if(bb != null)
            {
                if(bb.remaining() == 0)
                {
                    bb = getBlock(node, currentRangeId);
                    currentRangeId++;
                }

                if(bb != null)
                {
                    ret = bb.get() & 0xFF;
                }
            }

            return ret;
        }
    }

    private class CassandraOutputStream extends OutputStream
    {
        private int currentRangeId = 0;
        private ByteBuffer bb = ByteBuffer.allocate(getBlockSize());
        private final Node node;
        private long size = 0;
        private int numBlocks = 0;

        public CassandraOutputStream(Node node)
        {
            this.node = node;
        }

        @Override
        public void write(int i) throws IOException
        {
            if(!bb.hasRemaining())
            {
                bb.flip();
                writeBlock(node, currentRangeId, bb);
                numBlocks++;
                currentRangeId++;
                bb.clear();
            }

            bb.put((byte)i);
            size++;
        }

        @Override
        public void close() throws IOException
        {
            bb.flip();
            writeBlock(node, currentRangeId, bb);
            numBlocks++;
            currentRangeId++;
            bb.clear();

            writeNodeMetadata(node);
            writeNodeData(node, numBlocks, size);

            extractChecksums(node);
            createPatch(node);

            if(node.getMimeType().equals(MimeType.TEXT))
            {
                extractEntities(node);
            }
        }
    }

    @Override
    protected PatchDocument getPatchDocument(Node node)
    {
        PatchDocument patchDocument = new CassandraPatchDocument(node, checksumService.getBlockSize(), false);
        return patchDocument;
    }

    private class CassandraWriter extends Writer
    {
        private int currentRangeId = 0;
        private ByteBuffer current;
        private final Node node;
        private long size = 0;
        private int numBlocks = 0;

        public CassandraWriter(Node node)
        {
            this.node = node;
            this.current = ByteBuffer.allocate(getBlockSize());
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException
        {
            CharBuffer charBuffer = CharBuffer.wrap(cbuf, off, len);
            ByteBuffer bb = charset.encode(charBuffer);
            while(bb.remaining() > 0)
            {
                byte b = bb.get();
                size++;
                if(!current.hasRemaining())
                {
                    writeBlock(node, currentRangeId, current);
                    numBlocks++;
                    currentRangeId++;
                    current.clear();
                }

                current.put(b);
            }
        }

        @Override
        public void flush() throws IOException
        {
            writeBlock(node, currentRangeId, current);
            currentRangeId++;
        }

        @Override
        public void close() throws IOException
        {
            writeNodeMetadata(node);
            writeNodeData(node, numBlocks, size);

            extractChecksums(node);
            createPatch(node);

            if(node.getMimeType().equals(MimeType.TEXT))
            {
                extractEntities(node);
            }
        }
    }

    private class CassandraReader extends Reader
    {
        private int currentRangeId = 0;
        private CharBuffer charBuffer = CharBuffer.allocate(getBlockSize());
        private final Node node;

        public CassandraReader(Node node)
        {
            this.node = node;

            NodeMetadata nodeMetadata = getNodeMetadata(node);

            ResultSet rs = cassandraSession.getCassandraSession()
                    .execute(getNodeStatement.bind(node.getNodeId(), node.getNodeVersion(),
                            nodeMetadata.getMimeType().getMimetype()));
            Row row = rs.one();
            if(row == null)
            {
                throw new IllegalArgumentException("No such node " + node);
            }
//            this.mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimetype")); 
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
            int total = 0;

            do
            {
                int remaining = charBuffer.remaining();
                if(remaining == 0)
                {
                    currentRangeId++;
                    ByteBuffer bb = getBlock(node, currentRangeId);
                    charBuffer = charset.decode(bb);
                    remaining = charBuffer.remaining();
                    if(remaining == 0)
                    {
                        total = -1;
                        break;
                    }
                }
                total += remaining;
                charBuffer.get(cbuf, 0, remaining);
            }
            while(total < cbuf.length);

            return total;
        }

        @Override
        public void close() throws IOException
        {
            // TODO Auto-generated method stub
            
        }
    }

    @Override
    protected ContentReader getReaderImpl(Node node) throws IOException
    {
        ContentReader ret = new CassandraContentReader(node);
        return ret;
    }

    @Override
    protected ContentReader getReaderImpl(Node node, MimeType mimeType) throws IOException
    {
        ContentReader ret = new CassandraContentReader(node);
        return ret;
    }

    @Override
    public ReadableByteChannel getChannel(Node node) throws IOException
    {
        ContentReader reader = getReaderImpl(node);
        return (reader != null ? reader.getChannel() : null);
    }

    @SuppressWarnings("unused")
    @Override
    public Node applyPatch(Node node, PatchDocument patchDocument) throws IOException
    {
        int blockSize = patchDocument.getBlockSize();

        NodeMetadata nodeMetadata = getNodeMetadata(node);
        Node newNode = Node.build().nodeId(node.getNodeId()).nodeVersion(node.getNodeVersion() + 1)
                .mimeType(nodeMetadata.getMimeType());

        try(ReadableByteChannel inChannel = new CassandraReadingByteChannel(node);
                WritableByteChannel outChannel = new CassandraWritableByteChannel(newNode))
        {
            applyPatch(inChannel, outChannel, patchDocument);
            return newNode;
        }
    }

    private class CassandraContentReader extends AbstractContentReader
    {
//        private MimeType mimeType;
        private int numBlocks;
        private long size;

        CassandraContentReader(Node node)
        {
            super(node);
            init();
        }

//        CassandraContentReader(Node node, MimeType mimeType)
//        {
//            super(node);
//            init(mimeType);
//        }

        private void init()
        {
            NodeMetadata nodeMetadata = getNodeMetadata(node);

            ResultSet rs = cassandraSession.getCassandraSession()
                    .execute(getNodeStatement.bind(node.getNodeId(), node.getNodeVersion(),
                            nodeMetadata.getMimeType().getMimetype()));
            Row row = rs.one();
            if(row == null)
            {
                throw new IllegalArgumentException("No such node " + node);
            }
//            this.mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimeType"));
            this.numBlocks = row.getInt("num_blocks");
            this.size = row.getLong("size");
        }

        @Override
        public ReadableByteChannel getChannel() throws IOException
        {
            ReadableByteChannel ret = numBlocks > 0 ? new CassandraReadingByteChannel(node) : null;
            return ret;
        }

        @Override
        public Long getSize()
        {
            return size;
        }

        @Override
        public ContentStore getStore()
        {
            return CassandraContentStore.this;
        }

        @Override
        public MimeType getMimeType()
        {
            return node.getMimeType();
        }

        @Override
        public InputStream getStream() throws IOException
        {
            InputStream in = new CassandraInputStream(node);
            return in;
        }

        @Override
        public Reader getReader() throws IOException
        {
            Reader reader = new CassandraReader(node);
            return reader;
        }
    }

    private class CassandraContentWriter extends AbstractContentWriter
    {
        CassandraContentWriter(Node node)
        {
            super(node);
        }

        public OutputStream getOutputStream() throws IOException
        {
            OutputStream out = new CassandraOutputStream(node);
            return out;
        }

        public Writer getWriter() throws IOException
        {
            Writer out = new CassandraWriter(node);
            return out;
        }

        public void writeStream(InputStream stream) throws IOException
        {
            
        }

        @Override
        public ContentStore getStore()
        {
            return CassandraContentStore.this;
        }
    }

    @Override
    public boolean exists(Node node)
    {
        final Statement statement = QueryBuilder
                .select()
                .from(cassandraSession.getKeyspace(), "content")
                .where(eq("nodeId", node.getNodeId()))
                .and(eq("nodeVersion", node.getNodeVersion()))
                .and(eq("mimeType", node.getMimeType().getMimetype()));
        ResultSet resultSet = cassandraSession.getCassandraSession().execute(statement);
        Row row = resultSet.one();
        return row != null;
    }

    @Override
    protected ContentWriter getWriterImpl(Node node)
    {
        ContentWriter ret = new CassandraContentWriter(node);
        return ret;
    }
}
