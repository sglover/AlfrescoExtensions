/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * 
 * @author sglover
 *
 */
public class Node implements Serializable
{
    private static final long serialVersionUID = 1038231899658487035L;

    private Long nodeInternalId;
    private String nodeId;
    private String versionLabel;
    private Long nodeVersion;
    private String nodePath;
    private MimeType mimeType;

    public static Node fromNodeId(String nodeIdStr)
    {
        StringTokenizer st = new StringTokenizer(nodeIdStr, ":");
        if(st.countTokens() < 2)
        {
            throw new IllegalArgumentException();
        }

        String nodeId = st.nextToken();
        long nodeVersion = Long.parseLong(st.nextToken());
        MimeType mimeType = st.hasMoreTokens() ? MimeType.INSTANCES.getByMimetype(st.nextToken()) : MimeType.TEXT;
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion).mimeType(mimeType);
        return node;
    }

    private Node()
    {
    }

    public static Node build()
    {
        return new Node();
    }

    public String getNodePath()
    {
        return nodePath;
    }

    public MimeType getMimeType()
    {
        return mimeType;
    }

    public Node mimeType(MimeType mimeType)
    {
        this.mimeType = mimeType;
        return this;
    }

    public Node nodeInternalId(Long nodeInternalId)
    {
        this.nodeInternalId = nodeInternalId;
        return this;
    }

    public Node nodePath(String nodePath)
    {
        this.nodePath = nodePath;
        return this;
    }

    public Node nodeId(String nodeId)
    {
        this.nodeId = nodeId;
        return this;
    }

    public Node versionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
        return this;
    }

    public Node nodeVersion(Long nodeVersion)
    {
        this.nodeVersion = nodeVersion;
        return this;
    }

    public Node newNodeVersion(Long nodeVersion)
    {
        Node ret = new Node(nodeInternalId, nodeId, versionLabel, nodeVersion);
        return ret;
    }

    public Node(Long nodeInternalId, String nodeId, String versionLabel,
            long nodeVersion)
    {
        this(nodeId, versionLabel);
        this.nodeInternalId = nodeInternalId;
        this.nodeVersion = nodeVersion;
    }

    public Node(String nodeId, String versionLabel)
    {
        super();
        this.nodeId = nodeId;
        this.versionLabel = versionLabel;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public Long getNodeInternalId()
    {
        return nodeInternalId;
    }

    public Long getNodeVersion()
    {
        return nodeVersion;
    }

    public String getVersionLabel()
    {
        return versionLabel;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mimeType == null) ? 0 : mimeType.hashCode());
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result
                + ((nodeInternalId == null) ? 0 : nodeInternalId.hashCode());
        result = prime * result
                + ((nodePath == null) ? 0 : nodePath.hashCode());
        result = prime * result
                + ((nodeVersion == null) ? 0 : nodeVersion.hashCode());
        result = prime * result
                + ((versionLabel == null) ? 0 : versionLabel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (mimeType != other.mimeType)
            return false;
        if (nodeId == null)
        {
            if (other.nodeId != null)
                return false;
        }
        else if (!nodeId.equals(other.nodeId))
            return false;
        if (nodeInternalId == null)
        {
            if (other.nodeInternalId != null)
                return false;
        }
        else if (!nodeInternalId.equals(other.nodeInternalId))
            return false;
        if (nodePath == null)
        {
            if (other.nodePath != null)
                return false;
        }
        else if (!nodePath.equals(other.nodePath))
            return false;
        if (nodeVersion == null)
        {
            if (other.nodeVersion != null)
                return false;
        }
        else if (!nodeVersion.equals(other.nodeVersion))
            return false;
        if (versionLabel == null)
        {
            if (other.versionLabel != null)
                return false;
        }
        else if (!versionLabel.equals(other.versionLabel))
            return false;
        return true;
    }

    public String getId()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeId);
        sb.append(":");
        sb.append(nodeVersion);
        sb.append(":");
        sb.append(mimeType.toString());
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "Node [nodeInternalId=" + nodeInternalId + ", nodeId=" + nodeId
                + ", versionLabel=" + versionLabel + ", nodeVersion="
                + nodeVersion + ", nodePath=" + nodePath + ", mimeType="
                + mimeType + "]";
    }
}
