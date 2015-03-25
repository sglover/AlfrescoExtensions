/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateIndex
{
    private ElasticSearchClient client;

    @Before
    public void before()
    {
    	this.client = new ElasticSearchClient("localhost", 9300, "alfresco");
    }

    @After
    public void after()
    {
        client.shutdown();
    }

	@Test
	public void reCreateIndex() throws Exception
	{
        XContentBuilder contentMapping = jsonBuilder().startObject()
                .startObject(IndexType.content.getName())
                  .startObject("properties")
                    .startObject("_n")
                    	.field("type", "entities")
                    	.field("store",true)
                    .endObject()
                    .startObject("id")
                    	.field("type", "string")
                    	.field("store",true)
                    .endObject()
                    .startObject("t")
                    	.field("type", "string")
                    	.field("store", true)
                    .endObject()
                    .startObject("n")
                    	.field("type", "long")
                    	.field("store", true)
                    .endObject()
                    .startObject("v")
                    	.field("type", "long")
                    	.field("store",true)
                    .endObject()
                    .startObject("c")
                    	.field("type", "string")
                    	.field("index", "analyzed")
                    	.field("store", true)
                    .endObject()
                  .endObject()
             .endObject();

        XContentBuilder eventMapping = jsonBuilder().startObject()
                .startObject(IndexType.content.getName())
                  .startObject("properties")
                    .startObject("t")
                    	.field("type", "string")
                    	.field("store",true)
                    .endObject()
                    .startObject("u")
                    	.field("type", "string")
                    	.field("store",true)
                    .endObject()
                    .startObject("ti")
                    	.field("type", "date")
                    	.field("store", true)
                    .endObject()
                    .startObject("id")
                    	.field("type", "long")
                    	.field("store", true)
                    .endObject()
                    .startObject("v")
                    	.field("type", "long")
                    	.field("store",true)
                    .endObject()
                    .startObject("n")
                    	.field("type", "string")
                    	.field("store", true)
                    .endObject()
                  .endObject()
             .endObject();

        Map<String, XContentBuilder> mappings = new HashMap<>();
        mappings.put(IndexType.content.getName(), contentMapping);
        mappings.put(IndexType.event.getName(), eventMapping);
		client.createIndex("alfresco", true, mappings);
	}
}
