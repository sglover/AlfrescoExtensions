/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import org.alfresco.service.common.mongo.MongoDbFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

/**
 * 
 * @author sglover
 *
 */
public class AbstractEntitiesTest
{
    private static MongodForTestsFactory mongoFactory;

    protected DB db;

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
    }
    
    @AfterClass
    public static void afterClass()
    {
        mongoFactory.shutdown();
    }

	public void init() throws Exception
	{
        final MongoDbFactory factory = new MongoDbFactory();
        boolean useEmbeddedMongo = ("true".equals(System.getProperty("useEmbeddedMongo")) ? true : false);
        if (useEmbeddedMongo)
        {
            final Mongo mongo = mongoFactory.newMongo();
            factory.setMongo(mongo);
        }
        else
        {
            factory.setMongoURI("mongodb://127.0.0.1:27017");
            factory.setDbName("test");
        }
        this.db = factory.createInstance();
	}
}
