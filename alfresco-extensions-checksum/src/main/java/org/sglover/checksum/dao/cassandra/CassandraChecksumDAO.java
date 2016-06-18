/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.checksum.dao.cassandra;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.alfrescoextensions.common.CassandraSession;
import org.sglover.checksum.Checksum;
import org.sglover.checksum.NodeChecksums;
import org.sglover.checksum.dao.ChecksumDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * 
 * @author sglover
 *
 */
@Component
public class CassandraChecksumDAO implements ChecksumDAO
{
    protected static Log logger = LogFactory.getLog(CassandraChecksumDAO.class);

    @Autowired
    private CassandraSession cassandraSession;

    private Session session;
    private PreparedStatement insertChecksumStatement;
    private PreparedStatement insertChecksumsMetadataStatement;
    private PreparedStatement getChecksumsStatement;
    private PreparedStatement getChecksumsMetadataStatement;

    public CassandraChecksumDAO(CassandraSession cassandraSession)
    {
        this.cassandraSession = cassandraSession;
        init();
    }

    public CassandraChecksumDAO()
    {
    }

    @PostConstruct
    public void init()
    {
        this.session = cassandraSession.getCassandraSession();

        createSchema();

        String keyspace = cassandraSession.getKeyspace();

        this.getChecksumsStatement = session.prepare(
                "SELECT * FROM " + keyspace + ".checksums where node_id = ? and node_version = ?");
        this.getChecksumsMetadataStatement = session.prepare(
                "SELECT * FROM " + keyspace + ".checksums_metadata where node_id = ? and node_version = ?");
        this.insertChecksumsMetadataStatement = session.prepare(
                "INSERT INTO " + keyspace + ".checksums_metadata (node_id, node_version, version_label, block_size, num_blocks) VALUES (?, ?, ?, ?, ?)");
        this.insertChecksumStatement = session.prepare(
                "INSERT INTO " + keyspace + ".checksums (node_id, node_version, block_idx, hash, adler32, md5, start, end) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
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
            if(keySpaceMetadata.getTable("checksums_metadata") == null)
            {
                session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".checksums_metadata (node_id text, node_version bigint, "
                        + "version_label text, block_size int, num_blocks bigint, "
                        + "PRIMARY KEY((node_id, node_version)));");
            }
            if(keySpaceMetadata.getTable("checksums") == null)
            {
                session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".checksums (node_id text, node_version bigint, "
                        + "block_idx int, hash int, adler32 int, md5 text, start bigint, end bigint, "
                        + "PRIMARY KEY((node_id, node_version)));");
            }
        }
    }

    @Override
    public void saveChecksums(NodeChecksums checksums)
    {
        logger.info("Checksums for " + checksums.getNodeId() + "." + checksums.getNodeVersion() + " " + checksums.getChecksums().size());

        cassandraSession.getCassandraSession()
                .execute(insertChecksumsMetadataStatement.bind(checksums.getNodeId(), checksums.getNodeVersion(),
                        checksums.getVersionLabel(), checksums.getBlockSize(), checksums.getNumBlocks()));
        for(Map.Entry<Integer, List<Checksum>> checksum : checksums.getChecksums().entrySet())
        {
            Integer hash = checksum.getKey();
            for(Checksum cs : checksum.getValue())
            {
                int blockIndex = cs.getBlockIndex();
                int adler32 = cs.getAdler32();
                String md5 = cs.getMd5();
                long start = cs.getStart();
                long end = cs.getEnd();
                cassandraSession.getCassandraSession()
                    .execute(insertChecksumStatement.bind(checksums.getNodeId(), checksums.getNodeVersion(),
                            blockIndex, hash, adler32, md5, start, end));
            }
        }
    }

    private NodeChecksums toChecksums(String nodeId, long nodeVersion, Row row)
    {
        int blockSize = row.getInt("block_size");
        int numBlocks = row.getInt("num_blocks");

        NodeChecksums nodeChecksums = new NodeChecksums(nodeId, -1l, nodeVersion, "", blockSize, numBlocks);

        ResultSet rs1 = cassandraSession.getCassandraSession()
                .execute(getChecksumsStatement.bind(nodeId, nodeVersion));
        for(Row row1 : rs1.all())
        {
            int blockIndex = row1.getInt("block_idx");
            long start = row1.getLong("start");
            long end = row1.getLong("end");
            int hash = row1.getInt("hash");
            int adler32 = row1.getInt("adler32");
            String md5 = row1.getString("md5");
            Checksum checksum = new Checksum(blockIndex, start, end, hash, adler32, md5);
            nodeChecksums.addChecksum(checksum);
        }

        return nodeChecksums;
    }

    @Override
    public NodeChecksums getChecksums(String nodeId, long nodeVersion)
    {
        NodeChecksums nodeChecksums = null;

        ResultSet rs = cassandraSession.getCassandraSession()
                .execute(getChecksumsMetadataStatement.bind(nodeId, nodeVersion));
        Row row = rs.one();
        if(row != null)
        {
            nodeChecksums = toChecksums(nodeId, nodeVersion, row);
        }

        return nodeChecksums;
    }
}
