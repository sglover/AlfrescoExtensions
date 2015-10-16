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
import org.alfresco.service.synchronization.api.SyncEvent;
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

	protected ElasticSearchMonitoringIndexer elasticSearchMonitoringIndexer;
	protected ElasticSearchIndexer elasticSearchIndexer;
	protected ExecutorService executorService;

    public EventListener(ElasticSearchIndexer elasticSearchIndexer, ElasticSearchMonitoringIndexer elasticSearchMonitoringIndexer)
    {
	    super();
	    this.elasticSearchIndexer = elasticSearchIndexer;
	    this.elasticSearchMonitoringIndexer = elasticSearchMonitoringIndexer;
    }

    public void onChange(Object message)
    {
        try
        {
        	logger.debug("message: " + message);

            if (message instanceof SyncEvent)
            {
            	SyncEvent event = (SyncEvent)message;
            	elasticSearchMonitoringIndexer.indexSync(event);
            }
            else if (message instanceof NodeEvent)
            {
            	NodeEvent nodeEvent = (NodeEvent)message;

	            if (nodeEvent instanceof NodeAddedEvent)
	            {
	                NodeAddedEvent event = (NodeAddedEvent)nodeEvent;
	                elasticSearchIndexer.indexNode(event);
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
	            	elasticSearchIndexer.reindexNode(event);
//					elasticSearch.indexEntities(nodeEvent);
	            }
	            else if (nodeEvent instanceof NodeContentPutEvent)
	            {
	            	NodeContentPutEvent event = (NodeContentPutEvent)nodeEvent;
	            	elasticSearchIndexer.indexNode(event);
	            	elasticSearchIndexer.indexContent(event);
//					elasticSearch.indexEntities(nodeEvent);
	            }
	
	            elasticSearchIndexer.indexEvent(nodeEvent);
            }
            else if (message instanceof org.alfresco.events.types.NodeEvent)
            {
            	org.alfresco.events.types.NodeEvent nodeEvent = (org.alfresco.events.types.NodeEvent)message;

	            if (nodeEvent instanceof org.alfresco.events.types.NodeAddedEvent)
	            {
	            	org.alfresco.events.types.NodeAddedEvent event = (org.alfresco.events.types.NodeAddedEvent)nodeEvent;
	            	elasticSearchIndexer.indexNode(event);
	            }
	            else if (nodeEvent instanceof org.alfresco.events.types.NodeRemovedEvent)
	            {
	            	org.alfresco.events.types.NodeRemovedEvent event = (org.alfresco.events.types.NodeRemovedEvent)nodeEvent;
//	            	elasticSearch.unindexNode(event);
//	            	elasticSearch.unindexContent(event);
	            	// TODO
	            }
	            else if (nodeEvent instanceof org.alfresco.events.types.NodeUpdatedEvent)
	            {
	            	org.alfresco.events.types.NodeUpdatedEvent event = (org.alfresco.events.types.NodeUpdatedEvent)nodeEvent;
	            	elasticSearchIndexer.reindexNode(event);
//					elasticSearch.indexEntities(nodeEvent);
	            }
	            else if (nodeEvent instanceof org.alfresco.events.types.NodeContentPutEvent)
	            {
	            	org.alfresco.events.types.NodeContentPutEvent event = (org.alfresco.events.types.NodeContentPutEvent)nodeEvent;
	            	elasticSearchIndexer.indexNode(event);
	            	elasticSearchIndexer.indexContent(event);
//					elasticSearch.indexEntities(nodeEvent);
	            }
	
	            elasticSearchIndexer.indexEvent(nodeEvent);
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
