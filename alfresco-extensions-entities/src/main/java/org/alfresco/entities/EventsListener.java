/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.io.IOException;

import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentGetEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
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
	private static final Log logger = LogFactory.getLog(EventsListener.class);

	private EntitiesService entitiesService;
	private UserTrackingService userTrackingService;

    public EventsListener(EntitiesService entitiesService, UserTrackingService userTrackingService)
    {
	    super();
	    this.entitiesService = entitiesService;
	    this.userTrackingService = userTrackingService;
    }

	/** Entry point for consuming messages from the repository. 
	 * @throws IOException 
	 * @throws AuthenticationException */
    public synchronized void onChange(Object message) throws AuthenticationException, IOException
    {
		if(message instanceof NodeContentPutEvent || message instanceof NodeAddedEvent ||
				message instanceof NodeUpdatedEvent)
		{
			NodeEvent nodeEvent = (NodeEvent)message;
			entitiesService.getEntities(nodeEvent);
		}
		else if(message instanceof NodeContentGetEvent)
		{
			NodeContentGetEvent nodeEvent = (NodeContentGetEvent)message;
			userTrackingService.handleContentGet(nodeEvent);
		}
		else
		{
			logger.debug("Event not handled " + message);
		}
    }
}
