/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.domain.node.mongo;

import org.alfresco.repo.domain.node.ConcurrentModificationException;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.TransactionListener;
import org.alfresco.repo.domain.node.TransactionSupport;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * 
 * @author sglover
 *
 */
public class MongoNodeDAO implements NodeDAO, TransactionListener
{
	private DBCollection nodes;

	private DB db;
	private String nodesCollectionName;
    private DBCollection nodesData;

	public void setDb(DB db)
	{
		this.db = db;
	}

	public void setNodesCollectionName(String nodesCollectionName)
	{
		this.nodesCollectionName = nodesCollectionName;
	}
	
	public void drop()
	{
		nodesData.drop();
	}

	private NodeEntity toNodeEntity(DBObject dbObject)
	{
		NodeEntity entity = new NodeEntity();
		entity.setParentNodeId((Long)dbObject.get("pn"));
		entity.setParentNodeVersion((Long)dbObject.get("pv"));
		entity.setNodeId((Long)dbObject.get("n"));
		entity.setNodeVersion((Long)dbObject.get("v"));
		entity.setTxnId((String)dbObject.get("t"));
		entity.setVersionLabel((String)dbObject.get("vl"));
		return entity;
	}

	private DBCollection getCollection(DB db, String collectionName, WriteConcern writeConcern)
	{
		DBCollection collection = db.getCollection(collectionName);
		collection.setWriteConcern(writeConcern);
		return collection;
	}

	public void init()
	{
        if (db == null)
        {
            throw new RuntimeException("Mongo DB must not be null");
        }

        this.nodes = getCollection(db, nodesCollectionName, WriteConcern.ACKNOWLEDGED);

        {
	        DBObject primary = BasicDBObjectBuilder
	                .start("n", 1)
	                .add("v", 1)
	                .get();
	        DBObject options = BasicDBObjectBuilder
	                .start("unique", true)
	                .get();
	        nodes.createIndex(primary, options);
        }

        {
	        DBObject idx1 = BasicDBObjectBuilder
	                .start("n", 1)
	                .add("vl", 1)
	                .add("c", 1)
	                .add("v", 1)
	                .get();
	        nodes.createIndex(idx1);
        }

        {
	        DBObject idx1 = BasicDBObjectBuilder
	                .start("n", 1)
	                .add("c", 1)
	                .add("v", 1)
	                .get();
	        nodes.createIndex(idx1);
        }
	}

	@Override
	public NodeEntity newNode(long parentNodeId, long parentNodeVersion, long childNodeId)
	{
		String txnId = TransactionSupport.getTxnId();
		String childVersionLabel = "1.0";
		long childNodeVersion = 1;

		DBObject insert = BasicDBObjectBuilder
			.start("t", txnId)
			.add("pn", parentNodeId)
			.add("pv", parentNodeVersion)
			.add("n", childNodeId)
			.add("v", childNodeVersion)
			.add("vl", childVersionLabel)
			.get();
		try
		{
			nodes.insert(insert);
		}
		catch(MongoException.DuplicateKey e)
		{
			throw new RuntimeException("Already exists");
		}

		NodeEntity entity = new NodeEntity();
		entity.setParentNodeId(parentNodeId);
		entity.setParentNodeVersion(parentNodeVersion);
		entity.setNodeId(childNodeId);
		entity.setNodeVersion(childNodeVersion);
		entity.setTxnId(txnId);
		entity.setVersionLabel(childVersionLabel);
		return entity;
	}

	private NodeEntity getLatest(long nodeId)
	{
		NodeEntity nodeEntity = null;

		DBObject query = QueryBuilder
				.start("n").is(nodeId)
				.and("c").is(true)
				.get();
		DBObject order = BasicDBObjectBuilder
				.start("v", -1)
				.get();
		DBCursor cursor = nodes.find(query).sort(order).limit(1);
		try
		{
			if(cursor.hasNext())
			{
				DBObject dbObject = cursor.next();
				nodeEntity = toNodeEntity(dbObject);
			}
		}
		finally
		{
			cursor.close();
		}

		return nodeEntity;
	}

	@Override
	public NodeEntity updateNode(long childNodeId, String childVersionLabel)
	{
		String txnId = TransactionSupport.getTxnId();

		NodeEntity nodeEntity = getLatest(childNodeId);
		long childNodeVersion = nodeEntity.getNodeVersion() + 1;
		long parentNodeId = nodeEntity.getParentNodeId();
		long parentNodeVersion = nodeEntity.getParentNodeVersion();

		DBObject insert = BasicDBObjectBuilder
			.start("t", txnId)
			.add("pn", parentNodeId)
			.add("pv", parentNodeVersion)
			.add("n", childNodeId)
			.add("v", childNodeVersion)
			.add("vl", childVersionLabel)
			.get();
		try
		{
			nodes.insert(insert);
		}
		catch(MongoException.DuplicateKey e)
		{
			throw new ConcurrentModificationException();
		}

		NodeEntity entity = new NodeEntity();
		entity.setParentNodeId(parentNodeId);
		entity.setParentNodeVersion(parentNodeVersion);
		entity.setNodeId(childNodeId);
		entity.setNodeVersion(childNodeVersion);
		entity.setTxnId(txnId);
		entity.setVersionLabel(childVersionLabel);
		return entity;
	}

	@Override
	public NodeEntity getByVersionLabel(long nodeId, String versionLabel)
	{
		NodeEntity nodeEntity = null;

		DBObject query = QueryBuilder
			.start("n").is(nodeId)
			.and("vl").is(versionLabel)
			.and("c").is(true)
			.get();
		DBObject orderBy = BasicDBObjectBuilder
				.start("v", -1)
				.get();
		DBCursor cursor = nodes.find(query).sort(orderBy).limit(1);
		try
		{
			int size = cursor.size();
			if(size > 1)
			{
				throw new RuntimeException();
			}
			else if(size == 1)
			{
				DBObject dbObject = cursor.next();
				nodeEntity = toNodeEntity(dbObject);
			}
		}
		finally
		{
			cursor.close();
		}

		return nodeEntity;
	}

	@Override
	public void onRollback(String txnId)
	{
		DBObject query = BasicDBObjectBuilder
				.start("t", txnId)
				.get();
		nodes.remove(query);
	}

	@Override
	public void onCommit(String txnId)
	{
		DBObject query = QueryBuilder
				.start("t").is(txnId)
				.get();
		DBObject update = BasicDBObjectBuilder
				.start("$set", BasicDBObjectBuilder
						.start("c", true)
						.get())
				.get();
		nodes.update(query, update, false, true);
	}
}
