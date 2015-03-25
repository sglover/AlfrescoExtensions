/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.semantics;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
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
public class MongoSemantics
{
    private DB db;
    private String relationsCollectionName;
    private DBCollection relations;

    public void setDb(DB db)
    {
        this.db = db;
    }

    public void setRelationsCollectionName(String relationsCollectionName)
    {
        this.relationsCollectionName = relationsCollectionName;
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

    public void init()
    {
        this.relations = getCollection(db, relationsCollectionName,
                WriteConcern.ACKNOWLEDGED);

        {
	        DBObject idx = BasicDBObjectBuilder
	        		.start("f", 1)
	                .add("c", 1)
	                .get();
	        DBObject options = BasicDBObjectBuilder
	        		.start("unique", false)
	        		.add("sparse", true)
	        		.add("name", "from")
	                .get();
	        this.relations.createIndex(idx, options);
        }

        {
	        DBObject idx = BasicDBObjectBuilder
	        		.start("t", 1)
	                .add("c", 1)
	                .get();
	        DBObject options = BasicDBObjectBuilder
	        		.start("unique", false)
	        		.add("sparse", true)
	        		.add("name", "to")
	                .get();
	        this.relations.createIndex(idx, options);
        }
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

	public void addRelation(String fromId, String toId, String category)
	{
		QueryBuilder queryBuilder = QueryBuilder
				.start("f").is(fromId)
				.and("t").is(toId);
		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder builder = BasicDBObjectBuilder
			.start("f", fromId)
			.add("t", toId)
			.add("c", category)
			.add("w", 1.0);
		DBObject insert = builder.get();
		
		WriteResult result = relations.update(query, insert, true, false);
        checkResult(result, 1);
	}

	public void deleteRelation(String fromId, String toId)
	{
		QueryBuilder queryBuilder = QueryBuilder
			.start("f").is(fromId)
			.and("t").is(toId);
		DBObject query = queryBuilder.get();

		WriteResult result = relations.remove(query);
        checkResult(result, 1);
	}

	public void increaseWeight(String fromId, String toId, double weight)
	{
		QueryBuilder queryBuilder = QueryBuilder
				.start("f").is(fromId)
				.and("t").is(toId);
		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder updateBuilder = BasicDBObjectBuilder.start("$inc", 
				BasicDBObjectBuilder.start("w", weight).get());
		DBObject update = updateBuilder.get();

		WriteResult result = relations.update(query, update, false, false);
        checkResult(result, 1);
	}
	
	public List<Relation> relationsFrom(String fromId, Set<String> categories, int skip, int maxItems)
	{
		List<Relation> ret = new LinkedList<>();

		QueryBuilder queryBuilder = QueryBuilder
				.start("f").is(fromId);
		if(categories != null && categories.size() > 0)
		{
			queryBuilder.and("c").in(categories);
		}
		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder
				.start("t", 1)
				.add("w", 1);
		DBObject orderBy = orderByBuilder.get();

		DBCursor cursor = relations.find(query).sort(orderBy).skip(skip).limit(maxItems);
		try
		{
			for(DBObject dbObject : cursor)
			{
				String toId = (String)dbObject.get("t");
				double weight = (Double)dbObject.get("w");
				Relation r = new Relation(fromId, toId, weight);
				ret.add(r);
			}
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
			}
		}

		return ret;
	}
	
	public List<Relation> relationsTo(String toId, Set<String> categories, int skip, int maxItems)
	{
		List<Relation> ret = new LinkedList<>();

		QueryBuilder queryBuilder = QueryBuilder
				.start("t").is(toId);
		if(categories != null && categories.size() > 0)
		{
			queryBuilder.and("c").in(categories);
		}
		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder
				.start("f", 1)
				.add("w", 1);
		DBObject orderBy = orderByBuilder.get();

		DBCursor cursor = relations.find(query).sort(orderBy).skip(skip).limit(maxItems);
		try
		{
			for(DBObject dbObject : cursor)
			{
				String fromId = (String)dbObject.get("f");
				double weight = (Double)dbObject.get("w");
				Relation r = new Relation(fromId, toId, weight);
				ret.add(r);
			}
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
			}
		}

		return ret;
	}
}
