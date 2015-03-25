/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

import org.alfresco.repo.Client;

/**
 * A Basic Event that occurs on an Alfresco node.
 * 
 * @author Gethin James
 * @author sglover
 */
public class BasicNodeEventImpl extends RepositoryEventImpl implements BasicNodeEvent
{
    private static final long serialVersionUID = -5915563756442975835L;

    protected long nodeInternalId;
    protected long nodeVersion;
    protected String versionLabel;
    protected String nodeId; // node id (guid)
    protected String siteId;
    protected String nodeType;
    protected String name;
    
    public BasicNodeEventImpl()
    {
        super();
    }

    public BasicNodeEventImpl(long sequenceNumber, String type, String txnId, String networkId, long timestamp,
    		String username, Client client)
    {
        super(sequenceNumber, type, txnId, networkId, timestamp, username, client);
    }

    public BasicNodeEventImpl(long sequenceNumber, String type, String txnId, String networkId, long timestamp,
                String username, String nodeId, String siteId, String nodeType, String name, Client client)
    {
        super(sequenceNumber, type, txnId, networkId, timestamp, username, client);
        this.nodeId = nodeId;
        this.siteId = siteId;
        this.nodeType = nodeType;
        this.client = client;
        this.name = name;
    }

    public String getVersionLabel()
	{
		return versionLabel;
	}

	public void setVersionLabel(String versionLabel)
	{
		this.versionLabel = versionLabel;
	}

	public long getNodeInternalId()
    {
        return nodeInternalId;
    }

    public void setNodeInternalId(long nodeInternalId)
    {
        this.nodeInternalId = nodeInternalId;
    }

    public long getNodeVersion()
	{
		return nodeVersion;
	}

	public void setNodeVersion(long nodeVersion)
	{
		this.nodeVersion = nodeVersion;
	}

	/*
     * @see org.alfresco.events.types.BasicNodeEvent#getNodeId()
     */
    @Override
    public String getNodeId()
    {
        return this.nodeId;
    }
    /*
     * @see org.alfresco.events.types.BasicNodeEvent#getSiteId()
     */
    @Override
    public String getSiteId()
    {
        return this.siteId;
    }
    /*
     * @see org.alfresco.events.types.BasicNodeEvent#getNodeType()
     */
    @Override
    public String getNodeType()
    {
        return this.nodeType;
    }

    /*
     * @see org.alfresco.events.types.BasicNodeEvent#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    @Override
    public void setSiteId(String siteId)
    {
        this.siteId = siteId;
    }

    @Override
    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.nodeId == null) ? 0 : this.nodeId.hashCode());
        result = prime * result + ((this.nodeType == null) ? 0 : this.nodeType.hashCode());
        result = prime * result + ((this.siteId == null) ? 0 : this.siteId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        BasicNodeEventImpl other = (BasicNodeEventImpl) obj;
        if (this.name == null)
        {
            if (other.name != null) return false;
        }
        else if (!this.name.equals(other.name)) return false;
        if (this.nodeId == null)
        {
            if (other.nodeId != null) return false;
        }
        else if (!this.nodeId.equals(other.nodeId)) return false;
        if (this.nodeType == null)
        {
            if (other.nodeType != null) return false;
        }
        else if (!this.nodeType.equals(other.nodeType)) return false;
        if (this.siteId == null)
        {
            if (other.siteId != null) return false;
        }
        else if (!this.siteId.equals(other.siteId)) return false;
        return true;
    }
}