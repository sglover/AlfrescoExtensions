/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import org.alfresco.entities.EntitiesDAO;
import org.alfresco.entities.MongoEntitiesDAO;
import org.alfresco.entities.Spark1;
import org.alfresco.service.common.mongo.MongoDbFactory;
import org.alfresco.services.nlp.Entities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

/**
 * 
 * @author sglover
 *
 */
public class SparkServiceTest
{
    private static MongodForTestsFactory mongoFactory;

    private EntitiesDAO entitiesDAO;
//    private SparkService sparkService;
    private Spark1 spark1;

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
	
	@Before
	public void before() throws Exception
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
            factory.setDbName("entitiesTest");
        }
        final DB db = factory.createInstance();

        MongoEntitiesDAO dao = new MongoEntitiesDAO();
        dao.setDb(db);
        dao.setEntitiesCollectionName("entities" + System.currentTimeMillis());
        dao.init();
        this.entitiesDAO = dao;

//        this.sparkService = new SparkService();
//        this.sparkService.setEntitiesDAO(entitiesDAO);
//        this.sparkService.init();

        this.spark1 = new Spark1();
        this.spark1.init();
	}

	@After
	public void after()
	{
//		sparkService.shutdown();
		spark1.shutdown();
	}

//	@Test
//	public void test1() throws Exception
//	{
//		Nodes nodes = sparkService.matchingNodes(1l, 1l);
//		System.out.println(nodes);
//	}

	@Test
	public void test2() throws Exception
	{
		Entities entities = Entities
				.empty()
				.addName("Steve Glover", "Steve Glover works for Alfresco", 1.0)
				.addOrg("Alfresco", "Steve Glover works for Alfresco", 1.0);

		spark1.matchNodes(entities);

//		Nodes nodes = spark1.matchNodes(entities);
//		System.out.println(nodes);
	}
}
