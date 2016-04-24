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

import org.sglover.alfrescoextensions.common.CassandraSession;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.EntitiesDAO;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;

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
public class CassandraEntitiesDAO implements EntitiesDAO
{
    private final CassandraSession cassandraSession;

    private final PreparedStatement insertNamesByNodeStatement;
    private final PreparedStatement insertMiscByNodeStatement;
    private final PreparedStatement insertMoneyByNodeStatement;
    private final PreparedStatement insertOrgsByNodeStatement;

    private final PreparedStatement insertNamesByEntityStatement;
    private final PreparedStatement insertMiscByEntityStatement;
    private final PreparedStatement insertMoneyByEntityStatement;
    private final PreparedStatement insertOrgsByEntityStatement;

    private final PreparedStatement getNamesByNodeStatement;
    private final PreparedStatement getOrgsByNodeStatement;
    private final PreparedStatement getNodesByName;

    private Set<String> allTypes = new HashSet<>();

    private Map<String, String> map = new HashMap<>();

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
    public Collection<Entity<String>> getNames(Node node)
    {
        Collection<Entity<String>> ret = new HashSet<>();

        ResultSet rs = cassandraSession.getCassandraSession().execute(getNamesByNodeStatement.bind(node.getNodeId(), node.getNodeVersion()));
        for(Row row : rs)
        {
            String name = row.getString("name");
            Entity<String> entity = new Entity<String>("name", name);
            ret.add(entity);
        }

        return ret;
    }

    @Override
    public Collection<Entity<String>> getOrgs(Node node)
    {
        Collection<Entity<String>> ret = new HashSet<>();

        ResultSet rs = cassandraSession.getCassandraSession().execute(getOrgsByNodeStatement.bind(node.getNodeId(), node.getNodeVersion()));
        for(Row row : rs)
        {
            String name = row.getString("org");
            Entity<String> entity = new Entity<String>("org", name);
            ret.add(entity);
        }

        return ret;
    }

//    @Override
//    public EntityCounts<String> getEntityCounts(Node node) {
//        String nodeId = node.getNodeId();
//        String nodeVersion = node.getVersionLabel();
//
//        EntityCounts<String> ret = new EntityCounts<>();
//
//        QueryBuilder queryBuilder = QueryBuilder
//                // .and("c").is(true)
//                .start("n").is(nodeId).and("v").is(nodeVersion);
//        DBObject query = queryBuilder.get();
//
//        BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder.start("c",
//                1);
//        DBObject orderBy = orderByBuilder.get();
//
//        DBCursor cursor = entitiesData.find(query).sort(orderBy);
//        try {
//            for (DBObject dbObject : cursor) {
//                Entity<String> entity = getEntity(dbObject);
//                ret.addEntity(entity);
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return ret;
//    }

