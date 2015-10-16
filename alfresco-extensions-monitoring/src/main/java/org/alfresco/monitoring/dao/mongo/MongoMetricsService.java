/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.monitoring.dao.mongo;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alfresco.monitoring.dao.Metrics;
import org.alfresco.monitoring.dao.MetricsService;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.InitializingBean;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

/**
 * 
 * @author sglover
 *
 */
public class MongoMetricsService implements MetricsService, InitializingBean
{
    /** The collection of users, which can be reused by derived extensions. */
    protected final DBCollection collection;

    public MongoMetricsService(DB db, String collection)
    {
        this.collection = db.getCollection(collection);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        checkIndexes();
    }

    /**
     * Ensure that the MongoDB collection has the required indexes associated with
     * this user bean.
     */
    private void checkIndexes()
    {
        collection.setWriteConcern(WriteConcern.SAFE);
    }

    @Override
    public void addMetrics(DBObject syncMetrics, DBObject subsMetrics, DBObject activeMQStats)
    {
    	long timestamp = System.currentTimeMillis();
    	LocalDateTime time = new LocalDateTime(timestamp, DateTimeZone.UTC);
    	String formattedTime = time.toString();

    	DBObject insert = BasicDBObjectBuilder
    			.start("timestamp", timestamp)
    			.add("time", formattedTime)
    			.add("sync", syncMetrics)
    			.add("subs", subsMetrics)
    			.add("activeMQ", activeMQStats)
    			.get();
    	collection.insert(insert);
    }
    
    @Override
    public Stream<Metrics> getMetrics(int skip, int limit)
    {
    	DBObject query = BasicDBObjectBuilder
    			.start()
    			.get();
    	DBObject orderBy = BasicDBObjectBuilder
    			.start("timestampMS", 1)
    			.get();
    	DBCursor cur = collection.find(query).sort(orderBy).skip(skip).limit(limit);
    	Stream<Metrics> stream = StreamSupport.stream(cur.spliterator(), false)
    		.onClose(() -> cur.close()) // need to close cursor;
    		.map(dbo -> Metrics.fromDBObject(dbo));
    	return stream;
    }
}
