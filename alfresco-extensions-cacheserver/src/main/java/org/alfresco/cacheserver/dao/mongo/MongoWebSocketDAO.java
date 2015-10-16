/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dao.mongo;

import org.alfresco.cacheserver.dao.WebsocketDAO;
import org.alfresco.cacheserver.dao.data.Registration;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * 
 * @author sglover
 *
 */
public class MongoWebSocketDAO implements WebsocketDAO
{
	private DB db;

	private String registrationDataCollectionName;
	private DBCollection registrationData;

	public MongoWebSocketDAO(DB db, String registrationDataCollectionName) throws Exception
	{
		this.db = db;
		this.registrationDataCollectionName = registrationDataCollectionName;
		init();
	}

	public void drop()
	{
	    registrationData.drop();
	}

	protected DBCollection getCollection(DB db, String collectionName, WriteConcern writeConcern)
	{
	    if(!db.collectionExists(collectionName))
	    {
	        DBObject options = new BasicDBObject();
	        db.createCollection(collectionName, options);
	    }
	    DBCollection collection = db.getCollection(collectionName);
	    collection.setWriteConcern(writeConcern);

	    return collection;
	}

    protected DBCollection getCappedCollection(DB db, String collectionName, Integer maxCollectionSize, Integer maxDocuments, WriteConcern writeConcern)
    {
        if(!db.collectionExists(collectionName))
        {
            BasicDBObjectBuilder builder = BasicDBObjectBuilder
                    .start();

            builder.add("capped", true);
            
            if(maxCollectionSize != null)
            {
                builder.add("size", maxCollectionSize);
            }

            if(maxDocuments != null)
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
	    if(!ok)
	    {
	        throw new RuntimeException("Mongo write failed");
	    }
	    if(expectedNum != result.getN())
	    {
	        throw new RuntimeException("Mongo write failed, expected " + expectedNum + " writes, got " + result.getN());
	    }
	}

	
	protected void checkResult(WriteResult result)
	{
        boolean ok = result.getLastError().ok();
        if(!ok)
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

		this.registrationData = getCollection(db, registrationDataCollectionName, WriteConcern.ACKNOWLEDGED);

		{
	        DBObject keys = BasicDBObjectBuilder
	        		.start("u", 1)
	                .get();
	        this.registrationData.ensureIndex(keys, "byUserName", false);
		}
	}

	@Override
	public void register(Registration registration)
	{
        DBObject insert = fromRegistration(registration);
        WriteResult result = registrationData.insert(insert);
        checkResult(result);
	}

	private Registration toRegistration(DBObject dbObject)
	{
	    Registration registration = null;

		if(dbObject != null)
		{
			String ipAddress = (String)dbObject.get("i");
			String username = (String)dbObject.get("u");
			registration = new Registration(ipAddress, username);
		}

		return registration;
	}

	private DBObject fromRegistration(Registration registration)
	{
		BasicDBObjectBuilder builder = BasicDBObjectBuilder
				.start("u", registration.getUsername())
				.add("i", registration.getIpAddress());
		return builder.get();
	}

	@Override
	public Registration getByUsername(String username)
	{
	    QueryBuilder queryBuilder = QueryBuilder
	            .start("u").is(username);
	    DBObject query = queryBuilder.get();

	    DBObject dbObject = registrationData.findOne(query);
	    Registration registration = toRegistration(dbObject);
	    return registration;
	}
}
