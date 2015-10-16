/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao;

/**
 * 
 * @author sglover
 *
 */
public class Node
{
	private String nodeId;
	private String nodeVersion;

	public Node()
	{
	}

	public static Node withNodeId(String nodeId)
	{
		Node node = new Node();
		node.nodeId = nodeId;
		return node;
	}

	public Node withNodeVersion(String nodeVersion)
	{
		this.nodeVersion = nodeVersion;
		return this;
	}

	public Node(String nodeId, String nodeVersion)
    {
        super();
        this.nodeId = nodeId;
        this.nodeVersion = nodeVersion;
    }
	public String getNodeId()
	{
		return nodeId;
	}
	public String getNodeVersion()
	{
		return nodeVersion;
	}
	
	
	@Override
    public int hashCode()
    {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
	    result = prime * result
	            + ((nodeVersion == null) ? 0 : nodeVersion.hashCode());
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
	    if (nodeVersion == null)
	    {
		    if (other.nodeVersion != null)
			    return false;
	    } else if (!nodeVersion.equals(other.nodeVersion))
		    return false;
	    return true;
    }

	@Override
    public String toString()
    {
        return "Node [nodeId=" + nodeId + ", nodeVersion=" + nodeVersion
                + "]";
    }
}
