/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

import java.io.Serializable;

import org.alfresco.repo.Client;

/**
 * 
 * @author steveglover
 *
 */
public class RepositoryEventImpl extends EventImpl implements RepositoryEvent, Serializable
{
    private static final long serialVersionUID = 8016433304529447871L;

    protected long txnInternalId;
    protected String txnId;
    protected String networkId; // network/tenant
    protected Client client;
    
    public RepositoryEventImpl()
    {
    	super();
    }

    public RepositoryEventImpl(long seqNumber, String type, String txnId, String networkId,
            long timestamp, String username, Client client)
    {
        super(seqNumber, type, timestamp, username);
        this.txnId = txnId;
        this.networkId = networkId;
        this.client = client;
    }

    public String getNetworkId()
    {
        return networkId;
    }

    public void setNetworkId(String networkId)
    {
        this.networkId = networkId;
    }

    public long getTxnInternalId()
    {
        return txnInternalId;
    }

    public void setTxnInternalId(long txnInternalId)
    {
        this.txnInternalId = txnInternalId;
    }

    public void setTxnId(String txnId)
    {
        this.txnId = txnId;
    }

    public String getTxnId()
    {
        return txnId;
    }

    @Override
    public Client getClient()
    {
        return client;
    }

    public void setClient(Client client) 
    {
		this.client = client;
	}

	@Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RepositoryEventImpl [id=").append(this.id).append(", type=")
                    .append(this.type).append(", username=").append(this.username)
                    .append(", client=").append(this.client)
                    .append(", timestamp=").append(this.timestamp).append(", txnId=")
                    .append(this.txnId).append(", networkId=").append(this.networkId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.client == null) ? 0 : this.client.hashCode());
        result = prime * result + ((this.networkId == null) ? 0 : this.networkId.hashCode());
        result = prime * result + ((this.txnId == null) ? 0 : this.txnId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        RepositoryEventImpl other = (RepositoryEventImpl) obj;
        if (this.client == null)
        {
            if (other.client != null) return false;
        }
        else if (!this.client.equals(other.client)) return false;
        if (this.networkId == null)
        {
            if (other.networkId != null) return false;
        }
        else if (!this.networkId.equals(other.networkId)) return false;
        if (this.txnId == null)
        {
            if (other.txnId != null) return false;
        }
        else if (!this.txnId.equals(other.txnId)) return false;
        return true;
    }

}
