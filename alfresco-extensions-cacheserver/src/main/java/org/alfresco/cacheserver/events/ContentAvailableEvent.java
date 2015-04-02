/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.events;

import java.io.Serializable;

/**
 * 
 * @author sglover
 *
 */
public class ContentAvailableEvent implements Serializable
{
	private static final long serialVersionUID = 1545601516434160821L;

	private String cacheServerId;
	private String nodeId;
	private String nodeVersion;
	private String nodePath;
	private String mimeType;
	private long size;
	private String hostname;
	private int port;

	public ContentAvailableEvent()
	{
	}

	public ContentAvailableEvent(String cacheServerId, String nodeId, String nodeVersion, String nodePath, String mimeType,
			long size, String hostname, int port)
    {
	    super();
	    this.cacheServerId = cacheServerId;
	    this.nodeId = nodeId;
	    this.nodeVersion = nodeVersion;
	    this.nodePath = nodePath;
	    this.mimeType = mimeType;
	    this.size = size;
	    this.hostname = hostname;
	    this.port = port;
    }

	public String getCacheServerId()
	{
		return cacheServerId;
	}

	public void setCacheServerId(String cacheServerId)
	{
		this.cacheServerId = cacheServerId;
	}

	public String getNodePath()
	{
		return nodePath;
	}

	public void setNodePath(String nodePath)
	{
		this.nodePath = nodePath;
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

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	@Override
    public String toString()
    {
	    return "ContentAvailableEvent [cacheServerId=" + cacheServerId + ", nodeId=" + nodeId
	            + ", nodeVersion=" + nodeVersion + ", nodePath=" + nodePath
	            + ", mimeType=" + mimeType + ", size=" + size + ", hostname="
	            + hostname + ", port=" + port + "]";
    }

}
