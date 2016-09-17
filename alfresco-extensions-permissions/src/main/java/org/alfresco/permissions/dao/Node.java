/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao;

import java.util.Optional;

import org.alfresco.permissions.Properties;

/**
 * 
 * @author sglover
 *
 */
public class Node
{
    private String nodeId;
    private int nodeVersion;
    private Optional<Properties> properties;

    public Node()
    {
    }

    public static Node withNodeId(String nodeId)
    {
        Node node = new Node();
        node.nodeId = nodeId;
        return node;
    }

    public Node withNodeVersion(int nodeVersion)
    {
        this.nodeVersion = nodeVersion;
        return this;
    }

    public Node withProperties(Optional<Properties> properties)
    {
        this.properties = properties;
        return this;
    }

    public Node(String nodeId, int nodeVersion, Optional<Properties> properties)
    {
        super();
        this.nodeId = nodeId;
        this.nodeVersion = nodeVersion;
        this.properties = properties;
    }

    public Node(String nodeId)
    {
        super();
        int idx = nodeId.indexOf(".");
        if (idx == -1)
        {
            throw new IllegalArgumentException();
        }
        this.nodeId = nodeId.substring(0, idx);
        this.nodeVersion = Integer.parseInt(nodeId.substring(idx + 1));
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public int getNodeVersion()
    {
        return nodeVersion;
    }

    public Optional<Properties> getProperties()
    {
        return properties;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result + nodeVersion;
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
        if (nodeId == null)
        {
            if (other.nodeId != null)
                return false;
        } else if (!nodeId.equals(other.nodeId))
            return false;
        if (nodeVersion != other.nodeVersion)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "Node [nodeId=" + nodeId + ", nodeVersion=" + nodeVersion + "]";
    }
}