    @Override
    public List<Node> matchingNodes(String type, String name)
    {
        List<Node> nodes = new LinkedList<>();

        PreparedStatement st = null;
        switch(type)
        {
            case "name": 
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

        return nodes;
    }

    @Override
    public Entities getEntities(Node node)
    {
        Entities entities = Entities.empty();

        Collection<Entity<String>> names = getNames(node);
        for(Entity<String> nameEntity : names)
        {
            entities.addName(nameEntity.getEntity());
        }

        Collection<Entity<String>> orgs = getOrgs(node);
        for(Entity<String> orgEntity : orgs)
        {
            entities.addOrg(orgEntity.getEntity());
        }

        return entities;
    }

//    @Override
//    public EntityCounts<String> getNodeMatches(Entities entities) {
//        EntityCounts<String> entityCounts = new EntityCounts<>();
//
//        List<DBObject> ors = new LinkedList<>();
//
//        {
//            List<String> entityNames = new LinkedList<>();
//            for (Entity<String> entity : entities.getNames()) {
//                entityNames.add(entity.getEntity());
//            }
//
//            String key = map.get("name");
//            DBObject dbObject = QueryBuilder.start(key).in(entityNames).get();
//            ors.add(dbObject);
//        }
//
//        {
//            List<String> entityNames = new LinkedList<>();
//            for (Entity<String> entity : entities.getLocations()) {
//                entityNames.add(entity.getEntity());
//            }
//
//            String key = map.get("location");
//            DBObject dbObject = QueryBuilder.start(key).in(entityNames).get();
//            ors.add(dbObject);
//        }
//
//        QueryBuilder queryBuilder = QueryBuilder.start()
//                .or(ors.toArray(new DBObject[0]));
//
//        DBObject query = queryBuilder.get();
//
//        BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder.start("c",
//                1);
//        DBObject orderBy = orderByBuilder.get();
//
//        DBCursor cursor = entitiesData.find(query).sort(orderBy);
//        try {
//            for (DBObject dbObject : cursor) {
//                Entity<String> entity = getEntity(dbObject);
//                entityCounts.addEntity(entity);
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return entityCounts;
//    }

//    @Override
//    public Entities getEntities(Node node, Set<String> types) {
//        String nodeId = node.getNodeId();
//        String nodeVersion = node.getVersionLabel();
//
//        Entities entities = Entities.empty(nodeId, nodeVersion);
//
//        QueryBuilder queryBuilder = QueryBuilder.start("n").is(nodeId).and("v")
//                .is(nodeVersion);
//
//        if (types != null && types.size() > 0) {
//            queryBuilder.and("t").in(types);
//        }
//
//        DBObject query = queryBuilder.get();
//
//        DBCursor cursor = entitiesData.find(query);
//        try {
//            for (DBObject dbObject : cursor) {
//                Entity<String> entity = getEntity(dbObject);
//                entities.addEntity(entity);
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return entities;
//    }

//    private boolean different(String nodeId1, String nodeVersion1,
//            String nodeId2, String nodeVersion2) {
//        boolean different = false;
//        if (!nodeId1.equals(nodeId2)) {
//            different = true;
//        } else {
//            if (nodeVersion1 != nodeVersion2) {
//                different = !EqualsHelper.nullSafeEquals(nodeVersion1,
//                        nodeVersion2);
//            }
//        }
//
//        return different;
//    }

//    @Override
//    public List<Entities> getEntities() {
//        List<Entities> allEntities = new LinkedList<>();
//
//        QueryBuilder queryBuilder = QueryBuilder.start();
//
//        DBObject query = queryBuilder.get();
//
//        DBObject orderBy = BasicDBObjectBuilder.start("n", 1).add("v", 1).get();
//
//        DBCursor cursor = entitiesData.find(query).sort(orderBy);
//        try {
//            Entities entities = null;
//
//            for (DBObject dbObject : cursor) {
//                String nodeId = (String) dbObject.get("n");
//                String nodeVersion = (String) dbObject.get("v");
//                Entity<String> entity = getEntity(dbObject);
//                if (entities == null) {
//                    entities = Entities.empty(nodeId, nodeVersion);
//                    allEntities.add(entities);
//                } else {
//                    if (different(nodeId, nodeVersion, entities.getNodeId(),
//                            entities.getNodeVersion())) {
//                        entities = Entities.empty(nodeId, nodeVersion);
//                        allEntities.add(entities);
//                    }
//                }
//                entities.addEntity(entity);
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return allEntities;
//    }

//    @Override
//    public List<Entities> unprocessedEntites() {
//        // TODO Auto-generated method stub
//        return null;
//    }

//    @Override
//    public List<Entities> getEntitiesForTxn(String txnId) {
//        List<Entities> ret = new LinkedList<>();
//
//        QueryBuilder queryBuilder = QueryBuilder.start("tx").is(txnId);
//
//        DBObject query = queryBuilder.get();
//
//        DBCursor cursor = entitiesData.find(query);
//        try {
//            Entities entities = null;
//
//            for (DBObject dbObject : cursor) {
//                String nodeId = (String) dbObject.get("n");
//                String nodeVersion = (String) dbObject.get("v");
//                Entity<String> entity = getEntity(dbObject);
//                if (entities == null) {
//                    entities = Entities.empty(nodeId, nodeVersion);
//                    ret.add(entities);
//                } else {
//                    if (!nodeId.equals(entities.getNodeId())
//                            && !EqualsHelper.nullSafeEquals(nodeVersion,
//                                    entities.getNodeVersion())) {
//                        entities = Entities.empty(nodeId, nodeVersion);
//                        ret.add(entities);
//                    }
//                }
//                entities.addEntity(entity);
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return ret;
//    }

    // @Override
    // public EntityCounts<String> overlap(Node node)
    // {
    // String nodeId = node.getNodeId();
    // String nodeVersion = node.getNodeVersion();
    //
    // Entities entities = getEntities(node, allTypes);
    // EntityCounts<String> entityCounts = getNodeMatches(entities);
    // return entityCounts;
    // }
}
