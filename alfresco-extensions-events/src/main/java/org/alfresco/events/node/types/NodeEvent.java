/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.Client;

/**
 * A node event.
 * 
 * @author steveglover
 *
 */
public class NodeEvent extends BasicNodeEventImpl implements Serializable, NodeInfoEvent
{
	private static final long serialVersionUID = 1632258418479600707L;

	protected Long nodeModificationTime;
	protected List<String> paths; // all paths, first one is the primary
	protected List<List<String>> parentNodeIds; // all paths, first one is the primary
	protected Set<String> aspects;
	protected Map<String, Serializable> nodeProperties = new HashMap<String, Serializable>();
	protected long aclId;

    // TODO checksum?
	// TODO changeId?

	public NodeEvent()
	{
    	super();
	}

	@SuppressWarnings("unchecked")
	public NodeEvent(long seqNumber, String name, String type, String txnId, long timestamp, String networkId, String siteId, 
                   String nodeId, String nodeType, List<String> paths, List<List<String>> parentNodeIds, String username,
                   Long nodeModificationTime, Client client, Set<String> aspects, Map<String, Serializable> nodeProperties)
    {
         super(seqNumber, type, txnId, networkId, timestamp,username, nodeId,  siteId, nodeType, name, client);
         this.paths = (List<String>) (paths==null?new ArrayList<>():paths);
         this.parentNodeIds = (List<List<String>>) (parentNodeIds==null?new ArrayList<>():parentNodeIds);
         this.nodeModificationTime = nodeModificationTime;
         this.aspects = (Set<String>)(aspects==null?new HashSet<>():aspects);
         this.nodeProperties =  (Map<String, Serializable>)(nodeProperties==null?new HashMap<>():nodeProperties);
    }

	public long getAclId()
	{
        return aclId;
    }

    public void setAclId(long aclId)
    {
        this.aclId = aclId;
    }

    public Set<String> getAspects()
	{
		return aspects;
	}

	public void setAspects(Set<String> aspects)
	{
		this.aspects = aspects;
	}

	public Map<String, Serializable> getNodeProperties()
	{
		return nodeProperties;
	}

	public void setNodeProperties(Map<String, Serializable> nodeProperties)
	{
		this.nodeProperties = nodeProperties;
	}

    public Long getNodeModificationTime()
	{
		return nodeModificationTime;
	}

	public void setNodeModificationTime(Long nodeModificationTime)
	{
		this.nodeModificationTime = nodeModificationTime;
	}

	/*
     * @see org.alfresco.events.types.NodeInfoEvent#getPaths()
     */
	@Override
    public List<String> getPaths()
	{
		return paths;
	}

	public void setPaths(List<String> paths)
	{
		this.paths = paths;
	}

	public void setNetworkId(String networkId)
	{
		this.networkId = networkId;
	}

	public String getNetworkId()
	{
		return networkId;
	}
	
	/*
     * @see org.alfresco.events.types.NodeInfoEvent#getParentNodeIds()
     */
	@Override
    public List<List<String>> getParentNodeIds()
	{
        return parentNodeIds;
    }

    public void setParentNodeIds(List<List<String>> parentNodeIds)
    {
        this.parentNodeIds = parentNodeIds;
    }

    @Override
    public String toString()
    {
        return "NodeEvent [name=" + name + ", nodeModificationTime=" + nodeModificationTime + ", nodeId="
                + getNodeId() + ", siteId=" + getSiteId() + ", username=" + username
                + ", networkId=" + networkId + ", paths=" + paths + ", nodeType="
                + ", seqNumber=" + seqNumber + ", parentNodeIds=" + parentNodeIds 
                + getNodeType() + ", type=" + type + ", txnId=" + txnId + ", timestamp="
                + timestamp + "]";
    }

}
