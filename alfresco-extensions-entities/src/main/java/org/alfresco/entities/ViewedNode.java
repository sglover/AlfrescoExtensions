/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

/**
 * 
 * @author sglover
 *
 */
public class ViewedNode
{
	private String username;
	private long nodeInternalId;
	private long nodeVersion;
	private long timestamp;
	public ViewedNode(String username, long nodeInternalId, long nodeVersion,
            long timestamp)
    {
	    super();
	    this.username = username;
	    this.nodeInternalId = nodeInternalId;
	    this.nodeVersion = nodeVersion;
	    this.timestamp = timestamp;
    }
	public String getUsername()
	{
		return username;
	}
	public long getNodeInternalId()
	{
		return nodeInternalId;
	}
	public long getNodeVersion()
	{
		return nodeVersion;
	}
	public long getTimestamp()
	{
		return timestamp;
	}

	
}
