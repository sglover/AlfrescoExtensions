/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao.mongo;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.events.node.types.NodeContentGetEvent;
import org.alfresco.events.node.types.TransactionCommittedEvent;
import org.alfresco.service.common.mongo.AbstractMongoDAO;
import org.sglover.entities.dao.UserTrackingDAO;
import org.sglover.entities.values.ViewedNode;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * 
 * @author sglover
 *
 */
public class MongoUserTrackingDAO extends AbstractMongoDAO implements UserTrackingDAO
{
    private DB db;
    private String collectionName;
    private DBCollection data;

    public void setDb(DB db)
    {
        this.db = db;
    }

    public void setCollectionName(String collectionName)
    {
        this.collectionName = collectionName;
    }

    public void drop()
    {
        data.drop();
    }

    public void init()
    {
        if (db == null)
        {
            throw new RuntimeException("Mongo DB must not be null");
        }

        this.data = getCollection(db, collectionName, WriteConcern.ACKNOWLEDGED);

        {
            DBObject keys = BasicDBObjectBuilder.start("ic", 1).add("u", 1).add("t", 1).get();
            this.data.ensureIndex(keys, "main", false);
        }

        {
            DBObject keys = BasicDBObjectBuilder.start("tx", 1).get();
            this.data.ensureIndex(keys, "byTxn", false);
        }
    }

    @Override
    public void addUserNodeView(NodeContentGetEvent event)
    {
        String txnId = event.getTxnId();
        String nodeId = event.getNodeId();
        String nodeVersion = event.getVersionLabel();
        String username = event.getUsername();
        long timestamp = event.getTimestamp();

        DBObject dbObject = BasicDBObjectBuilder.start("tx", txnId).add("n", nodeId)
                .add("v", nodeVersion).add("u", username).add("t", timestamp).get();
        WriteResult result = data.insert(dbObject);
        checkResult(result);
    }

    @Override
    public List<ViewedNode> viewedNodes(String username, long timeDelta)
    {
        List<ViewedNode> viewedNodes = new LinkedList<>();

        long time = System.currentTimeMillis() - timeDelta;

        DBObject query = QueryBuilder.start("ic").is(true).and("u").is(username).and("t")
                .greaterThanEquals(time).get();
        DBCursor cursor = data.find(query);

        try
        {
            for (DBObject dbObject : cursor)
            {
                String nodeId = (String) dbObject.get("n");
                long nodeInternalId = (Long) dbObject.get("ni");
                String nodeVersion = (String) dbObject.get("v");
                long timestamp = (Long) dbObject.get("t");
                ViewedNode viewedNode = new ViewedNode(username, nodeId, nodeInternalId,
                        nodeVersion, timestamp);
                viewedNodes.add(viewedNode);
            }
        } finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        return viewedNodes;
    }

    @Override
    public void txnCommitted(TransactionCommittedEvent event)
    {
        DBObject query = QueryBuilder.start("tx").is(event.getTxnId()).get();

        DBObject update = BasicDBObjectBuilder
                .start("$set", BasicDBObjectBuilder.start("ic", true).get()).get();

        WriteResult result = data.update(query, update, false, false);
        checkResult(result);
    }
}
