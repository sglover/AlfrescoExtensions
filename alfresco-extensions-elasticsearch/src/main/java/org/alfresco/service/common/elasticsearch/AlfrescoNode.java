/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlfrescoNode
{
	private String changeTxnId;
	private Long txnId;
    private long nodeInternalId;
    private String nodeId;
    private long nodeVersion;
    private String nodeRef;
    private String type;
    private long aclId;
    private Map<String, Serializable> properties;
    private Set<String> aspects;
    private List<String> paths;

    
    public Long getTxnId()
	{
		return txnId;
	}
	public void setTxnId(Long txnId)
	{
		this.txnId = txnId;
	}
	public String getChangeTxnId()
	{
		return changeTxnId;
	}
	public void setChangeTxnId(String changeTxnId)
	{
		this.changeTxnId = changeTxnId;
	}


	public long getNodeInternalId()
	{
		return nodeInternalId;
	}
	public void setNodeInternalId(long nodeInternalId)
	{
		this.nodeInternalId = nodeInternalId;
	}
	public String getNodeId()
	{
		return nodeId;
	}
	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}
	public long getNodeVersion()
	{
		return nodeVersion;
	}
	public void setNodeVersion(long nodeVersion)
	{
		this.nodeVersion = nodeVersion;
	}
	public String getNodeRef()
    {
        return nodeRef;
    }
    public void setNodeRef(String nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public long getAclId()
    {
        return aclId;
    }
    public void setAclId(long aclId)
    {
        this.aclId = aclId;
    }
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }
    public void setProperties(Map<String, Serializable> properties)
    {
        this.properties = properties;
    }
    public Set<String> getAspects()
    {
        return aspects;
    }
    public void setAspects(Set<String> aspects)
    {
        this.aspects = aspects;
    }
    public List<String> getPaths()
    {
        return paths;
    }
    public void setPaths(List<String> paths)
    {
        this.paths = paths;
    }
	@Override
    public String toString()
    {
	    return "AlfrescoNode [changeTxnId=" + changeTxnId + ", txnId=" + txnId
	            + ", nodeId=" + nodeId + ", nodeVersion=" + nodeVersion
	            + ", nodeRef=" + nodeRef + ", type=" + type + ", aclId="
	            + aclId + ", properties=" + properties + ", aspects=" + aspects
	            + ", paths=" + paths + "]";
    }
}
