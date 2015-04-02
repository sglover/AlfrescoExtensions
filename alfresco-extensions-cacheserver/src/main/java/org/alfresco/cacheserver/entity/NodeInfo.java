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
public class NodeInfo
{
	private String nodeId;
	private String nodeVersion;
	private String nodePath;
	private String contentPath;
	private String mimeType;
	private Long size;

	public NodeInfo()
	{
	}

	public NodeInfo(String nodeId, String nodeVersion, String nodePath, String contentPath, String mimeType,
			Long size)
	{
	    super();
	    this.nodeId = nodeId;
	    this.nodeVersion = nodeVersion;
	    this.nodePath = nodePath;
	    this.contentPath = contentPath;
	    this.mimeType = mimeType;
	    this.size = size;
    }

	public Long getSize()
	{
		return size;
	}

	public void setSize(Long size)
	{
		this.size = size;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
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

	public String getNodePath()
	{
		return nodePath;
	}

	public void setNodePath(String nodePath)
	{
		this.nodePath = nodePath;
	}

	public String getContentPath()
	{
		return contentPath;
	}

	public void setContentPath(String contentPath)
	{
		this.contentPath = contentPath;
	}
}
