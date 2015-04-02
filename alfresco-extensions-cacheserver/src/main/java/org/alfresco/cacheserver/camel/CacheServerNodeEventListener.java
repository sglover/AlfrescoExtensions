/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.camel;

import java.io.IOException;
import java.util.List;

import org.alfresco.cacheserver.CacheServer;
import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;


/**
 * 
 * @author sglover
 *
 */
public class CacheServerNodeEventListener
{
	private CacheServer cacheServer;

	public CacheServerNodeEventListener(CacheServer cacheServer)
	{
		super();
		this.cacheServer = cacheServer;
	}

	private String getNodePath(NodeEvent nodeEvent)
	{
		List<String> paths = nodeEvent.getPaths();
		String nodePath = null;
		if(paths != null && paths.size() > 0)
		{
			nodePath = paths.get(0);
		}

		return nodePath;
	}

	private void nodeAdded(NodeAddedEvent event) throws IOException
	{
		String nodeId = event.getNodeId();
		String nodeVersion = event.getVersionLabel();
		String nodePath = getNodePath(event);

		cacheServer.nodeAdded(nodeId, nodeVersion, nodePath);
	}

	private void nodeRemoved(NodeRemovedEvent event)
	{
		String nodeId = event.getNodeId();
		String nodeVersion = event.getVersionLabel();
		cacheServer.removeContent(nodeId, nodeVersion);
	}

	private void nodeContentUpdated(NodeContentPutEvent event) throws IOException
	{
		String nodeId = event.getNodeId();
		String nodeVersion = event.getVersionLabel();
		List<String> paths = event.getPaths();
		String nodePath = null;
		if(paths != null && paths.size() > 0)
		{
			nodePath = paths.get(0);
		}
		String mimeType = event.getMimeType();
		long size = event.getSize();

		cacheServer.contentUpdated(nodeId, nodeVersion, nodePath, mimeType, size);
	}

	public void onChange(Object message) throws IOException
	{
		if(message instanceof NodeAddedEvent)
		{
			NodeAddedEvent nodeAddedEvent = (NodeAddedEvent)message;
			nodeAdded(nodeAddedEvent);
		}
		else if(message instanceof NodeRemovedEvent)
		{
			NodeRemovedEvent nodeRemovedEvent = (NodeRemovedEvent)message;
			nodeRemoved(nodeRemovedEvent);
		}
		else if(message instanceof NodeContentPutEvent)
		{
			NodeContentPutEvent nodeContentPutEvent = (NodeContentPutEvent)message;
			nodeContentUpdated(nodeContentPutEvent);
		}
	}
}
