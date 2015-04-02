/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.entity;

/**
 * 
 * @author sglover
 *
 */
public class NodeUsage
{
	private String nodeId;
	private String nodeVersion;
	private long timestamp;
	private String username;

	public NodeUsage(String nodeId, String nodeVersion, long timestamp, String username)
    {
	    super();
	    this.nodeId = nodeId;
	    this.nodeVersion = nodeVersion;
	    this.timestamp = timestamp;
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
	public String getNodeVersion()
	{
		return nodeVersion;
	}
	public void setNodeVersion(String nodeVersion)
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
