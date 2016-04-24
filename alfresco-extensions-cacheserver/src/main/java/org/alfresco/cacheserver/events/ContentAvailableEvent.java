/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.events;

import java.io.Serializable;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.NodeChecksums;

/**
 * 
 * @author sglover
 *
 */
public class ContentAvailableEvent implements Serializable
{
	private static final long serialVersionUID = 1545601516434160821L;

	private String cacheServerId;
	private Node node;
	private String mimeType;
	private long size;
	private String hostname;
	private int port;
	private NodeChecksums checksums;

	public ContentAvailableEvent()
	{
	}

	public ContentAvailableEvent(String cacheServerId, Node node, String mimeType, long size, String hostname, int port,
	        NodeChecksums checksums)
    {
	    super();
	    this.cacheServerId = cacheServerId;
	    this.node = node;
	    this.mimeType = mimeType;
	    this.size = size;
	    this.hostname = hostname;
	    this.port = port;
	    this.checksums = checksums;
    }

    public NodeChecksums getChecksums()
    {
        return checksums;
    }

    public void setChecksums(NodeChecksums checksums)
    {
        this.checksums = checksums;
    }

    public Node getNode()
	{
		return node;
	}

	public void setNode(Node node)
	{
		this.node = node;
	}

	public String getCacheServerId()
	{
		return cacheServerId;
	}

	public void setCacheServerId(String cacheServerId)
	{
		this.cacheServerId = cacheServerId;
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
	    return "ContentAvailableEvent [cacheServerId=" + cacheServerId
	            + ", node=" + node
	            + ", mimeType=" + mimeType + ", size=" + size + ", hostname="
	            + hostname + ", port=" + port + "]";
    }
}
