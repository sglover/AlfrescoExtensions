/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.alfresco.MockAlfrescoApi;
import org.alfresco.MockContentGetter;
import org.alfresco.cacheserver.contentstore.ContentStore;
import org.alfresco.cacheserver.dao.ContentDAO;
import org.alfresco.cacheserver.dao.mongo.MongoContentDAO;
import org.alfresco.cacheserver.entity.GUID;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.Content;
import org.apache.commons.io.IOUtils;
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
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "spring.xml" })
public class TestEdgeServer
{
	private static MongodForTestsFactory mongoFactory;

	private CacheServer edgeServer;
	private ContentDAO contentDAO;
	private ContentStore contentStore;
	private MockContentGetter contentGetter;

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
        final Mongo mongo = mongoFactory.newMongo();
        DB db = mongoFactory.newDB(mongo);

		CacheServerIdentity edgeServerIdentity = new CacheServerIdentity()
		{
			
			@Override
			public int getPort()
			{
				return 8080;
			}
			
			@Override
			public String getId()
			{
				return "";
			}
			
			@Override
			public String getHostname()
			{
				return "localhost";
			}
		};

        long time = System.currentTimeMillis();
        String contentCollectionName = "content" + time;
        String contentUsageCollectionName = "contentUsage" + time;
		this.contentDAO = new MongoContentDAO(db, contentCollectionName, contentUsageCollectionName, edgeServerIdentity);
		this.contentStore = new ContentStore();

		this.contentGetter = new MockContentGetter();
		AlfrescoApi alfrescoApi = new MockAlfrescoApi();

		this.edgeServer = new CacheServer(contentDAO, contentStore, contentGetter, alfrescoApi,
				edgeServerIdentity, null);
	}

	@Test
	public void test1() throws Exception
	{
		long nodeInternalId = 1l;
		String nodeId = GUID.generate();
		String nodeVersion = "1";
		String nodePath = "/1/2/3";
		byte[] bytes = "test".getBytes("UTF-8");
		String expectedMimeType = "text/plain";
		Long expectedSize = new Long(bytes.length);
		InputStream nodeContent = new ByteArrayInputStream(bytes);
		InputStream contentIn = null;
		try
		{
	
			contentGetter.addTestContent(nodeInternalId, nodeId, nodeVersion, "test", expectedMimeType);

//			edgeServer.nodeAdded(nodeId, nodeVersion, nodePath);
			edgeServer.contentUpdated(nodeId, nodeVersion, nodePath, expectedMimeType, expectedSize);
	
			Content content = edgeServer.getByNodePath(nodePath, "admin");
			contentIn = content.getIn();
			assertNotNull(contentIn);
			assertEquals(expectedMimeType, content.getMimeType());
			assertEquals(expectedSize, content.getSize());
			InputStream expectedNodeContent = new ByteArrayInputStream(bytes);
			IOUtils.contentEquals(expectedNodeContent, contentIn);
		}
		finally
		{
			if(nodeContent != null)
			{
				nodeContent.close();
			}
			if(contentIn != null)
			{
				contentIn.close();
			}
		}
	}
}
