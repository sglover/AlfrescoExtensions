/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.Client;

/**
 * Node properties update event.
 * 
 * @author steveglover
 *
 */
public class NodeUpdatedEvent extends NodeEvent
{
	private static final long serialVersionUID = 2475045139546824298L;

	public static final String EVENT_TYPE = "NODEUPDATED";

	private Map<String, Object> propertiesAdded;
	private Set<String> propertiesRemoved;
	private Map<String, Object> propertiesChanged;
	private Set<String> aspectsAdded;
	private Set<String> aspectsRemoved;

	public NodeUpdatedEvent()
	{
    	super();
        this.type = EVENT_TYPE;
	}

	public NodeUpdatedEvent(long seqNumber, String name, String txnId, long time, String networkId, String siteId,
			String nodeId, String nodeType, List<String> paths, List<List<String>> pathNodeIds, String userId,
			Long modificationTime, Map<String, Object> propertiesAdded,
			Set<String> propertiesRemoved, Map<String, Object> propertiesChanged,
			Set<String> aspectsAdded, Set<String> aspectsRemoved, Client client, Set<String> aspects,
			Map<String, Serializable> properties)
	{
		super(seqNumber, name, EVENT_TYPE, txnId, time, networkId, siteId, nodeId, nodeType, paths, pathNodeIds, userId,
				modificationTime, client, aspects, properties);
		this.propertiesAdded = propertiesAdded;
		this.propertiesRemoved = propertiesRemoved;
		this.propertiesChanged = propertiesChanged;
		this.aspectsAdded = aspectsAdded;
		this.aspectsRemoved = aspectsRemoved;
	}
	
	public Set<String> getAspectsAdded()
	{
		return aspectsAdded;
	}

	public Set<String> getAspectsRemoved()
	{
		return aspectsRemoved;
	}
	
	public void setAspectsAdded(Set<String> aspectsAdded)
	{
		this.aspectsAdded = aspectsAdded;
	}

	public void setAspectsRemoved(Set<String> aspectsRemoved)
	{
		this.aspectsRemoved = aspectsRemoved;
	}

	public Map<String, Object> getPropertiesAdded()
	{
		return propertiesAdded;
	}

	public void setPropertiesAdded(Map<String, Object> propertiesAdded)
	{
		this.propertiesAdded = propertiesAdded;
	}

	public Set<String> getPropertiesRemoved()
	{
		return propertiesRemoved;
	}

	public void setPropertiesRemoved(Set<String> propertiesRemoved)
	{
		this.propertiesRemoved = propertiesRemoved;
	}

	public Map<String, Object> getPropertiesChanged()
	{
		return propertiesChanged;
	}

	public void setPropertiesChanged(Map<String, Object> propertiesChanged)
	{
		this.propertiesChanged = propertiesChanged;
	}

	@Override
	public String toString()
	{
		return "NodeUpdatedEvent [propertiesAdded=" + propertiesAdded
				+ ", propertiesRemoved=" + propertiesRemoved
				+ ", propertiesChanged=" + propertiesChanged
				+ ", aspectsAdded=" + aspectsAdded + ", aspectsRemoved="
				+ aspectsRemoved + ", nodeModificationTime=" + nodeModificationTime
				+ ", nodeId=" + nodeId + ", siteId=" + siteId + ", username="
				+ username + ", networkId=" + networkId + ", paths=" + paths
				+ ", type=" + type + ", txnId=" + txnId + ", timestamp=" + timestamp
				+ "]";
	}
	
}