/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.PatchDocument;
import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.Node;

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
    private final PreparedStatement getNodeMetadataWithMimeTypeStatement;
    private final PreparedStatement getNodeStatement;
    private final PreparedStatement writeBlockStatement;
    private final Charset charset = Charset.forName("UTF-8");

    public CassandraContentStore(CassandraSession cassandraSession, String contentRoot,
            ChecksumService checksumService, NodeUsageDAO nodeUsageDAO, int blockSize) throws IOException
    {
        super(contentRoot, checksumService, nodeUsageDAO, blockSize);
        this.cassandraSession = cassandraSession;
        createSchema();

        this.getBlockStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM alfresco.content_data where nodeId = ? and nodeVersion = ? and mimetype = ? and rangeId = ?");
        this.getNodeStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM alfresco.content where nodeId = ? and nodeVersion = ? and mimetype = ?");
        this.getNodeMetadataStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM alfresco.content_metadata where nodeId = ? and nodeVersion = ?");
        this.getNodeMetadataWithMimeTypeStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM alfresco.content_metadata where nodeId = ? and nodeVersion = ? and mimeType = ?");
        this.writeBlockStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO alfresco.content_data (nodeId, nodeVersion, mimetype, rangeId, data) VALUES(?, ?, ?, ?, ?);");
    }

    private void createSchema()
    {
        KeyspaceMetadata keySpaceMetadata = cassandraSession.getCluster().getMetadata().getKeyspace("alfresco");
        if(keySpaceMetadata == null)
        {
            throw new RuntimeException("No alfresco keyspace");
        }
        else
        {
            if(keySpaceMetadata.getTable("content_metadata") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE alfresco.content_metadata (nodeId text, nodeVersion bigint, "
                        + "mimeType text, "
                        + "PRIMARY KEY((nodeId, nodeVersion)));");
            }
            if(keySpaceMetadata.getTable("content") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE alfresco.content (nodeId text, nodeVersion bigint, "
                        + "mimetype text, path text, num_blocks int, size bigint, block_size int, "
                        + "PRIMARY KEY((nodeId, nodeVersion, mimetype)));");
            }
            if(keySpaceMetadata.getTable("content_data") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE alfresco.content_data (nodeId text, "
                        + "nodeVersion bigint, mimetype text, rangeId bigint, data blob, "
                        + "PRIMARY KEY((nodeId, nodeVersion, mimetype, rangeId)));");
            }
        }
    }

    private class CassandraReadingByteChannel implements SeekableByteChannel
    {
        private final Node node;
        private final int numBlocks;
        private final int blockSize;
        private final MimeType mimeType;
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
            this.mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimetype"));
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
            long numBytes = (pos + remaining > size ? size - pos : remaining);
            long numBlocks = numBytes / blockSize + (numBytes % blockSize > 0 ? 1 : 0);
            long rangeStart = pos / blockSize;
            long rangeEnd = rangeStart + numBlocks - 1;

            int numRead = 0;
            for (long i = rangeStart; i <= rangeEnd; i++)
            {
                ByteBuffer bb = null;
                if(dst.remaining() > blockSize)
                {
                    bb = getBlock(node, mimeType, i);
                }
                else
                {
                    bb = getBlock(node, mimeType, i, dst.remaining());
                }

                numRead += bb.remaining();

                dst.put(bb);
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
        private final MimeType mimeType;
        private final ByteBuffer bb;
        private final AtomicBoolean isOpen = new AtomicBoolean(true);
        private int currentRangeId = 0;
        private long size = 0;
        private int numBlocks = 0;

        public CassandraWritableByteChannel(Node node, MimeType mimeType)
        {
            this.node = node;
            this.mimeType = mimeType;
            this.bb = ByteBuffer.allocate(blockSize);
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
            writeBlock(node, mimeType, currentRangeId, bb);
            numBlocks++;
            currentRangeId++;
            bb.clear();

            writeNodeData(node, mimeType, numBlocks, size);
            writeNodeMetadata(node, mimeType);

            this.isOpen.getAndSet(false);
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
                    writeBlock(node, mimeType, currentRangeId, bb);
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

    private ByteBuffer getBlock(Node node, MimeType mimeType, long rangeId)
    {
        return getBlock(node, mimeType, rangeId, blockSize);
    }

    private ByteBuffer getBlock(Node node, MimeType mimeType, long rangeId, int size)
    {
        ByteBuffer bb = null;

        String nodeId = node.getNodeId();
        long nodeVersion = node.getNodeVersion();

        ResultSet rs = cassandraSession.getCassandraSession()
                .execute(getBlockStatement.bind(nodeId, nodeVersion, mimeType.getMimetype(), rangeId));
        Row row = rs.one();
        if(row != null)
        {
            if(size == blockSize)
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
                for(int i = 0; i < size; i++)
                {
                    bb.put(bb1.get());
                }
                bb.flip();
            }
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
                .execute(getNodeMetadataStatement.bind(nodeId, nodeVersion));
        Row row = rs.one();
        if(row != null)
        {
            MimeType mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimeType"));
            nodeMetadata = new NodeMetadata(nodeId, nodeVersion, mimeType);
        }

        return nodeMetadata;
    }

    private NodeMetadata getNodeMetadata(Node node, MimeType mimeType)
    {
        NodeMetadata nodeMetadata = null;

        String nodeId = node.getNodeId();
        long nodeVersion = node.getNodeVersion();

        ResultSet rs = cassandraSession.getCassandraSession()
                .execute(getNodeMetadataWithMimeTypeStatement.bind(nodeId, nodeVersion, mimeType));
        Row row = rs.one();
        if(row != null)
        {
            MimeType mt = MimeType.INSTANCES.getByMimetype(row.getString("mimeType"));
            nodeMetadata = new NodeMetadata(nodeId, nodeVersion, mt);
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

    private void writeNodeMetadata(Node node, MimeType mimeType)
    {
        StringBuilder sb = new StringBuilder("INSERT INTO alfresco.content_metadata (nodeId, nodeVersion, mimeType) ");
        sb.append(" VALUES('");
        sb.append(node.getNodeId());
        sb.append("', ");
        sb.append(node.getNodeVersion());
        sb.append(", '");
        sb.append(mimeType.getMimetype());
        sb.append("')");

        cassandraSession.getCassandraSession().execute(sb.toString());
    }

    private void writeNodeData(Node node, MimeType mimeType, int numBlocks, long size)
    {
        StringBuilder sb = new StringBuilder("INSERT INTO alfresco.content (nodeId, nodeVersion, mimetype, num_blocks, size, block_size) ");
        sb.append(" VALUES('");
        sb.append(node.getNodeId());
        sb.append("', ");
        sb.append(node.getNodeVersion());
        sb.append(", '");
        sb.append(mimeType.getMimetype());
        sb.append("', ");
        sb.append(numBlocks);
        sb.append(", ");
        sb.append(size);
        sb.append(", ");
        sb.append(blockSize);
        sb.append(")");

        cassandraSession.getCassandraSession().execute(sb.toString());
    }

    private void writeBlock(Node node, MimeType mimeType, long rangeId, ByteBuffer bb)
    {
        cassandraSession.getCassandraSession()
                .execute(writeBlockStatement.bind(node.getNodeId(), node.getNodeVersion(), mimeType.getMimetype(),
                        rangeId, bb));
    }

    private class CassandraInputStream extends InputStream
    {
        private int currentRangeId = 0;
        private ByteBuffer bb = ByteBuffer.allocate(CassandraContentStore.this.blockSize);
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
            bb = getBlock(node, mimeType, currentRangeId);
            currentRangeId++;
        }

        @Override
        public int read() throws IOException
        {
            if(bb.remaining() == 0)
            {
                bb = getBlock(node, mimeType, currentRangeId);
                currentRangeId++;
            }

            int i = bb != null ?  bb.get() : -1;
            return i;
        }
    }

    private class CassandraOutputStream extends OutputStream
    {
        private int currentRangeId = 0;
        private ByteBuffer bb = ByteBuffer.allocate(CassandraContentStore.this.blockSize);
        private final Node node;
        private final MimeType mimeType;
        private long size = 0;
        private int numBlocks = 0;

        public CassandraOutputStream(Node node, MimeType mimeType)
        {
            this.node = node;
            this.mimeType = mimeType;
        }

        @Override
        public void write(int i) throws IOException
        {
            if(!bb.hasRemaining())
            {
                bb.flip();
                writeBlock(node, mimeType, currentRangeId, bb);
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
            writeBlock(node, mimeType, currentRangeId, bb);
            numBlocks++;
            currentRangeId++;
            bb.clear();

            CassandraContentStore.this.writeNodeMetadata(node, mimeType);
            CassandraContentStore.this.writeNodeData(node, mimeType, numBlocks, size);
        }
    }

    private class CassandraWriter extends Writer
    {
        private int currentRangeId = 0;
        private ByteBuffer current;
        private final Node node;
        private final MimeType mimeType;
        private long size = 0;
        private int numBlocks = 0;

        public CassandraWriter(Node node, MimeType mimeType)
        {
            this.node = node;
            this.mimeType = mimeType;
            this.current = ByteBuffer.allocate(CassandraContentStore.this.blockSize);
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
                    writeBlock(node, mimeType, currentRangeId, current);
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
            writeBlock(node, mimeType, currentRangeId, current);
            currentRangeId++;
        }

        @Override
        public void close() throws IOException
        {
            CassandraContentStore.this.writeNodeMetadata(node, mimeType);
            CassandraContentStore.this.writeNodeData(node, mimeType, numBlocks, size);
        }
    }

    private class CassandraReader extends Reader
    {
        private int currentRangeId = 0;
        private CharBuffer charBuffer = CharBuffer.allocate(CassandraContentStore.this.blockSize);
        private final Node node;
        private final MimeType mimeType;

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
            this.mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimetype")); 
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
                    ByteBuffer bb = getBlock(node, mimeType, currentRangeId);
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
        ContentReader ret = new CassandraContentReader(node, mimeType);
        return ret;
    }

    @Override
    public ReadableByteChannel getChannel(Node node) throws IOException
    {
        ContentReader reader = getReaderImpl(node);
        return (reader != null ? reader.getChannel() : null);
    }

    @Override
    public ReadableByteChannel getChannel(Node node, MimeType mimeType) throws IOException
    {
        ContentReader reader = getReaderImpl(node, mimeType);
        return (reader != null ? reader.getChannel() : null);
    }

    @SuppressWarnings("unused")
    @Override
    public Node applyPatch(String nodeId, long nodeVersion, PatchDocument patchDocument) throws IOException
    {
        int blockSize = patchDocument.getBlockSize();

        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
        Node newNode = Node.build().nodeId(nodeId).nodeVersion(nodeVersion + 1);
        NodeMetadata nodeMetadata = getNodeMetadata(node);

        try(ReadableByteChannel inChannel = new CassandraReadingByteChannel(node);
                WritableByteChannel outChannel = new CassandraWritableByteChannel(newNode, nodeMetadata.getMimeType()))
        {
            applyPatch(inChannel, outChannel, patchDocument);
            return newNode;
        }
    }

    private class CassandraContentReader extends AbstractContentReader
    {
        private MimeType mimeType;
        private int numBlocks;
        private long size;

        CassandraContentReader(Node node)
        {
            super(node);
            init(null);
        }

        CassandraContentReader(Node node, MimeType mimeType)
        {
            super(node);
            init(mimeType);
        }

        private void init(MimeType mimeType)
        {
            NodeMetadata nodeMetadata = (mimeType == null ? getNodeMetadata(node) :
                getNodeMetadata(node, mimeType));

            ResultSet rs = cassandraSession.getCassandraSession()
                    .execute(getNodeStatement.bind(node.getNodeId(), node.getNodeVersion(),
                            nodeMetadata.getMimeType().getMimetype()));
            Row row = rs.one();
            if(row == null)
            {
                throw new IllegalArgumentException("No such node " + node);
            }
            this.mimeType = MimeType.INSTANCES.getByMimetype(row.getString("mimeType"));
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
            return mimeType;
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
        private MimeType mimeType;

        CassandraContentWriter(Node node)
        {
            super(node);
        }

        CassandraContentWriter(Node node, MimeType mimeType)
        {
            super(node);
            this.mimeType = mimeType;
        }

        public OutputStream getOutputStream() throws IOException
        {
            OutputStream out = new CassandraOutputStream(node, mimeType);
            return out;
        }

        public Writer getWriter() throws IOException
        {
            Writer out = new CassandraWriter(node, mimeType);
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
    public boolean exists(String nodeId, long nodeVersion)
    {
        final Statement statement = QueryBuilder
                .select()
                .from("alfresco", "content")
                .where(eq("nodeId", nodeId))
                .and(eq("nodeVersion", nodeVersion));
        ResultSet resultSet = cassandraSession.getCassandraSession().execute(statement);
        Row row = resultSet.one();
        return row != null;
    }

    @Override
    public boolean exists(String nodeId, long nodeVersion, MimeType mimeType)
    {
        final Statement statement = QueryBuilder
                .select()
                .from("alfresco", "content")
                .where(eq("nodeId", nodeId))
                .and(eq("nodeVersion", nodeVersion))
                .and(eq("mimeType", mimeType.getMimetype()));
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

    @Override
    protected ContentWriter getWriterImpl(Node node, MimeType mimeType)
    {
        ContentWriter ret = new CassandraContentWriter(node, mimeType);
        return ret;
    }
}
