/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.io.Serializable;

/**
 * 
 * @author sglover
 *
 */
public class Node implements Serializable
{
	private static final long serialVersionUID = 1038231899658487035L;

	private long nodeInternalId;
	private String nodeId;
	private String nodeVersion;

	private Node()
	{
	}

	public static Node build()
	{
		return new Node();
	}
	
	public Node nodeInternalId(long nodeInternalId)
	{
		this.nodeInternalId = nodeInternalId;
		return this;
	}

	public Node nodeId(String nodeId)
	{
		this.nodeId = nodeId;
		return this;
	}

	public Node nodeVersion(String nodeVersion)
	{
		this.nodeVersion = nodeVersion;
		return this;
	}

	public Node(long nodeInternalId, String nodeId, String nodeVersion)
    {
		this(nodeId, nodeVersion);
		this.nodeInternalId = nodeInternalId;
    }

	public Node(String nodeId, String nodeVersion)
    {
	    super();
	    this.nodeId = nodeId;
	    this.nodeVersion = nodeVersion;
    }

	public String getNodeId()
	{
		return nodeId;
	}

	public long getNodeInternalId()
	{
		return nodeInternalId;
	}

	public String getNodeVersion()
	{
		return nodeVersion;
	}
}
