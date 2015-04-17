/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.entities.EntitiesService;
import org.alfresco.entities.EntitiesServiceImpl;
import org.alfresco.entities.dao.mongo.MongoEntitiesDAO;
import org.alfresco.entities.values.Node;
import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.services.nlp.EntityExtracter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class MinHashTest extends AbstractEntitiesTest
{
	private static Log logger = LogFactory.getLog(MinHashTest.class.getName());

	private ExecutorService executorService;
	private MockContentGetter contentGetter;
	private EntityExtracter entityExtracter;
	private EntitiesService entitiesService;

	@Before
	public void before() throws Exception
	{
		super.init();
		this.executorService = Executors.newFixedThreadPool(10);
		this.contentGetter = MockContentGetter.start();
		this.entityExtracter =
				EntityExtracter.stanfordNLPEntityExtracter(contentGetter, executorService);
//		this.entitiesGetter = new EntitiesGetter()
//		{
//			@Override
//			public Entities entities(long nodeInternalId, Set<EntityType> entityTypes)
//			{
//				Entities entities = null;
//
//				try
//				{
//					entities = entityExtracter.getEntities(nodeInternalId);
//				}
//				catch(IOException | AuthenticationException e)
//				{
//					logger.error(e);
//				}
//
//				return entities;
//			}
//
//			@Override
//			public Entities entities(String nodeId, Set<EntityType> entityTypes)
//			{
//				throw new UnsupportedOperationException();
//			}
//		};
//		this.entitiesService = new EntitiesService(entitiesGetter);

		long time = System.currentTimeMillis();

		MongoEntitiesDAO entitiesDAO = new MongoEntitiesDAO();
		entitiesDAO.setDb(db);
		entitiesDAO.setEntitiesCollectionName("entities" + time);
//		entitiesDAO.setSimilarityCollectionName("similarity" + time);
		entitiesDAO.init();

		EntitiesServiceImpl entitiesService = new EntitiesServiceImpl();
		entitiesService.setEntitiesDAO(entitiesDAO);
		entitiesService.setEntityExtracter(entityExtracter);
		this.entitiesService = entitiesService;
	}

//	@Test
	public void test1() throws Exception
	{
		contentGetter
			.addTestContent(1, "1", "1", "I like San Francisco but I like Hawaii more", "text/plain")
			.addTestContent(2, "2", "2", "I like San Francisco but I like Hawaii more", "text/plain");

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setNodeInternalId(1);
			nodeEvent.setNodeId("1");
			nodeEvent.setVersionLabel("1");
			nodeEvent.setNodeType("cm:content");
			entitiesService.getEntities(nodeEvent);
		}

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setNodeInternalId(2);
			nodeEvent.setNodeId("2");
			nodeEvent.setVersionLabel("2");
			nodeEvent.setNodeType("cm:content");
			entitiesService.getEntities(nodeEvent);
		}

		Node node1 = Node.build().nodeId("1").nodeVersion("1");
		Node node2 = Node.build().nodeId("2").nodeVersion("2");
		double similarity = entitiesService.getSimilarity(node1, node2);
		assertEquals(1.0, similarity, 0.00001);
	}

//	@Test
	public void test2() throws Exception
	{
		contentGetter
			.addTestContent(1, "1", "1", new File("/Users/sglover/Documents/PublicApiBenchmark.txt"), "text/plain")
			.addTestContent(2, "2", "2", new File("/Users/sglover/Documents/UsingVoltDB.txt"), "text/plain");

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setNodeInternalId(1);
			nodeEvent.setNodeId("1");
			nodeEvent.setVersionLabel("1");
			nodeEvent.setNodeType("cm:content");
			entitiesService.getEntities(nodeEvent);
		}

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setNodeInternalId(2);
			nodeEvent.setNodeId("2");
			nodeEvent.setVersionLabel("2");
			nodeEvent.setNodeType("cm:content");
			entitiesService.getEntities(nodeEvent);
		}

		Node node1 = Node.build().nodeId("1").nodeVersion("1");
		Node node2 = Node.build().nodeId("2").nodeVersion("2");
		double similarity = entitiesService.getSimilarity(node1, node2);
		System.out.println(similarity);
//		assertEquals(1.0, similarity, 0.00001);
	}

//	@Test
//	public void test3() throws Exception
//	{
//		contentGetter
//			.addTestContent(1, "1", "1", new File("/Users/sglover/Documents/UsingVoltDB.txt"), "text/plain")
//			.addTestContent(2, "2", "2", new File("/Users/sglover/Documents/UsingVoltDB.txt"), "text/plain");
//
//		{
//			NodeAddedEvent nodeEvent = new NodeAddedEvent();
//			nodeEvent.setNodeInternalId(1);
//			nodeEvent.setNodeId("1");
//			nodeEvent.setVersionLabel("1");
//			nodeEvent.setNodeType("cm:content");
//			entitiesService.getEntities(nodeEvent);
//		}
//
//		{
//			NodeAddedEvent nodeEvent = new NodeAddedEvent();
//			nodeEvent.setNodeInternalId(2);
//			nodeEvent.setNodeId("2");
//			nodeEvent.setVersionLabel("2");
//			nodeEvent.setNodeType("cm:content");
//			entitiesService.getEntities(nodeEvent);
//		}
//
//		Node node1 = Node.build().nodeId("1").nodeVersion("1");
//		Node node2 = Node.build().nodeId("2").nodeVersion("2");
//		double similarity = entitiesService.getSimilarity(node1, node2);
//		System.out.println(similarity);
////		assertEquals(1.0, similarity, 0.00001);
//	}

	@Test
	public void test4() throws Exception
	{
		contentGetter
			.addTestContent(1, "1", "1", new File("/Users/sglover/Documents/Performance Appraisal Review-1-2011.txt"), "text/plain")
			.addTestContent(2, "2", "2", new File("/Users/sglover/Documents/nosql_brownbag.txt"), "text/plain");

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setNodeInternalId(1);
			nodeEvent.setNodeId("1");
			nodeEvent.setVersionLabel("1");
			nodeEvent.setNodeType("cm:content");
			entitiesService.getEntities(nodeEvent);
		}

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setNodeInternalId(2);
			nodeEvent.setNodeId("2");
			nodeEvent.setVersionLabel("2");
			nodeEvent.setNodeType("cm:content");
			entitiesService.getEntities(nodeEvent);
		}

		Node node1 = Node.build().nodeId("1").nodeVersion("1");
		Node node2 = Node.build().nodeId("2").nodeVersion("2");
		double similarity = entitiesService.getSimilarity(node1, node2);
		System.out.println(similarity);
//		assertEquals(1.0, similarity, 0.00001);
	}

//	@Test
//	public void test1() throws Exception
//	{
//		Set<String> set1 = new HashSet<String>();
//		set1.add("FRANCISCO");
//		set1.add("MISSION");
//		set1.add("SAN");
//
//		Set<String> set2 = new HashSet<String>();
//		set2.add("FRANCISCO");
//		set2.add("MISSION");
//		set2.add("SIN");
//		set2.add("USA");
//		set2.add("SA");
//		set2.add("US");
//		set2.add("UA");
//
//		MinHashImpl<String> minHash = new MinHashImpl<String>(set1.size()+set2.size());
//		System.out.println(minHash.similarity(set1, set2));
//	}
}
