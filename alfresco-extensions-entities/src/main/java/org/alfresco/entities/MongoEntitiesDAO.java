/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.alfresco.services.nlp.EntityLocation;
import org.alfresco.service.common.mongo.AbstractMongoDAO;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
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
public class MongoEntitiesDAO extends AbstractMongoDAO implements EntitiesDAO, Serializable
{
	private static final long serialVersionUID = -5890516563565690912L;

	private DB db;
	private String entitiesCollectionName;
	private DBCollection entitiesData;

	private Set<String> allTypes = new HashSet<>();

	private Map<String, String> map = new HashMap<>();
	
	public void setDb(DB db)
	{
		this.db = db;
	}

	public void setEntitiesCollectionName(String entitiesCollectionName)
	{
		this.entitiesCollectionName = entitiesCollectionName;
	}
	
	public void dropEntities()
	{
		entitiesData.drop();
	}
	
	public void drop()
	{
		dropEntities();
	}

	public void init()
	{
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

        if (db == null)
        {
            throw new AlfrescoRuntimeException("Mongo DB must not be null");
        }

		this.entitiesData = getCollection(db, entitiesCollectionName, WriteConcern.ACKNOWLEDGED);

		// we want a record per (txnId, nodeId, changeType)
        DBObject keys = BasicDBObjectBuilder
                .start("n", 1)
                .add("nm", 1)
                .get();
        this.entitiesData.ensureIndex(keys, "name", false);
	}

	private void addEntities(long nodeId, long nodeVersion, String type, String key, Collection<Entity<String>> entities)
	{
		BulkWriteOperation bulk = entitiesData.initializeUnorderedBulkOperation();

		if(entities.size() > 0)
		{
			int expected = entities.size();
			for(Entity<String> nameEntity : entities)
			{
				List<EntityLocation> locations = nameEntity.getLocations();
				List<DBObject> locs = new LinkedList<>();
				for(EntityLocation location : locations)
				{
					long start = location.getStartOffset();
					long end = location.getEndOffset();
					String context = location.getContext();
					double probability = location.getProbability();
	
					DBObject locDBObject = BasicDBObjectBuilder
							.start("s", start)
							.add("e", end)
							.add("p", probability)
							.add("c", context)
							.get();
					locs.add(locDBObject);
				}
	
				DBObject dbObject = BasicDBObjectBuilder
						.start("n", nodeId)
						.add("v", nodeVersion)
						.add("t", type)
						.add(key, nameEntity.getEntity())
						.add("c", nameEntity.getCount())
						.add("locs", locs)
						.get();
				bulk.insert(dbObject);
			}
	
			BulkWriteResult result = bulk.execute();
			int inserted = result.getInsertedCount();
	
			if(expected != inserted)
			{
				throw new RuntimeException("Mongo write failed");
			}
		}
	}

	@Override
    public void addEntities(long nodeId, long nodeVersion, Entities entities)
    {
		Collection<Entity<String>> nameEntities = entities.getNames();
		String key = map.get("name");
		addEntities(nodeId, nodeVersion, "name", key, nameEntities);

		Collection<Entity<String>> locationEntities = entities.getLocations();
		key = map.get("location");
		addEntities(nodeId, nodeVersion, "location", key, locationEntities);

		Collection<Entity<String>> orgEntities = entities.getOrgs();
		key = map.get("org");
		addEntities(nodeId, nodeVersion, "org", key, orgEntities);
    }

