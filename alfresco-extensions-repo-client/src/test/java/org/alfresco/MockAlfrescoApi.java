/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.NodeId;

/**
 * 
 * @author sglover
 *
 */
public class MockAlfrescoApi implements AlfrescoApi
{
	private Map<String, NodeId> nodesByPath = new HashMap<>();
	private Map<NodeId, String> nodePathsById = new HashMap<>();

	public void addNode(String nodeId, String nodeVersion, String nodePath)
	{
		NodeId fullNodeId = new NodeId(nodeId, nodeVersion);
		nodesByPath.put(nodePath, fullNodeId);
		nodePathsById.put(fullNodeId, nodePath);
	}

	@Override
    public NodeId getObjectIdForNodePath(String nodePath)
    {
	    return nodesByPath.get(nodePath);
    }

	@Override
    public String getPrimaryNodePathForNodeId(String nodeId, String nodeVersion)
    {
		NodeId fullNodeId = new NodeId(nodeId, nodeVersion);
	    return nodePathsById.get(fullNodeId);
    }

}
