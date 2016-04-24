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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.TransactionCommittedEvent;
import org.alfresco.events.serialize.EventsSerializer;
import org.alfresco.service.common.mongo.AbstractMongoDAO;
import org.sglover.entities.dao.EventsDAO;

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
public class MongoEventsDAO extends AbstractMongoDAO implements EventsDAO
{
	private DB db;
	private String eventsCollectionName;
	private DBCollection eventData;

	private EventsSerializer eventsSerializer;

	public MongoEventsDAO(DB db, String eventsCollectionName, EventsSerializer eventsSerializer)
	{
		this.eventsCollectionName = eventsCollectionName;
		this.db = db;
		this.eventsSerializer = eventsSerializer;
		init();
	}
	
	public void dropEvents()
	{
		eventData.drop();
	}
	
	public void drop()
	{
		dropEvents();
	}

	private void init()
	{
        if (db == null)
        {
            throw new AlfrescoRuntimeException("Mongo DB must not be null");
        }

		this.eventData = getCollection(db, eventsCollectionName, WriteConcern.ACKNOWLEDGED);

		{
	        DBObject keys = BasicDBObjectBuilder
	                .start("ic", 1)
	                .add("tx", 1)
	                .get();
	        this.eventData.ensureIndex(keys, "main", false);
		}
	}

	@Override
	public void addEvent(Event nodeEvent)
	{
		DBObject dbObject = eventsSerializer.serialize(nodeEvent);

		eventData.insert(dbObject);
	}

	@Override
	public void txnCommitted(TransactionCommittedEvent event)
	{
		{
			DBObject query = QueryBuilder
					.start("txnId").is(event.getTxnId())
					.get();
	
			DBObject update = BasicDBObjectBuilder
					.start("$set",
							BasicDBObjectBuilder
								.start("ic", true)
								.get())
					.get();
	
			WriteResult result = eventData.update(query, update, false, true);
			checkResult(result);
		}
	}

	@Override
	public List<Event> getEventsForTxn(String txnId)
	{
		List<Event> ret = new LinkedList<>();

		DBObject query = QueryBuilder
			.start("ic").is(true)
			.and("txnId").is(txnId)
			.get();

		DBCursor cursor = eventData.find(query);
		try
		{
			for(DBObject dbObject : cursor)
			{
				Event event = eventsSerializer.deSerialize(dbObject);
				ret.add(event);
			}
		}
		finally
		{
			cursor.close();
		}

		return ret;
	}
}
