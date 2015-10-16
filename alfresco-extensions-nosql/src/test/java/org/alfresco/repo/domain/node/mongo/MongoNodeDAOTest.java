/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.domain.node.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.alfresco.repo.domain.node.ConcurrentModificationException;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.TransactionSupport;
import org.alfresco.service.common.mongo.MongoDbFactory;
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
public class MongoNodeDAOTest
{
    private static MongodForTestsFactory mongoFactory;

    private NodeDAO nodeDAO;

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
        long time = System.currentTimeMillis();

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
        final DB db = factory.createInstance();

        System.out.println("node = nodes" + time);

        MongoNodeDAO nodeDAO = new MongoNodeDAO();
        nodeDAO.setDb(db);
        nodeDAO.setNodesCollectionName("nodes" + time);
        nodeDAO.init();
        this.nodeDAO = nodeDAO;

        TransactionSupport.addListener(nodeDAO);
	}

	@Test
	public void testCommit() throws Exception
	{
		TransactionSupport.begin();
		NodeEntity nodeEntity = nodeDAO.newNode(1, 1, 2);
		TransactionSupport.commit();

		NodeEntity nodeEntity1 = nodeDAO.getByVersionLabel(2, "1.0");
		assertEquals(nodeEntity, nodeEntity1);
	}

	@Test
	public void testRollback() throws Exception
	{
		TransactionSupport.begin();
		nodeDAO.newNode(1, 1, 2);
		TransactionSupport.rollback();

		NodeEntity nodeEntity1 = nodeDAO.getByVersionLabel(2, "1.0");
		assertNull(nodeEntity1);
	}

	@Test
	public void test1() throws Exception
	{
		TransactionSupport.begin();
		NodeEntity nodeEntity = nodeDAO.newNode(1, 1, 2); // v1
		TransactionSupport.commit();

		TransactionSupport.begin();
		nodeDAO.updateNode(2, "2.0"); // v2
		TransactionSupport.rollback();

		TransactionSupport.begin();
		NodeEntity nodeEntity2 = nodeDAO.updateNode(2, "2.0"); // v2
		TransactionSupport.commit();

		NodeEntity nodeEntity3 = nodeDAO.getByVersionLabel(2, "1.0");
		assertEquals(nodeEntity, nodeEntity3);

		NodeEntity nodeEntity4 = nodeDAO.getByVersionLabel(2, "2.0");
		assertEquals(nodeEntity2, nodeEntity4);
	}

	@Test
	public void testConcurrentUpdate() throws Exception
	{
		TransactionSupport.begin();
		nodeDAO.newNode(1, 1, 2); // v1
		TransactionSupport.commit();

		TransactionSupport.begin();
		nodeDAO.updateNode(2, "2.0"); // v1
		TransactionSupport.commit();

		TransactionSupport.begin();
		try
		{
			nodeDAO.updateNode(2, "3.0"); // v1
			fail();
		}
		catch(ConcurrentModificationException e)
		{
			// ok
		}
		TransactionSupport.commit();
	}
}