	@Override
    public Collection<Entity<String>> getNames(long nodeId, long nodeVersion)
    {
		Collection<Entity<String>> ret = new LinkedList<>();

		QueryBuilder queryBuilder = QueryBuilder
				.start("n").is(nodeId)
				.and("v").is(nodeVersion);
		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder
				.start("nm", 1);
		DBObject orderBy = orderByBuilder.get();

		DBCursor cursor = entitiesData.find(query).sort(orderBy);
		try
		{
			for(DBObject dbObject : cursor)
			{
				String name = (String)dbObject.get("nm");
				int count = (Integer)dbObject.get("c");
				String type = map.get("nm");
				Entity<String> entity = new Entity<>(type, name, count);
				ret.add(entity);
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
	
	@SuppressWarnings("unchecked")
    private Entity<String> getEntity(DBObject dbObject)
	{
		String type = (String)dbObject.get("t");
		String key = map.get(type);
		String name = (String)dbObject.get(key);
		long count = (Long)dbObject.get("c");
		List<DBObject> locs = (List<DBObject>)dbObject.get("locs");

		Entity<String> entity = new Entity<>(type, name, count);
		for(DBObject locDBObject : locs)
		{
			long start = (Long)locDBObject.get("s");
			long end = (Long)locDBObject.get("e");
			double probability = (Double)locDBObject.get("p");
			String context = (String)locDBObject.get("c");

			entity.addLocation(start, end, probability, context);
		}

		return entity;
	}

	@Override
	public EntityCounts<String> getEntityCounts(long nodeId, long nodeVersion)
	{
		EntityCounts<String> ret = new EntityCounts<>();

		QueryBuilder queryBuilder = QueryBuilder
				.start("n").is(nodeId)
				.and("v").is(nodeVersion);
		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder
				.start("c", 1);
		DBObject orderBy = orderByBuilder.get();

		DBCursor cursor = entitiesData.find(query).sort(orderBy);
		try
		{
			for(DBObject dbObject : cursor)
			{
				Entity<String> entity = getEntity(dbObject);
				ret.addEntity(entity);
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

	@Override
	public List<Node> matchingNodes(String type, String name)
	{
		List<Node> nodes = new LinkedList<>();

		QueryBuilder queryBuilder = QueryBuilder
			.start("t").is(type)
			.and("nm").is(name);
		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder
				.start("c", 1);
		DBObject orderBy = orderByBuilder.get();

		DBCursor cursor = entitiesData.find(query).sort(orderBy);
		try
		{
			for(DBObject dbObject : cursor)
			{
				long nodeInernalId = (Long)dbObject.get("n");
				long nodeVersion = (Long)dbObject.get("v");
				Node node = new Node(nodeInernalId, nodeVersion);
				nodes.add(node);
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

	public EntityCounts<String> getNodeMatches(Entities entities)
	{
		EntityCounts<String> entityCounts = new EntityCounts<>();

		List<DBObject> ors = new LinkedList<>();

		{
			List<String> entityNames = new LinkedList<>();
			for(Entity<String> entity : entities.getNames())
			{
				entityNames.add(entity.getEntity());
			}

			String key = map.get("name");
			DBObject dbObject = QueryBuilder
					.start(key).in(entityNames)
					.get();
			ors.add(dbObject);
		}

		{
			List<String> entityNames = new LinkedList<>();
			for(Entity<String> entity : entities.getLocations())
			{
				entityNames.add(entity.getEntity());
			}

			String key = map.get("location");
			DBObject dbObject = QueryBuilder
					.start(key).in(entityNames)
					.get();
			ors.add(dbObject);
		}

		QueryBuilder queryBuilder = QueryBuilder
				.start()
				.or(ors.toArray(new DBObject[0]));

		DBObject query = queryBuilder.get();

		BasicDBObjectBuilder orderByBuilder = BasicDBObjectBuilder
				.start("c", 1);
		DBObject orderBy = orderByBuilder.get();

		DBCursor cursor = entitiesData.find(query).sort(orderBy);
		try
		{
			for(DBObject dbObject : cursor)
			{
				Entity<String> entity = getEntity(dbObject);
				entityCounts.addEntity(entity);
			}
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
			}
		}

		return entityCounts;
	}

	@Override
	public Entities getEntities(long nodeId, long nodeVersion, Set<String> types)
	{
		Entities entities = Entities.empty();

		QueryBuilder queryBuilder = QueryBuilder
				.start("n").is(nodeId)
				.and("v").is(nodeVersion);
		if(types != null && types.size() > 0)
		{
			queryBuilder.and("t").in(types);
		}

		DBObject query = queryBuilder.get();

		DBCursor cursor = entitiesData.find(query);
		try
		{
			for(DBObject dbObject : cursor)
			{
				Entity<String> entity = getEntity(dbObject);
				entities.addEntity(entity);
			}
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
			}
		}

		return entities;
	}

	@Override
	public EntityCounts<String> overlap(long nodeId, long nodeVersion)
	{
		Entities entities = getEntities(nodeId, nodeVersion, allTypes);
		EntityCounts<String> entityCounts = getNodeMatches(entities);
		return entityCounts;
	}
}
