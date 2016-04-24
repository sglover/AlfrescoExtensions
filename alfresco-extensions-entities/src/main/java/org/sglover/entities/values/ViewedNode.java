/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.values;

/**
 * 
 * @author sglover
 *
 */
public class ViewedNode
{
	private String username;
	private String nodeId;
	private long nodeInternalId;
	private String nodeVersion;
	private long timestamp;

	public ViewedNode(String username, String nodeId, long nodeInternalId, String nodeVersion,
            long timestamp)
    {
	    super();
	    this.username = username;
	    this.nodeId = nodeId;
	    this.nodeInternalId = nodeInternalId;
	    this.nodeVersion = nodeVersion;
	    this.timestamp = timestamp;
    }
	public String getUsername()
	{
		return username;
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
	public long getTimestamp()
	{
		return timestamp;
	}
	@Override
    public String toString()
    {
	    return "ViewedNode [username=" + username + ", nodeId=" + nodeId
	            + ", nodeInternalId=" + nodeInternalId + ", nodeVersion="
	            + nodeVersion + ", timestamp=" + timestamp + "]";
    }
}
