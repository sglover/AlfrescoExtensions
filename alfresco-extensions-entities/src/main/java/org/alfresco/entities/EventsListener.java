/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.entities.dao.EventsDAO;
import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentGetEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
import org.alfresco.events.node.types.Property;
import org.alfresco.events.node.types.TransactionCommittedEvent;
import org.alfresco.httpclient.AuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class EventsListener
{
/*	private static final Log logger = LogFactory.getLog(EventsListener.class);

	private EventsDAO eventsDAO;
	private EntitiesService entitiesService;
	private UserTrackingService userTrackingService;

    public EventsListener(EventsDAO eventsDAO, EntitiesService entitiesService,
    		UserTrackingService userTrackingService)
    {
	    super();
	    this.eventsDAO = eventsDAO;
	    this.entitiesService = entitiesService;
	    this.userTrackingService = userTrackingService;
    }

	*//** Entry point for consuming messages from the repository. 
	 * @throws IOException 
	 * @throws AuthenticationException *//*
    public synchronized void onEvent(Object message) throws AuthenticationException, IOException
    {
    	logger.debug("message = " + message);

    	if(message instanceof NodeEvent)
    	{
    		NodeEvent nodeEvent = (NodeEvent)message;
    		eventsDAO.addEvent(nodeEvent);
    	}
    	else if(message instanceof NodeContentGetEvent)
		{
			NodeContentGetEvent nodeEvent = (NodeContentGetEvent)message;
			userTrackingService.handleContentGet(nodeEvent);
		}
    	else if(message instanceof TransactionCommittedEvent)
		{
			TransactionCommittedEvent nodeEvent = (TransactionCommittedEvent)message;

			logger.debug("Committing txn " + nodeEvent.getTxnId());

			eventsDAO.txnCommitted(nodeEvent);
			txnCommitted(nodeEvent);
		}
		else
		{
			logger.debug("Event not handled " + message);
		}
    }

    private void getEntitiesForEvent(final NodeContentPutEvent nodeEvent) throws AuthenticationException, IOException
    {
		final String txnId = nodeEvent.getTxnId();
    	final long nodeInternalId = nodeEvent.getNodeInternalId();
    	final String nodeId = nodeEvent.getNodeId();
    	final String nodeVersion = nodeEvent.getVersionLabel();

    	entitiesService.getEntities(txnId, nodeInternalId, nodeId, nodeVersion);
    }

    private void getEntitiesForEvent(final NodeUpdatedEvent nodeEvent) throws IOException
    {
		final String txnId = nodeEvent.getTxnId();
    	final String nodeId = nodeEvent.getNodeId();
    	final String nodeVersion = nodeEvent.getVersionLabel();

		Map<String, Property> propertiesAdded = nodeEvent.getPropertiesAdded();
    	for(Map.Entry<String, Property> entry : propertiesAdded.entrySet())
    	{
    		Property property = entry.getValue();
    		Serializable value = property.getValue();
    		if(value instanceof String)
    		{
    			String content = (String)value;
    			entitiesService.getEntities(txnId, nodeId, nodeVersion, content);
    		}
    	}
    }

    private void getEntitiesForEvent(final NodeAddedEvent nodeEvent) throws IOException
    {
		final String txnId = nodeEvent.getTxnId();
    	final String nodeId = nodeEvent.getNodeId();
    	final String nodeVersion = nodeEvent.getVersionLabel();

		Map<String, Serializable> propertiesAdded = nodeEvent.getNodeProperties();
    	for(Map.Entry<String, Serializable> entry : propertiesAdded.entrySet())
    	{
    		Serializable value = entry.getValue();
    		if(value instanceof String)
    		{
    			String content = (String)value;
    			entitiesService.getEntities(txnId, nodeId, nodeVersion, content);
    		}
    	}
    }

    private void getEntitiesForEvent(Event event) throws IOException, AuthenticationException
    {
    	String eventType = event.getType();
    	switch(eventType)
    	{
    	case NodeAddedEvent.EVENT_TYPE:
    	{
    		getEntitiesForEvent((NodeAddedEvent)event);
    		break;
    	}
    	case NodeContentPutEvent.EVENT_TYPE:
    	{
    		getEntitiesForEvent((NodeContentPutEvent)event);
    		break;
    	}
    	case NodeUpdatedEvent.EVENT_TYPE:
    	{
    		getEntitiesForEvent((NodeUpdatedEvent)event);
    		break;
    	}
    	default:
    		// TODO
    	}
    }

	private void txnCommitted(TransactionCommittedEvent txnCommittedEvent)
	{
		try
		{
			List<Event> events = eventsDAO.getEventsForTxn(txnCommittedEvent.getTxnId());
			for(Event event : events)
			{
				getEntitiesForEvent(event);
			}
			
		}
		catch(IOException e)
		{
			// TOOD
			logger.error(e);
		}
		catch(AuthenticationException e)
		{
			// TOOD
			logger.error(e);
		}
//		entitiesDAO.txnCommitted(event);
//
		entitiesService.calculateSimilarities(txnCommittedEvent.getTxnId());
	}*/
}
