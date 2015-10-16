/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.common;

import java.io.Serializable;

/**
 * 
 * @author sglover
 *
 */
public class Node implements Serializable
{
	private static final long serialVersionUID = 1038231899658487035L;

	private Long nodeInternalId;
	private String nodeId;
	private String versionLabel;
	private Long nodeVersion;
	private String nodePath;

	private Node()
	{
	}

	public static Node build()
	{
		return new Node();
	}

	public String getNodePath()
	{
		return nodePath;
	}

	public Node nodeInternalId(Long nodeInternalId)
	{
		this.nodeInternalId = nodeInternalId;
		return this;
	}

	public Node nodePath(String nodePath)
	{
		this.nodePath = nodePath;
		return this;
	}

	public Node nodeId(String nodeId)
	{
		this.nodeId = nodeId;
		return this;
	}

	public Node versionLabel(String versionLabel)
	{
		this.versionLabel = versionLabel;
		return this;
	}

	public Node nodeVersion(Long nodeVersion)
	{
		this.nodeVersion = nodeVersion;
		return this;
	}

	public Node(long nodeInternalId, String nodeId, String nodeVersion)
    {
		this(nodeId, nodeVersion);
		this.nodeInternalId = nodeInternalId;
    }

	public Node(String nodeId, String versionLabel)
    {
	    super();
	    this.nodeId = nodeId;
	    this.versionLabel = versionLabel;
    }

	public String getNodeId()
	{
		return nodeId;
	}

	public Long getNodeInternalId()
	{
		return nodeInternalId;
	}

	public Long getNodeVersion()
	{
		return nodeVersion;
	}

	public String getVersionLabel()
	{
		return versionLabel;
	}

	@Override
    public String toString()
    {
	    return "Node [nodeInternalId=" + nodeInternalId + ", nodeId=" + nodeId
	            + ", versionLabel=" + versionLabel + ", nodeVersion="
	            + nodeVersion + ", nodePath=" + nodePath + "]";
    }
}
