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
	private long nodeVersion;

	public Node(long nodeInternalId, long nodeVersion)
    {
	    super();
	    this.nodeInternalId = nodeInternalId;
	    this.nodeVersion = nodeVersion;
    }

	public long getNodeInternalId()
	{
		return nodeInternalId;
	}
	public long getNodeVersion()
	{
		return nodeVersion;
	}
}
