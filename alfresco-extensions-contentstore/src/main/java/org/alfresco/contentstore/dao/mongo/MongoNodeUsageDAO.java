/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao.mongo;

import org.alfresco.contentstore.dao.NodeUsage;
import org.alfresco.contentstore.dao.NodeUsageDAO;
import org.sglover.alfrescoextensions.common.identity.ServerIdentity;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * 
 * @author sglover
 *
 */
public class MongoNodeUsageDAO implements NodeUsageDAO
{
    private DB db;

    private String nodeUsageCollectionName;
    private DBCollection nodeUsageData;
    private ServerIdentity serverIdentity;

    public MongoNodeUsageDAO(DB db, String nodeUsageCollectionName, ServerIdentity serverIdentity)
            throws Exception
    {
        this.db = db;
        this.nodeUsageCollectionName = nodeUsageCollectionName;
        this.serverIdentity = serverIdentity;
        init();
    }

    public void drop()
    {
        nodeUsageData.drop();
    }

    protected DBCollection getCollection(DB db, String collectionName,
            WriteConcern writeConcern)
    {
        if (!db.collectionExists(collectionName))
        {
            DBObject options = new BasicDBObject();
            db.createCollection(collectionName, options);
        }
        DBCollection collection = db.getCollection(collectionName);
        collection.setWriteConcern(writeConcern);

        return collection;
    }

    protected DBCollection getCappedCollection(DB db, String collectionName,
            Integer maxCollectionSize, Integer maxDocuments,
            WriteConcern writeConcern)
    {
        if (!db.collectionExists(collectionName))
        {
            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

            builder.add("capped", true);

            if (maxCollectionSize != null)
            {
                builder.add("size", maxCollectionSize);
            }

            if (maxDocuments != null)
            {
                builder.add("max", maxDocuments);
            }

            DBObject options = builder.get();
            db.createCollection(collectionName, options);
        }
        DBCollection collection = db.getCollection(collectionName);
        collection.setWriteConcern(writeConcern);

        return collection;
    }

    protected void checkResult(WriteResult result, int expectedNum)
    {
        boolean ok = result.getLastError().ok();
        if (!ok)
        {
            throw new RuntimeException("Mongo write failed");
        }
        if (expectedNum != result.getN())
        {
            throw new RuntimeException("Mongo write failed, expected "
                    + expectedNum + " writes, got " + result.getN());
        }
    }

    protected void checkResult(WriteResult result)
    {
        boolean ok = result.getLastError().ok();
        if (!ok)
        {
            throw new RuntimeException("Mongo write failed");
        }
    }

    private void init()
    {
        if (db == null)
        {
            throw new RuntimeException("Mongo DB must not be null");
        }

        this.nodeUsageData = getCollection(db, nodeUsageCollectionName,
                WriteConcern.ACKNOWLEDGED);
        DBObject keys = BasicDBObjectBuilder.start("e", 1).add("n", 1)
                .add("m", 1).get();
        this.nodeUsageData.ensureIndex(keys, "byMimeType", false);
    }

    private DBObject fromNodeUsageInfo(NodeUsage nodeUsage)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start("e", serverIdentity.getId())
                .add("n", nodeUsage.getNodeId())
                .add("v", nodeUsage.getNodeVersion())
                .add("t", nodeUsage.getTimestamp())
                .add("u", nodeUsage.getUsername());
        return builder.get();
    }

    @Override
    public void addUsage(NodeUsage nodeUsage)
    {
        DBObject insert = fromNodeUsageInfo(nodeUsage);
        WriteResult result = nodeUsageData.insert(insert);
        checkResult(result);
    }
}
