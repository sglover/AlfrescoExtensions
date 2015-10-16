/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.dao.mongo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.entities.dao.EntitiesDAO;
import org.alfresco.entities.values.EntityCounts;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extensions.common.Node;
import org.alfresco.service.common.mongo.AbstractMongoDAO;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.alfresco.services.nlp.EntityLocation;
import org.alfresco.util.EqualsHelper;

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
public class MongoEntitiesDAO extends AbstractMongoDAO implements EntitiesDAO
{
	private DB db;
	private String entitiesCollectionName;
	private DBCollection entitiesData;

	private Set<String> allTypes = new HashSet<>();

	private Map<String, String> map = new HashMap<>();
	
	public MongoEntitiesDAO(DB db, String entitiesCollectionName)
	{
		this.db = db;
		this.entitiesCollectionName = entitiesCollectionName;
		init();
	}

	public void dropEntities()
	{
		entitiesData.drop();
	}
	
	public void drop()
	{
		dropEntities();
	}

	private void init()
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

		{
	        DBObject keys = BasicDBObjectBuilder
	                .start("n", 1)
	                .add("v", 1)
	                .get();
	        this.entitiesData.ensureIndex(keys, "main", false);
		}
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
			long beginOffset = (Long)locDBObject.get("s");
			long endOffset = (Long)locDBObject.get("e");
			double probability = (Double)locDBObject.get("p");
			String context = (String)locDBObject.get("c");
			EntityLocation location = new EntityLocation(beginOffset, endOffset, probability, context);
			entity.addLocation(location);
		}

		return entity;
	}

	private void addEntities(String txnId, Node node,
			String type, String key, Collection<Entity<String>> entities)
	{
		BulkWriteOperation bulk = entitiesData.initializeUnorderedBulkOperation();

		String nodeId = node.getNodeId();
		long nodeInternalId = node.getNodeInternalId();
		String nodeVersion = node.getVersionLabel();

		if(entities.size() > 0)
		{
			int expected = entities.size();
			for(Entity<String> nameEntity : entities)
			{
				List<EntityLocation> locations = nameEntity.getLocations();
				List<DBObject> locs = new LinkedList<>();
				for(EntityLocation location : locations)
				{
					long start = location.getBeginOffset();
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
						.start("tx", txnId)
						.add("n", nodeId)
						.add("ni", nodeInternalId)
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
	public void addEntities(String txnId, Node node, Entities entities)
	{
		Collection<Entity<String>> nameEntities = entities.getNames();
		String key = map.get("name");
		addEntities(txnId, node, "name", key, nameEntities);

		Collection<Entity<String>> locationEntities = entities.getLocations();
		key = map.get("location");
		addEntities(txnId, node, "location", key, locationEntities);

		Collection<Entity<String>> orgEntities = entities.getOrgs();
		key = map.get("org");
		addEntities(txnId, node, "org", key, orgEntities);
    }

	@Override
	public Collection<Entity<String>> getNames(Node node)
	{
		String nodeId = node.getNodeId();
		String nodeVersion = node.getVersionLabel();

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

	@Override
	public EntityCounts<String> getEntityCounts(Node node)
	{
		String nodeId = node.getNodeId();
		String nodeVersion = node.getVersionLabel();

		EntityCounts<String> ret = new EntityCounts<>();

		QueryBuilder queryBuilder = QueryBuilder
//				.and("c").is(true)
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
				String nodeId = (String)dbObject.get("n");
				long nodeInternalId = (Long)dbObject.get("ni");
				String nodeVersion = (String)dbObject.get("v");
				Node node = new Node(nodeInternalId, nodeId, nodeVersion);
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

	@Override
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
	public Entities getEntities(Node node, Set<String> types)
	{
		String nodeId = node.getNodeId();
		String nodeVersion = node.getVersionLabel();

		Entities entities = Entities.empty(nodeId, nodeVersion);

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

	private boolean different(String nodeId1, String nodeVersion1, String nodeId2, String nodeVersion2)
	{
		boolean different = false;
		if(!nodeId1.equals(nodeId2))
		{
			different = true;
		}
		else
		{
			if(nodeVersion1 != nodeVersion2)
			{
				different = !EqualsHelper.nullSafeEquals(nodeVersion1, nodeVersion2);
			}
		}

		return different;
	}

	@Override
    public List<Entities> getEntities()
    {
		List<Entities> allEntities = new LinkedList<>();

		QueryBuilder queryBuilder = QueryBuilder
				.start();

		DBObject query = queryBuilder.get();

		DBObject orderBy = BasicDBObjectBuilder
				.start("n", 1)
				.add("v", 1)
				.get();

		DBCursor cursor = entitiesData.find(query).sort(orderBy);
		try
		{
			Entities entities = null;

			for(DBObject dbObject : cursor)
			{
				String nodeId = (String)dbObject.get("n");
				String nodeVersion = (String)dbObject.get("v");
				Entity<String> entity = getEntity(dbObject);
				if(entities == null)
				{
					entities = Entities.empty(nodeId, nodeVersion);
					allEntities.add(entities);
				}
				else
				{
					if(different(nodeId, nodeVersion, entities.getNodeId(), entities.getNodeVersion()))
					{
						entities = Entities.empty(nodeId, nodeVersion);
						allEntities.add(entities);
					}
				}
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

		return allEntities;
    }

	@Override
    public List<Entities> unprocessedEntites()
    {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
	public List<Entities> getEntitiesForTxn(String txnId)
	{
		List<Entities> ret = new LinkedList<>();

		QueryBuilder queryBuilder = QueryBuilder
				.start("tx").is(txnId);

		DBObject query = queryBuilder.get();

		DBCursor cursor = entitiesData.find(query);
		try
		{
			Entities entities = null;

			for(DBObject dbObject : cursor)
			{
				String nodeId = (String)dbObject.get("n");
				String nodeVersion = (String)dbObject.get("v");
				Entity<String> entity = getEntity(dbObject);
				if(entities == null)
				{
					entities = Entities.empty(nodeId, nodeVersion);
					ret.add(entities);
				}
				else
				{
					if(!nodeId.equals(entities.getNodeId()) &&
							!EqualsHelper.nullSafeEquals(nodeVersion, entities.getNodeVersion()))
					{
						entities = Entities.empty(nodeId, nodeVersion);
						ret.add(entities);
					}
				}
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

		return ret;
	}

//	@Override
//	public EntityCounts<String> overlap(Node node)
//	{
//		String nodeId = node.getNodeId();
//		String nodeVersion = node.getNodeVersion();
//
//		Entities entities = getEntities(node, allTypes);
//		EntityCounts<String> entityCounts = getNodeMatches(entities);
//		return entityCounts;
//	}
}
