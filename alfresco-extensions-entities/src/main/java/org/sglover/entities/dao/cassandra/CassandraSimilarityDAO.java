/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao.cassandra;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.sglover.alfrescoextensions.common.CassandraSession;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.SimilarityDAO;
import org.sglover.entities.values.Similarity;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

/**
 * 
 * @author sglover
 *
 */
public class CassandraSimilarityDAO implements SimilarityDAO
{
    private final CassandraSession cassandraSession;
    private final PreparedStatement insertSimilarityStatement;
    private final PreparedStatement getSimilarityStatement;

    public CassandraSimilarityDAO(CassandraSession cassandraSession) throws IOException
    {
        this.cassandraSession = cassandraSession;

        createSchema();

        this.insertSimilarityStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + cassandraSession.getKeyspace() + ".similarity" 
                        + "(n1id, n1v, n2id, n2v, s) VALUES (?, ?, ?, ?, ?);");
        this.getSimilarityStatement = cassandraSession.getCassandraSession().prepare(
                "SELECT * FROM " + cassandraSession.getKeyspace() + ".similarity" 
                        + " WHERE n1id = ? AND n1v = ? AND n2id = ? AND n2v = ?;");
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
            if(keySpaceMetadata.getTable("similarity") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".similarity (n1id text, n1v bigint, "
                        + "n2id text, n2v bigint, s double, "
                        + "PRIMARY KEY((n1id, n1v, n2id, n2v)));");
            }
        }
    }

    @Override
    public void saveSimilarity(Node node1, Node node2, double similarity)
    {
        BoundStatement statement = insertSimilarityStatement.bind(node1.getNodeId(), node1.getNodeVersion(),
                node2.getNodeId(), node2.getNodeVersion(), similarity);
        cassandraSession.getCassandraSession().execute(statement);
    }

    @Override
    public double getSimilarity(Node node1, Node node2)
    {
        double similarity = -1.0;

        BoundStatement statement = getSimilarityStatement.bind(node1.getNodeId(), node1.getNodeVersion(),
                node2.getNodeId(), node2.getNodeVersion());
        ResultSet rs = cassandraSession.getCassandraSession().execute(statement);
        Row row = rs.one();
        if(row == null)
        {
            statement = getSimilarityStatement.bind(node2.getNodeId(), node2.getNodeVersion(),
                    node1.getNodeId(), node1.getNodeVersion());
            rs = cassandraSession.getCassandraSession().execute(statement);
            row = rs.one();
        }

        if(row != null)
        {
            similarity = row.getDouble("s");
        }

        return similarity;
    }

    @Override
    public List<Similarity> getSimilar(Node node)
    {
        return Collections.emptyList();
    }
}
