/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeleteIndexes
{
    private ElasticSearchClient client;

//    private String idxName = IndexType.node.getName();

    @Before
    public void before()
    {
    	this.client = new ElasticSearchClient("elk", "alfresco");
//        this.client = new TransportClient()
//        .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

//        this.node = nodeBuilder().node();
//        this.client = node.client();
    }

    @After
    public void after()
    {
        client.shutdown();
    }

	@Test
	public void deleteIndexes() throws Exception
	{
		client.deleteIndex("test");
		client.deleteIndex("node");
		client.deleteIndex("alfresco");
		client.deleteIndex("content");
		client.deleteIndex("_river");
	}
}
