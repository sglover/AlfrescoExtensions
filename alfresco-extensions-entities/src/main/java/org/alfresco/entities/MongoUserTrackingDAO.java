/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.common.mongo.AbstractMongoDAO;

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
			throw new AlfrescoRuntimeException("Mongo DB must not be null");
		}

		this.data = getCollection(db, collectionName, WriteConcern.ACKNOWLEDGED);

		// we want a record per (txnId, nodeId, changeType)
//        DBObject keys = BasicDBObjectBuilder
//                .start("n", 1)
//                .add("nm", 1)
//                .get();
//        this.data.ensureIndex(keys, "name", false);
	}

	@Override
	public void addUserNodeView(long nodeInternalId, long nodeVersion, String username, long timestamp)
	{
		DBObject dbObject = BasicDBObjectBuilder
				.start("n", nodeInternalId)
				.add("v", nodeVersion)
				.add("u", username)
				.add("t", timestamp)
				.get();
		WriteResult result = data.insert(dbObject);
		checkResult(result);
	}

	@Override
	public List<ViewedNode> viewedNodes(String username, long timeDelta)
	{
		List<ViewedNode> viewedNodes = new LinkedList<>();

		long time = System.currentTimeMillis() - timeDelta;

		DBObject query = QueryBuilder
			.start("u").is(username)
			.and("t").greaterThanEquals(time)
			.get();
		DBCursor cursor = data.find(query);

		try
		{
			for(DBObject dbObject : cursor)
			{
				long nodeInternalId = (Long)dbObject.get("n");
				long nodeVersion = (Long)dbObject.get("v");
				long timestamp = (Long)dbObject.get("t");
				ViewedNode viewedNode = new ViewedNode(username, nodeInternalId, nodeVersion, timestamp);
				viewedNodes.add(viewedNode);
			}
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
			}
		}

		return viewedNodes;
	}

}
