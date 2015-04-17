/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.dao.mongo;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.entities.dao.SimilarityDAO;
import org.alfresco.entities.values.Node;
import org.alfresco.entities.values.Similarity;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.common.mongo.AbstractMongoDAO;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * 
 * @author sglover
 *
 */
public class MongoSimilarityDAO extends AbstractMongoDAO implements SimilarityDAO
{
	private DB db;
	private String similarityCollectionName;
	private DBCollection similarityData;

	public void setDb(DB db)
	{
		this.db = db;
	}

	public void setSimilarityCollectionName(String similarityCollectionName)
	{
		this.similarityCollectionName = similarityCollectionName;
	}

	public void drop()
	{
	}

	public void init()
	{
        if (db == null)
        {
            throw new AlfrescoRuntimeException("Mongo DB must not be null");
        }

		this.similarityData = getCollection(db, similarityCollectionName, WriteConcern.ACKNOWLEDGED);

		{
	        DBObject keys = BasicDBObjectBuilder
	                .start("tx", 1)
	                .get();
	        this.similarityData.ensureIndex(keys, "btTxn", false);
		}
	}

	@Override
    public void saveSimilarity(Node node1, Node node2, double similarity)
    {
		DBObject dbObject = BasicDBObjectBuilder
				.start("n1", node1.getNodeId())
				.add("v1", node1.getNodeVersion())
				.add("n2", node2.getNodeId())
				.add("v2", node2.getNodeVersion())
				.add("s", similarity)
				.get();
		similarityData.insert(dbObject);
    }

	@Override
    public double getSimilarity(Node node1, Node node2)
    {
		List<DBObject> ors = new LinkedList<>();
		ors.add(QueryBuilder
			.start("n1").is(node1.getNodeId())
			.and("v1").is(node1.getNodeVersion())
			.and("n2").is(node2.getNodeId())
			.and("v2").is(node2.getNodeVersion())
			.get());
		ors.add(QueryBuilder
				.start("n1").is(node2.getNodeId())
				.and("v1").is(node2.getNodeVersion())
				.and("n2").is(node1.getNodeId())
				.and("v2").is(node1.getNodeVersion())
				.get());

		DBObject query = QueryBuilder
				.start().or(ors.toArray(new DBObject[0]))
				.get();

		DBObject dbObject = similarityData.findOne(query);
		Double similarity = (dbObject != null ? (Double)dbObject.get("s") : null);
		return (similarity != null ? similarity : -1.0);
    }

	@Override
	public List<Similarity> getSimilar(Node node)
	{
		List<Similarity> nodes = new LinkedList<>();

		List<DBObject> ors = new LinkedList<>();
		ors.add(QueryBuilder
			.start("n1").is(node.getNodeId())
			.and("v1").is(node.getNodeVersion())
			.get());
		ors.add(QueryBuilder
				.start("n2").is(node.getNodeId())
				.and("v2").is(node.getNodeVersion())
				.get());

		DBObject query = QueryBuilder
				.start().or(ors.toArray(new DBObject[0]))
				.get();

		DBObject orderBy = BasicDBObjectBuilder
				.start("s", -1)
				.get();

		DBCursor cursor = similarityData.find(query).sort(orderBy);
		try
		{
			for(DBObject dbObject : cursor)
			{
				String node1Id = (String)dbObject.get("n1");
				String node1Version = (String)dbObject.get("v1");
				String node2Id = (String)dbObject.get("n2");
				String node2Version = (String)dbObject.get("v2");
				double similarity = (Double)dbObject.get("s");

				if(node1Id.equals(node.getNodeId()))
				{
					if(!node2Id.equals(node.getNodeId()))
					{
						Node n = Node.build().nodeId(node2Id).nodeVersion(node2Version);
						Similarity s = new Similarity(n, similarity);
						nodes.add(s);
					}
				}
				else
				{
					if(node2Id.equals(node.getNodeId()))
					{
						Node n = Node.build().nodeId(node1Id).nodeVersion(node1Version);
						Similarity s = new Similarity(n, similarity);
						nodes.add(s);
					}
				}
			}
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
			}
		}

		return nodes;
	}
}
