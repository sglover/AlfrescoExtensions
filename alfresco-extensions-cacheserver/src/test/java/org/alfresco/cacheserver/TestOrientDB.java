/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import static org.junit.Assert.assertEquals;

import org.alfresco.cacheserver.dao.orient.OrientContentDAO;
import org.alfresco.cacheserver.entity.GUID;
import org.alfresco.cacheserver.entity.NodeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class TestOrientDB
{
	private OrientContentDAO contentDAO;

	@Before
	public void before() throws Exception
	{
		this.contentDAO = new OrientContentDAO("localhost", "content", "admin", "admin", true);
		this.contentDAO.init();
	}
	
	@After
	public void after() throws Exception
	{
		this.contentDAO.shutdown();
	}

	@Test
	public void test1() throws Exception
	{
		String nodeId = GUID.generate();
		String nodeVersion = "1";
		String nodePath = "/1/2/3";
		String contentPath = "/1/2/3";
		byte[] bytes = "test".getBytes("UTF-8");
		String mimeType = "text/plain";
		Long size = new Long(bytes.length);

		NodeInfo nodeInfo = new NodeInfo(nodeId, nodeVersion, nodePath, contentPath, mimeType, size);
//		contentDAO.addNode(nodeInfo);
		contentDAO.updateNode(nodeInfo);

		NodeInfo nodeInfo1 = contentDAO.getByNodeId(nodeId, nodeVersion);
		assertEquals(contentPath, nodeInfo1.getContentPath());
		assertEquals(nodePath, nodeInfo1.getNodePath());
		assertEquals(nodeId, nodeInfo1.getNodeId());
		assertEquals(nodeVersion, nodeInfo1.getNodeVersion());
		assertEquals(mimeType, nodeInfo1.getMimeType());
		assertEquals(size, nodeInfo1.getSize());
	}
}
