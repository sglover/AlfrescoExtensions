package org.alfresco.contentstore;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.sglover.alfrescoextensions.common.CassandraSession;
import org.sglover.alfrescoextensions.common.MimeType;
import org.sglover.alfrescoextensions.common.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

/**
 * 
 * @author sglover
 *
 */
@Component
public class NodeMetadataCache
{
    @Autowired
    private CassandraSession cassandraSession;

    private PreparedStatement getNodeMetadataStatement;
    private PreparedStatement writeNodeMetadataStatement;

    private ConcurrentHashMap<Node, NodeMetadata> nodeMetadata = new ConcurrentHashMap<>();

    public NodeMetadataCache()
    {
    }

    @PostConstruct
    public void init()
    {
        createSchema();

        this.getNodeMetadataStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM " + cassandraSession.getKeyspace()
                + ".content_metadata where nodeId = ? and nodeVersion = ? and mimeType = ?");
        this.writeNodeMetadataStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + cassandraSession.getKeyspace()
                + ".content_metadata (nodeId, nodeVersion, mimetype, block_num, global_block_num) VALUES(?, ?, ?, ?, ?);");
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
                        + "mimeType text, block_num int, global_block_num bigint,"
                        + "PRIMARY KEY((nodeId, nodeVersion, mimeType), block_num));");
            }
        }
    }

    public NodeMetadata getNodeMetadata(Node node)
    {
        NodeMetadata nodeMetadata = this.nodeMetadata.get(node);
        if(nodeMetadata == null)
        {
            String nodeId = node.getNodeId();
            long nodeVersion = node.getNodeVersion();
            MimeType mimeType = node.getMimeType();

            ResultSet rs = cassandraSession.getCassandraSession()
                    .execute(getNodeMetadataStatement.bind(node.getNodeId(), node.getNodeVersion(),
                            mimeType.getMimetype()));
            Row row = rs.one();
            if(row == null)
            {
                throw new IllegalArgumentException("No such node " + node);
            }
            int blockSize = row.getInt("block_size");

            FixedSizeBlockMap blockMap = new FixedSizeBlockMap(blockSize);

            rs = cassandraSession.getCassandraSession()
                    .execute(getNodeMetadataStatement.bind(nodeId, nodeVersion, mimeType.getMimetype()));
            for(Row row1 : rs)
            {
                int blockNum = row1.getInt("block_num");
                long globalBlockNum = row1.getLong("global_block_num");
                blockMap.addBlockMapping(blockNum, globalBlockNum);
            }

            nodeMetadata = new NodeMetadata(node, blockMap);
            this.nodeMetadata.put(node, nodeMetadata);
        }

        return nodeMetadata;
//        NodeMetadata nodeMetadata = this.nodeMetadata.get(node);
//        if(nodeMetadata == null)
//        {
//            String nodeId = node.getNodeId();
//            long nodeVersion = node.getNodeVersion();
//            MimeType mimeType = node.getMimeType();
//
//            ResultSet rs = cassandraSession.getCassandraSession()
//                    .execute(getNodeStatement.bind(node.getNodeId(), node.getNodeVersion(),
//                            nodeMetadata.getMimeType().getMimetype()));
//            Row row = rs.one();
//            if(row == null)
//            {
//                throw new IllegalArgumentException("No such node " + node);
//            }
//            this.blockSize = row.getInt("block_size");
//
//            BlockMap blockMap = new BlockMap();
//
//            ResultSet rs = cassandraSession.getCassandraSession()
//                    .execute(getNodeMetadataStatement.bind(nodeId, nodeVersion, mimeType.getMimetype()));
//            for(Row row : rs)
//            {
//                int blockNum = row.getInt("block_num");
//                long globalBlockNum = row.getLong("global_block_num");
//                blockMap.addBlockMapping(blockNum, globalBlockNum);
//            }
//
//            nodeMetadata = new NodeMetadata(node, blockMap);
//            this.nodeMetadata.put(node, nodeMetadata);
//        }
//        return nodeMetadata;
    }

    public void writeNodeMetadata(NodeMetadata nodeMetadata) throws IOException
    {
        Node node = nodeMetadata.getNode();

        Iterator<Long> blockMapIt = nodeMetadata.getBlockmap().iterator();
        int i = 0;
        while(blockMapIt.hasNext())
        {
            Long globalBlockNum = blockMapIt.next();

            cassandraSession.getCassandraSession()
                .execute(writeNodeMetadataStatement.bind(node.getNodeId(), node.getNodeVersion(),
                        node.getMimeType().toString(), i++, globalBlockNum));
        }

        this.nodeMetadata.put(node,  nodeMetadata);
    }
}
