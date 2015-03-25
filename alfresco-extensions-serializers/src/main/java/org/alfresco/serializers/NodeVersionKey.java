/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import java.io.Serializable;

/**
 * Key for caches that need to be bound implicitly to the current version of a node.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class NodeVersionKey implements Serializable, Comparable<NodeVersionKey>
{
    private static final long serialVersionUID = 2241045540959490539L;
    
    private final Long nodeId;
    private final Long version;

    public NodeVersionKey(Long nodeId, Long version)
    {
        this.nodeId = nodeId;
        this.version = version;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof NodeVersionKey))
        {
            return false;
        }
        NodeVersionKey o = (NodeVersionKey)other;
        return nodeId.equals(o.nodeId) && version.equals(o.version);
    }
    
    @Override
    public int hashCode()
    {
        return nodeId.hashCode() + version.hashCode()*37;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("NodeVersionKey ")
               .append("[nodeId=").append(nodeId)
               .append(", version=").append(version)
               .append("]");
        return builder.toString();
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public Long getVersion()
    {
        return version;
    }

	@Override
	public int compareTo(NodeVersionKey o)
	{
		return Long.compare(nodeId, o.nodeId);
	}
}
