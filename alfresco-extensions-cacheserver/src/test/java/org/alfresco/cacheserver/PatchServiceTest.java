/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.alfresco.cacheserver.content.ContentUpdater;
import org.alfresco.cacheserver.content.ContentUpdater.OperationType;
import org.alfresco.contentstore.AbstractContentStore;
import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.contentstore.dao.NodeInfo;
import org.alfresco.contentstore.patch.PatchService;
import org.alfresco.util.GUID;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.PatchDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.multipart.MultiPart;

/**
 * 
 * @author sglover
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-spring.xml" })
public class PatchServiceTest
{
    @Autowired
    private ContentDAO contentDAO;

    @Autowired
	private ContentUpdater contentUpdater;

    @Autowired
    private PatchService patchService;

    @Autowired
    private AbstractContentStore contentStore;

	@Before
	public void before() throws Exception
	{
//        final MongoDbFactory factory = new MongoDbFactory();
//        boolean useEmbeddedMongo = ("true".equals(System.getProperty("useEmbeddedMongo")) ? true : false);
//        if (useEmbeddedMongo)
//        {
//            final Mongo mongo = mongoFactory.newMongo();
//            factory.setMongo(mongo);
//        }
//        else
//        {
//            factory.setMongoURI("mongodb://127.0.0.1:27017");
//            factory.setDbName("test");
//        }
//        final DB db = factory.createInstance();
//
//        CacheServerIdentity cacheServerIdentity = new CacheServerIdentity()
//		{
//			@Override
//			public int getPort()
//			{
//				return 0;
//			}
//			
//			@Override
//			public String getId()
//			{
//				return GUID.generate();
//			}
//			
//			@Override
//			public String getHostname()
//			{
//				return GUID.generate();
//			}
//		};
//		this.contentStore = new ContentStore();
//		long time = System.currentTimeMillis();
//		this.contentDAO = new MongoContentDAO(db, "content" + time, "contentUsage" + time, cacheServerIdentity);
//		this.checksumDAO = new MongoChecksumDAO(db, "checksums" + time);
//		this.checksumService = new ChecksumServiceImpl(checksumDAO);
//		this.patchService = new PatchServiceImpl(contentDAO, checksumService, contentStore);
	}

	private void assertFileEquals(String contentPath1, String contentPath2) throws IOException
	{
	    File file1 = new File(contentPath1);
	    if(!file1.exists())
	    {
	        fail();
	    }

	    File file2 = new File(contentPath2);
	    if(!file2.exists())
	    {
	        fail();
	    }

        try(FileInputStream fis1 = new FileInputStream(file1); FileChannel channel1 = fis1.getChannel(); )
        {
            try(FileInputStream fis2 = new FileInputStream(file2); FileChannel channel2 = fis2.getChannel())
            {
    	        ByteBuffer bb1 = ByteBuffer.allocate(1024);
    	        channel1.read(bb1);
    	        ByteBuffer bb2 = ByteBuffer.allocate(1024);
    	        channel2.read(bb2);
    	        bb1.flip();
    	        bb2.flip();
    	        assertEquals(0, bb1.compareTo(bb2));
            }
	    }
	}

	@Test
	public void test1() throws Exception
	{
	    String nodeId = GUID.generate();
        long nodeVersion1 = 1;
        long nodeVersion2 = 2;

	    Node node1 = Node.build().nodeId(nodeId).nodeVersion(nodeVersion1);
//	    InputStream in1 = IOUtils.toInputStream("Hello world");
	    String mimeType = "text/plain";
	    long size1 = "Hello world".getBytes().length;
	    contentUpdater.updateContent(node1, OperationType.Sync, OperationType.Sync, mimeType, size1);
	    NodeInfo node1Info = contentDAO.getByNodeId(nodeId, nodeVersion1, true);
	    String contentPath1 = node1Info.getContentPath();

        Node node2 = Node.build().nodeId(nodeId).nodeVersion(nodeVersion2);
//        InputStream in2 = IOUtils.toInputStream("Hello world again");
        long size2 = "Hello world again".getBytes().length;
        contentUpdater.updateContent(node2, OperationType.Sync, OperationType.Sync, mimeType, size2);
        NodeInfo node2Info = contentDAO.getByNodeId(nodeId, nodeVersion2, true);
        String contentPath2 = node2Info.getContentPath();

        PatchDocument patchDocument = patchService.getPatch(nodeId, nodeVersion2);
        MultiPart entity = patchService.getPatchEntity(patchDocument);
        System.out.println("------");
        //entity.writeTo(System.out)
        System.out.println(entity);

        String newContentPath = contentStore.applyPatch(patchDocument, contentPath1);
        InputStream in3 = new FileInputStream(new File(newContentPath));
        IOUtils.copy(in3, System.out);

        assertFileEquals(contentPath2, newContentPath);

        MultiPart multiPart = patchService.getMultiPart(patchDocument);
        System.out.println(multiPart.toString());

        PatchDocument patchDocument1 = patchService.getPatch(multiPart);
        assertEquals(patchDocument, patchDocument1);
	}
}