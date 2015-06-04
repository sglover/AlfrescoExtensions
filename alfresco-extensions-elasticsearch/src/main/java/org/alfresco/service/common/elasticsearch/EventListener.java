/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class EventListener
{
    private static final Log logger = LogFactory.getLog(EventListener.class);

	protected ElasticSearchIndexer elasticSearch;
	protected ExecutorService executorService;

    public EventListener(ElasticSearchIndexer elasticSearch)
    {
	    super();
	    this.elasticSearch = elasticSearch;
    }

    public void onChange(Object message)
    {
        try
        {
        	logger.debug("message: " + message);

            if (message instanceof NodeEvent)
            {
            	NodeEvent nodeEvent = (NodeEvent)message;

	            if (nodeEvent instanceof NodeAddedEvent)
	            {
	                NodeAddedEvent event = (NodeAddedEvent)nodeEvent;
	                elasticSearch.indexNode(event);
	            }
	            else if (nodeEvent instanceof NodeRemovedEvent)
	            {
	            	NodeRemovedEvent event = (NodeRemovedEvent)nodeEvent;
//	            	elasticSearch.unindexNode(event);
//	            	elasticSearch.unindexContent(event);
	            	// TODO
	            }
	            else if (nodeEvent instanceof NodeUpdatedEvent)
	            {
	            	NodeUpdatedEvent event = (NodeUpdatedEvent)nodeEvent;
	            	elasticSearch.reindexNode(event);
//					elasticSearch.indexEntities(nodeEvent);
	            }
	            else if (nodeEvent instanceof NodeContentPutEvent)
	            {
	            	NodeContentPutEvent event = (NodeContentPutEvent)nodeEvent;
	            	elasticSearch.indexNode(event);
	            	elasticSearch.indexContent(event);
//					elasticSearch.indexEntities(nodeEvent);
	            }
	
	            if(nodeEvent instanceof NodeEvent)
	            {
	            	NodeEvent event = (NodeEvent)message;
	            	elasticSearch.indexEvent(event);
	            }
            }
            else
            {
            	
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
