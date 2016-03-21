/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

/**
 * 
 * @author sglover
 *
 */
public class CassandraSession
{
    private Cluster cluster;
    private Session cassandraSession;

    public CassandraSession(String host, boolean recreate)
    {
        this.cassandraSession = buildCassandraSession(host);
        if(recreate)
        {
            deleteKeyspace();
            createKeyspace();
        }
    }

    private Session buildCassandraSession(String host)
    {
        this.cluster = Cluster
                .builder()
                .addContactPoint(host)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(
                         new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                .build();
        Session session = cluster.connect();
        return session;
    }

    private void deleteKeyspace()
    {
        KeyspaceMetadata keySpaceMetadata = cassandraSession.getCluster().getMetadata().getKeyspace("alfresco");
        if(keySpaceMetadata != null)
        {
            cassandraSession.execute("DROP KEYSPACE alfresco;");
        }
    }

    private void createKeyspace()
    {
        cassandraSession.execute("CREATE KEYSPACE alfresco WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor':3};");
    }

    public Cluster getCluster()
    {
        return cluster;
    }

    public Session getCassandraSession()
    {
        return cassandraSession;
    }

    public void shutdown()
    {
        cassandraSession.getCluster().close();
    }
}
