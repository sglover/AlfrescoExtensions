/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao;

import org.alfresco.contentstore.ContentReference;
import org.alfresco.extensions.common.MimeType;
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
    private MimeType mimeType;
    private String encoding;
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

    public static NodeInfo start(ContentReference reference)
    {
        return new NodeInfo(reference);
    }

    public NodeInfo()
    {
    }

    public NodeInfo(ContentReference reference)
    {
        super();
        this.node = reference.getNode();
        this.mimeType = reference.getMimetype();
        this.encoding = reference.getEncoding();
    }

    public NodeInfo(Node node)
    {
        super();
        this.node = node;
    }

    public NodeInfo(Node node, String contentPath, MimeType mimeType,
            String encoding, Long size)
    {
        super();
        this.node = node;
        this.contentPath = contentPath;
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.size = size;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public Node getNode()
    {
        return node;
    }

    // public NodeInfo setNodeInternalId(Long nodeInternalId)
    // {
    // this.nodeInternalId = nodeInternalId;
    // return this;
    // }

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

    public MimeType getMimeType()
    {
        return mimeType;
    }

    public NodeInfo setMimeType(MimeType mimeType)
    {
        this.mimeType = mimeType;
        return this;
    }

    // public String getNodeId()
    // {
    // return nodeId;
    // }
    //
    // public NodeInfo setNodeId(String nodeId)
    // {
    // this.nodeId = nodeId;
    // return this;
    // }
    //
    // public String getNodeVersion()
    // {
    // return nodeVersion;
    // }
    //
    // public NodeInfo setNodeVersion(String nodeVersion)
    // {
    // this.nodeVersion = nodeVersion;
    // return this;
    // }

    // public String getNodePath()
    // {
    // return nodePath;
    // }
    //
    // public NodeInfo setNodePath(String nodePath)
    // {
    // this.nodePath = nodePath;
    // return this;
    // }

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
                + ", mimeType=" + mimeType + ", encoding=" + encoding
                + ", size=" + size + ", isPrimary=" + isPrimary
                + ", transformDuration=" + transformDuration + "]";
    }
}
