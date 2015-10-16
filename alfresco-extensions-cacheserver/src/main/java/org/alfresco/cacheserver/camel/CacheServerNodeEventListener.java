/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.camel;

import java.io.IOException;

import org.alfresco.cacheserver.NodeEventHandler;
import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;

/**
 * 
 * @author sglover
 *
 */
public class CacheServerNodeEventListener
{
	private NodeEventHandler nodeEventHandler;

	public CacheServerNodeEventListener(NodeEventHandler nodeEventHandler)
	{
		super();
		this.nodeEventHandler = nodeEventHandler;
	}

	public void onChange(Object message) throws IOException
	{
		if(message instanceof NodeAddedEvent)
		{
			NodeAddedEvent nodeAddedEvent = (NodeAddedEvent)message;
			nodeEventHandler.nodeAdded(nodeAddedEvent);
		}
		else if(message instanceof NodeRemovedEvent)
		{
			NodeRemovedEvent nodeRemovedEvent = (NodeRemovedEvent)message;
			nodeEventHandler.nodeRemoved(nodeRemovedEvent);
		}
		else if(message instanceof NodeContentPutEvent)
		{
			NodeContentPutEvent nodeContentPutEvent = (NodeContentPutEvent)message;
			nodeEventHandler.nodeContentUpdated(nodeContentPutEvent);
		}
//		else if(message instanceof NodeContentGetEvent)
//		{
//			NodeContentGetEvent nodeContentGetEvent = (NodeContentGetEvent)message;
//			nodeContentRead(nodeContentGetEvent);
//		}
	}
}
