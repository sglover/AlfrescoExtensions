/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.io.IOException;
import java.util.List;

import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;
import org.sglover.alfrescoextensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public class NodeEventHandler
{
    private CacheServer cacheServer;

    public NodeEventHandler(CacheServer cacheServer)
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

    public void nodeAdded(NodeAddedEvent event) throws IOException
    {
        String nodeId = event.getNodeId();
        String versionLabel = event.getVersionLabel();
        if(versionLabel == null)
        {
            versionLabel = "1.0";
        }
        String nodePath = getNodePath(event);
        long nodeInternalId = event.getNodeInternalId();
        long nodeVersion = event.getNodeVersion();

        Node node = Node.build().nodeId(nodeId).nodeInternalId(nodeInternalId).nodeVersion(nodeVersion)
                .versionLabel(versionLabel).nodePath(nodePath);
        cacheServer.nodeAdded(node);
    }

    public void nodeRemoved(NodeRemovedEvent event)
    {
        String nodeId = event.getNodeId();
        String versionLabel = event.getVersionLabel();
        if(versionLabel == null)
        {
            versionLabel = "1.0";
        }
        cacheServer.removeContent(nodeId, versionLabel);
    }

    public void nodeContentUpdated(NodeContentPutEvent event) throws IOException
    {
        long nodeInternalId = event.getNodeInternalId();
        String nodeId = event.getNodeId();
        String versionLabel = event.getVersionLabel();
        long nodeVersion = event.getNodeVersion();
        if(versionLabel == null)
        {
            versionLabel = "1.0";
        }
        List<String> paths = event.getPaths();
        String nodePath = null;
        if(paths != null && paths.size() > 0)
        {
            nodePath = paths.get(0);
        }
        String mimeType = event.getMimeType();
        long size = event.getSize();

        Node node = Node.build()
                .nodeId(nodeId)
                .nodeInternalId(nodeInternalId)
                .versionLabel(versionLabel)
                .nodeVersion(nodeVersion)
                .nodePath(nodePath);
        cacheServer.repoContentUpdated(node, mimeType, size, true);
    }
}
