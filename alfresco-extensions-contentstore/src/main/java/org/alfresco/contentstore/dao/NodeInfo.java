/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao;

import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public class NodeInfo
{
	private Node node;
	private String contentPath;
	private String mimeType;
	private Long size;
	private boolean isPrimary = true;
	private long transformDuration;

	public static NodeInfo start()
	{
		return new NodeInfo();
	}

	public static NodeInfo start(Node node)
	{
		return new NodeInfo(node);
	}

	public NodeInfo()
	{
	}

	public NodeInfo(Node node)
	{
	    super();
	    this.node = node;
	}

	public NodeInfo(Node node, String contentPath, String mimeType,
			Long size)
	{
	    super();
	    this.node = node;
	    this.contentPath = contentPath;
	    this.mimeType = mimeType;
	    this.size = size;
    }

//	public NodeInfo(Node node, String contentPath, String mimeType,
//			Long size)
//	{
//	    super();
//	    this.node = node;
//	    this.contentPath = contentPath;
//	    this.mimeType = mimeType;
//	    this.size = size;
//    }

	public Node getNode()
	{
		return node;
	}

//	public NodeInfo setNodeInternalId(Long nodeInternalId)
//	{
//		this.nodeInternalId = nodeInternalId;
//		return this;
//	}

	public boolean isPrimary()
	{
		return isPrimary;
	}

	public NodeInfo setPrimary(boolean isPrimary)
	{
		this.isPrimary = isPrimary;
		return this;
	}

	public long getTransformDuration()
	{
		return transformDuration;
	}

	public NodeInfo setTransformDuration(long transformDuration)
	{
		this.transformDuration = transformDuration;
		return this;
	}

	public Long getSize()
	{
		return size;
	}

	public NodeInfo setSize(Long size)
	{
		this.size = size;
		return this;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public NodeInfo setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
		return this;
	}

//	public String getNodeId()
//	{
//		return nodeId;
//	}
//
//	public NodeInfo setNodeId(String nodeId)
//	{
//		this.nodeId = nodeId;
//		return this;
//	}
//
//	public String getNodeVersion()
//	{
//		return nodeVersion;
//	}
//
//	public NodeInfo setNodeVersion(String nodeVersion)
//	{
//		this.nodeVersion = nodeVersion;
//		return this;
//	}

//	public String getNodePath()
//	{
//		return nodePath;
//	}
//
//	public NodeInfo setNodePath(String nodePath)
//	{
//		this.nodePath = nodePath;
//		return this;
//	}

	public String getContentPath()
	{
		return contentPath;
	}

	public NodeInfo setContentPath(String contentPath)
	{
		this.contentPath = contentPath;
		return this;
	}

	@Override
    public String toString()
    {
	    return "NodeInfo [node=" + node + ", contentPath=" + contentPath
	            + ", mimeType=" + mimeType + ", size=" + size + ", isPrimary="
	            + isPrimary + ", transformDuration=" + transformDuration + "]";
    }
}
