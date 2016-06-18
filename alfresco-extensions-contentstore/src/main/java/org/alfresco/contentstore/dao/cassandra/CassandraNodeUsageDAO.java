/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao.cassandra;

import javax.annotation.PostConstruct;

import org.alfresco.contentstore.dao.NodeUsage;
import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.sglover.alfrescoextensions.common.CassandraSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

/**
 * 
 * @author sglover
 *
 */
@Component
public class CassandraNodeUsageDAO implements NodeUsageDAO
{
    @Autowired
    private CassandraSession cassandraSession;

    private Session session;

    private PreparedStatement insertNodeUsageStatement;
    private PreparedStatement getNodeUsageStatement;

    public CassandraNodeUsageDAO()
    {
    }

    public CassandraNodeUsageDAO(CassandraSession cassandraSession)
    {
        this.cassandraSession = cassandraSession;
        init();
    }

    @PostConstruct
    public void init()
    {
        this.session = cassandraSession.getCassandraSession();

        createSchema();

        String keyspace = cassandraSession.getKeyspace();

        this.getNodeUsageStatement = session.prepare(
                "SELECT * FROM " + keyspace + ".node_usage where node_id = ? and node_version = ?");
        this.insertNodeUsageStatement = session.prepare(
                "INSERT INTO " + keyspace + ".node_usage (node_id, node_version, ts, type, username) VALUES (?, ?, ?, ?, ?)");
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
            if(keySpaceMetadata.getTable("node_usage") == null)
            {
                session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".node_usage (node_id text, node_version bigint, "
                        + "ts bigint, type text, username text, "
                        + "PRIMARY KEY((node_id, node_version)));");
            }
        }
    }

    public void drop()
    {

    }

    @Override
    public void addUsage(NodeUsage nodeUsage)
    {
        cassandraSession.getCassandraSession()
            .execute(insertNodeUsageStatement.bind(nodeUsage.getNodeId(), nodeUsage.getNodeVersion(),
                nodeUsage.getTimestamp(), nodeUsage.getType().toString(), nodeUsage.getUsername()));
    }
}
