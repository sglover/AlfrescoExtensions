/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao;

/**
 * 
 * @author sglover
 *
 */
public class NodeUsage
{
	private String nodeId;
	private Long nodeVersion;
	private long timestamp;
	private String username;
	private NodeUsageType type;

	public NodeUsage(String nodeId, Long nodeVersion, long timestamp, String username, NodeUsageType type)
    {
	    super();
	    this.nodeId = nodeId;
	    this.nodeVersion = nodeVersion;
	    this.timestamp = timestamp;
	    this.username = username;
	    this.type = type;
    }
	
	public NodeUsageType getType()
    {
        return type;
    }

    public void setType(NodeUsageType type)
    {
        this.type = type;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getNodeId()
	{
		return nodeId;
	}
	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}
	public Long getNodeVersion()
	{
		return nodeVersion;
	}
	public void setNodeVersion(Long nodeVersion)
	{
		this.nodeVersion = nodeVersion;
	}
	public long getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	public String getUsername()
	{
		return username;
	}

	
}
