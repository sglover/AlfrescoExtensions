/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.PatchDocument;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
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
    private CassandraSession cassandraSession;

    public CassandraContentStore(CassandraSession cassandraSession, String contentRoot,
            ChecksumService checksumService) throws IOException
    {
        super(contentRoot, checksumService);
        this.cassandraSession = cassandraSession;
        createSchema();
    }

    public CassandraContentStore(CassandraSession cassandraSession, ChecksumService checksumService) throws IOException
    {
        super(checksumService);
        this.cassandraSession = cassandraSession;
        createSchema();
    }

    public CassandraContentStore(CassandraSession cassandraSession, File rootDirectory, ChecksumService checksumService)
    {
        super(rootDirectory, checksumService);
        this.cassandraSession = cassandraSession;
        createSchema();
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
            if(keySpaceMetadata.getTable("content") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE alfresco.content (nodeId text, path text, "
                        + "num_blocks int, size bigint, block_size int, "
                        + "PRIMARY KEY(nodeId));");
            }
            if(keySpaceMetadata.getTable("content_data") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE alfresco.content_data (path text, "
                        + "rangeId int, data blob, "
                        + "PRIMARY KEY((nodeId, rangeId)));");
            }
        }
    }

    private class CassandraReadingByteChannel implements SeekableByteChannel
    {
        private final String nodeId;
        private final int numBlocks;
        private final int blockSize;
        private final AtomicBoolean isOpen = new AtomicBoolean(true);
        private final AtomicLong position = new AtomicLong(0);
        private final AtomicLong size = new AtomicLong(0);

        public CassandraReadingByteChannel(String nodeId)
        {
            this.nodeId = nodeId;
            final Statement statement = QueryBuilder
                    .select()
                    .from("alfresco", "content")
                    .where(eq("nodeId", nodeId));
            ResultSet resultSet = cassandraSession.getCassandraSession().execute(statement);
            Row row = resultSet.one();
            this.blockSize = row.getInt("block_size");
            this.numBlocks = row.getInt("num_blocks");
            this.size.getAndSet(row.getLong("size"));
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
            long size = this.size.get();

            int remaining = dst.remaining();
            long numBytes = (pos + remaining > size ? size - pos : remaining);
            long numBlocks = numBytes / blockSize + (numBytes % blockSize > 0 ? 1 : 0);
            long rangeStart = pos / blockSize;
            long rangeEnd = rangeStart + numBlocks;

            PreparedStatement statement1 = cassandraSession.getCassandraSession().prepare(
                    "SELECT * FROM alfresco.content_data where nodeId = ? and rangeId = ?");
            List<ResultSetFuture> futures = new ArrayList<>();
            for (long i = rangeStart; i <= rangeEnd; i++)
            {
                ResultSetFuture resultSetFuture = cassandraSession.getCassandraSession().executeAsync(statement1.bind(nodeId, i));
                futures.add(resultSetFuture);
            }

            int numRead = 0;
            for (ResultSetFuture future : futures)
            {
                ResultSet rs = future.getUninterruptibly();
                for(Row row1 : rs.all())
                {
                    // TODO can we rely on the ordering of the futures?
                    ByteBuffer bb = row1.getBytes("data");
                    dst.put(bb);
                    numRead += bb.limit();
                }
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

    @Override
    public SeekableByteChannel getContent(String path) throws IOException
    {
        SeekableByteChannel ret = null;

        {
            final Statement statement = QueryBuilder
                    .select()
                    .from("alfresco", "content")
                    .where(eq("path", path));
            ResultSet resultSet = cassandraSession.getCassandraSession().execute(statement);
            Row row = resultSet.one();
            int numBlocks = row.getInt("num_blocks");
            if(numBlocks > 0)
            {
                CassandraReadingByteChannel bc = new CassandraReadingByteChannel(path);
                ret = bc;
//                PreparedStatement statement1 = cassandraSession.getCassandraSession().prepare(
//                        "SELECT * FROM alfresco.content_data where nodeId = ? and rangeId = ?");
//                List<ResultSetFuture> futures = new ArrayList<>();
//                for (int i = 1; i < numBlocks; i++)
//                {
//                    ResultSetFuture resultSetFuture = cassandraSession.getCassandraSession().executeAsync(statement1.bind(nodeId, i));
//                    futures.add(resultSetFuture);
//                }
//
//                for (ResultSetFuture future : futures)
//                {
//                    ResultSet rs = future.getUninterruptibly();
//                    for(Row row1 : rs.all())
//                    {
//                        ByteBuffer bb = row1.getBytes("data");
//                    }
//                }

//                final Statement statement1 = QueryBuilder
//                        .select()
//                        .from("alfresco", "content_data")
//                        .where(eq("nodeId", nodeId));
//                final ResultSet resultSet1 = cassandraSession.getCassandraSession().execute(statement1);
//                final Spliterator<Row> spliterator = resultSet1.spliterator();
//                Stream<Row> stream = StreamSupport.stream(spliterator, true)
////                        .onClose(() -> resultSet1.)  // need to close cursor;
//                        .map(row -> row.getBytes("data"));
////                        .filter(pi -> pi != null);
//                for(Row row1 : resultSet.all())
//                {
//                    ByteBuffer bb = row1.getBytes("data");
//                }
            }
        }

        return ret;
    }

    @Override
    public String applyPatch(PatchDocument patchDocument,
            String existingContentPath) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
