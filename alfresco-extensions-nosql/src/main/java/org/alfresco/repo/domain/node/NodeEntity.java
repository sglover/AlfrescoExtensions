/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.domain.node;

/**
 * 
 * @author sglover
 *
 */
public class NodeEntity
{
	private String txnId;
	private long parentNodeId;
	private long parentNodeVersion;
	private long nodeId;
	private long nodeVersion;
	private String versionLabel;

	public long getParentNodeId()
	{
		return parentNodeId;
	}
	public void setParentNodeId(long parentNodeId)
	{
		this.parentNodeId = parentNodeId;
	}
	public long getParentNodeVersion()
	{
		return parentNodeVersion;
	}
	public void setParentNodeVersion(long parentNodeVersion)
	{
		this.parentNodeVersion = parentNodeVersion;
	}
	public String getTxnId()
	{
		return txnId;
	}
	public void setTxnId(String txnId)
	{
		this.txnId = txnId;
	}
	public long getNodeId()
	{
		return nodeId;
	}
	public void setNodeId(long nodeId)
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
	
	public String getVersionLabel()
	{
		return versionLabel;
	}
	public void setVersionLabel(String versionLabel)
	{
		this.versionLabel = versionLabel;
	}

	@Override
    public int hashCode()
    {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + (int) (nodeId ^ (nodeId >>> 32));
	    result = prime * result + (int) (nodeVersion ^ (nodeVersion >>> 32));
	    result = prime * result + (int) (parentNodeId ^ (parentNodeId >>> 32));
	    result = prime * result
	            + (int) (parentNodeVersion ^ (parentNodeVersion >>> 32));
	    result = prime * result + ((txnId == null) ? 0 : txnId.hashCode());
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
	    NodeEntity other = (NodeEntity) obj;
	    if (nodeId != other.nodeId)
		    return false;
	    if (nodeVersion != other.nodeVersion)
		    return false;
	    if (parentNodeId != other.parentNodeId)
		    return false;
	    if (parentNodeVersion != other.parentNodeVersion)
		    return false;
	    if (txnId == null)
	    {
		    if (other.txnId != null)
			    return false;
	    } else if (!txnId.equals(other.txnId))
		    return false;
	    if (versionLabel == null)
	    {
		    if (other.versionLabel != null)
			    return false;
	    } else if (!versionLabel.equals(other.versionLabel))
		    return false;
	    return true;
    }
	@Override
    public String toString()
    {
	    return "NodeEntity [txnId=" + txnId + ", parentNodeId=" + parentNodeId
	            + ", parentNodeVersion=" + parentNodeVersion + ", nodeId="
	            + nodeId + ", nodeVersion=" + nodeVersion + ", versionLabel="
	            + versionLabel + "]";
    }
	
}
