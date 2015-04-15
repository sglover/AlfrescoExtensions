/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

/**
 * 
 * @author sglover
 *
 */
public class NodeId
{
	private String nodeId;
	private String nodeVersion;
	public NodeId(String nodeId, String nodeVersion)
    {
	    super();
	    this.nodeId = nodeId;
	    this.nodeVersion = nodeVersion;
    }
	public String getNodeId()
	{
		return nodeId;
	}
	public String getNodeVersion()
	{
		return nodeVersion;
	}

	
}
