/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao.cassandra;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.sglover.alfrescoextensions.common.CassandraSession;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.EntitiesDAO;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;
import org.sglover.nlp.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.BatchStatement;
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
@Component
public class CassandraEntitiesDAO implements EntitiesDAO
{
    @Autowired
    private CassandraSession cassandraSession;

    private PreparedStatement insertNamesByNodeStatement;
    private PreparedStatement insertMiscByNodeStatement;
    private PreparedStatement insertMoneyByNodeStatement;
    private PreparedStatement insertOrgsByNodeStatement;

    private PreparedStatement insertNamesByEntityStatement;
    private PreparedStatement insertMiscByEntityStatement;
    private PreparedStatement insertMoneyByEntityStatement;
    private PreparedStatement insertOrgsByEntityStatement;

    private PreparedStatement getNamesByNodeStatement;
    private PreparedStatement getOrgsByNodeStatement;
    private PreparedStatement getNodesByName;

    private Set<String> allTypes = new HashSet<>();

    private Map<String, String> map = new HashMap<>();

    public CassandraEntitiesDAO()
    {
    }

    public CassandraEntitiesDAO(CassandraSession cassandraSession) throws IOException
    {
        this.cassandraSession = cassandraSession;

        createSchema();

        map.put("name", "nm");
        map.put("location", "l");
        map.put("misc", "mi");
        map.put("money", "m");
        map.put("date", "d");
        map.put("org", "o");

        map.put("nm", "name");
        map.put("l", "location");
        map.put("mi", "misc");
        map.put("m", "money");
        map.put("d", "date");
        map.put("o", "org");

        allTypes.add("name");
        allTypes.add("location");
        allTypes.add("org");
        allTypes.add("misc");
        allTypes.add("date");
        allTypes.add("money");

        String keyspace = cassandraSession.getKeyspace();

        this.insertNamesByNodeStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".namesByNode (nid, nv, name) "
                + " VALUES (?, ?, ?);");
        this.insertMiscByNodeStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".miscByNode (nid, nv, misc) "
                + " VALUES (?, ?, ?);");
        this.insertMoneyByNodeStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".moneyByNode (nid, nv, money) "
                + " VALUES (?, ?, ?);");
        this.insertOrgsByNodeStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".orgsByNode (nid, nv, org) "
                + " VALUES (?, ?, ?);");

        this.insertNamesByEntityStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".names (name, nid, nv) "
                + " VALUES (?, ?, ?);");
        this.insertMiscByEntityStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".misc (misc, nid, nv) "
                + " VALUES (?, ?, ?);");
        this.insertMoneyByEntityStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".money (money, nid, nv) "
                + " VALUES (?, ?, ?);");
        this.insertOrgsByEntityStatement = cassandraSession.getCassandraSession().prepare(
                "INSERT INTO " + keyspace + ".orgs (org, nid, nv) "
                + " VALUES (?, ?, ?);");

        this.getNamesByNodeStatement = cassandraSession.getCassandraSession().prepare("SELECT * FROM "
                + keyspace + ".namesByNode WHERE nid = ? and nv = ?;");
        this.getOrgsByNodeStatement = cassandraSession.getCassandraSession().prepare("SELECT * FROM "
                + keyspace + ".orgsByNode WHERE nid = ? and nv = ?;");

        this.getNodesByName = cassandraSession.getCassandraSession().prepare("SELECT * FROM "
                + keyspace + ".names WHERE name = ?;");
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
            if(keySpaceMetadata.getTable("namesByNode") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".namesByNode (nid text, nv bigint, "
                        + "name TEXT, "
                        + "PRIMARY KEY((nid, nv), name));");
            }
            if(keySpaceMetadata.getTable("miscByNode") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".miscByNode (nid text, nv bigint, "
                        + "misc TEXT, "
                        + "PRIMARY KEY((nid, nv), misc));");
            }
            if(keySpaceMetadata.getTable("moneyByNode") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".moneyByNode (nid text, nv bigint, "
                        + "money TEXT, "
                        + "PRIMARY KEY((nid, nv), money));");
            }
            if(keySpaceMetadata.getTable("orgsByNode") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".orgsByNode (nid text, nv bigint, "
                        + "org TEXT, "
                        + "PRIMARY KEY((nid, nv), org));");
            }

            if(keySpaceMetadata.getTable("names") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".names (name TEXT, nid text, nv bigint, "
                        + " PRIMARY KEY(name, nid, nv));");
            }
            if(keySpaceMetadata.getTable("misc") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".misc (misc text, nid text, nv bigint, "
                        + " PRIMARY KEY(misc, nid, nv));");
            }
            if(keySpaceMetadata.getTable("money") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".money (money text, nid text, nv bigint, "
                        + " PRIMARY KEY(money, nid, nv));");
            }
            if(keySpaceMetadata.getTable("orgs") == null)
            {
                cassandraSession.getCassandraSession().execute("CREATE TABLE IF NOT EXISTS " + keyspace + ".orgs (org TEXT, nid text, nv bigint, "
                        + " PRIMARY KEY(org, nid, nv));");
            }
        }
    }

    private void addEntitiesByNode(Node node, PreparedStatement statement, final Collection<Entity<String>> entities)
    {
        BatchStatement batch = new BatchStatement();

        for(Entity<String> nameEntity : entities)
        {
            String name = nameEntity.getEntity();
            BoundStatement st = statement.bind(node.getNodeId(), node.getNodeVersion(), name);
            batch.add(st);
        }

        cassandraSession.getCassandraSession().execute(batch);
    }

    private void addEntitiesByEntity(Node node, PreparedStatement statement, final Collection<Entity<String>> entities)
    {
        BatchStatement batch = new BatchStatement();

        for(Entity<String> entity : entities)
        {
            String name = entity.getEntity();
            BoundStatement st = statement.bind(name, node.getNodeId(), node.getNodeVersion());
            batch.add(st);
        }

        cassandraSession.getCassandraSession().execute(batch);
    }

    @Override
    public void addEntities(Node node, Entities entities)
    {
        addEntitiesByNode(node, insertNamesByNodeStatement, entities.getNames());
        addEntitiesByNode(node, insertMiscByNodeStatement, entities.getMisc());
        addEntitiesByNode(node, insertMoneyByNodeStatement, entities.getMoney());
        addEntitiesByNode(node, insertOrgsByNodeStatement, entities.getOrgs());

        addEntitiesByEntity(node, insertNamesByEntityStatement, entities.getNames());
        addEntitiesByEntity(node, insertMiscByEntityStatement, entities.getMisc());
        addEntitiesByEntity(node, insertMoneyByEntityStatement, entities.getMoney());
        addEntitiesByEntity(node, insertOrgsByEntityStatement, entities.getOrgs());
    }

    @Override
    public Stream<Entity<String>> getNames(Node node, int skip, int maxItems)
    {
        Collection<Entity<String>> ret = new HashSet<>();

        ResultSet rs = cassandraSession.getCassandraSession().execute(getNamesByNodeStatement.bind(node.getNodeId(), node.getNodeVersion()));
        for(Row row : rs)
        {
            String name = row.getString("name");
            Entity<String> entity = new Entity<String>(EntityType.names, name);
            ret.add(entity);
        }

        return ret.stream();
    }

    @Override
    public Stream<Entity<String>> getOrgs(Node node, int skip, int maxItems)
    {
        Collection<Entity<String>> ret = new HashSet<>();

        ResultSet rs = cassandraSession.getCassandraSession().execute(getOrgsByNodeStatement.bind(node.getNodeId(), node.getNodeVersion()));
        for(Row row : rs)
        {
            String name = row.getString("org");
            Entity<String> entity = new Entity<String>(EntityType.orgs, name);
            ret.add(entity);
        }

        return ret.stream();
    }

    @Override
    public Stream<Node> matchingNodes(EntityType type, String name)
    {
        List<Node> nodes = new LinkedList<>();

        PreparedStatement st = null;
        switch(type)
        {
            case names: 
            {
                st = getNodesByName;
                break;
            }
            default:
            {
                throw new IllegalArgumentException();
            }
        }

        ResultSet rs = cassandraSession.getCassandraSession().execute(st.bind(name));
        for(Row row : rs)
        {
            Node node = Node.build().nodeId(row.getString("nid")).nodeVersion(row.getLong("nv"));
            nodes.add(node);
        }

        return nodes.stream();
    }

    @Override
    public Entities getEntities(Node node)
    {
        // TODO
        Entities entities = Entities.empty();
        return entities;
    }
}
