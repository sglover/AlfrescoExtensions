/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import org.alfresco.entities.dao.EntitiesDAO;
import org.alfresco.entities.dao.mongo.MongoEntitiesDAO;
import org.alfresco.extensions.common.Node;
import org.alfresco.service.common.mongo.MongoDbFactory;
import org.alfresco.services.nlp.Entities;
import org.alfresco.util.GUID;
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
public class EntitiesDAOTest
{
    private static MongodForTestsFactory mongoFactory;

    private EntitiesDAO entitiesDAO;

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

        this.entitiesDAO = new MongoEntitiesDAO(db, "entities" + System.currentTimeMillis());
	}

	@Test
	public void test1() throws Exception
	{
		Entities entities1 = Entities
				.empty()
				.addName("Steve Glover", "Steve Glover works for Alfresco", 1.0)
				.addOrg("Alfresco", "Steve Glover works for Alfresco", 1.0);

		Entities entities2 = Entities
				.empty()
				.addName("Steve Glover", "Steve Glover works for Alfresco", 1.0);

		Entities entities3 = Entities
				.empty()
				.addOrg("Alfresco", "Steve Glover works for Alfresco", 1.0);

		String txnId = GUID.generate();
		String node1Id = "1";
		String node1Version = "1";
		String node2Id = "2";
		String node2Version = "2";
		String node3Id = "3";
		String node3Version = "3";

		entitiesDAO.addEntities(txnId, Node.build().nodeId(node1Id).versionLabel(node1Version), entities1);
		entitiesDAO.addEntities(txnId, Node.build().nodeId(node2Id).versionLabel(node2Version), entities2);
		entitiesDAO.addEntities(txnId, Node.build().nodeId(node3Id).versionLabel(node3Version), entities3);

//		EntityCounts<String> counts = entitiesDAO.overlap(node1InternalId, node1Version);
//		System.out.println(counts);
	}
}
