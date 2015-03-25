/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

import java.util.Date;

import org.alfresco.events.node.types.BasicNodeEventImpl;
import org.alfresco.repo.Client;

/**
 * Occurs when content is read.
 *
 * @author Gethin James
 * @since 5.0
 */
public class ContentEventImpl extends BasicNodeEventImpl implements ContentEvent
{
    private static final long serialVersionUID = 6471232122343040380L;

    String mimeType;
    long size;
    String encoding;

    public ContentEventImpl()
    {
        super();
    }
    
    public ContentEventImpl(String type, String username, String networkId, String txnId,  String nodeId,
                            String siteId, String nodeType, Client client, String name, String mimeType, long size,
                            String encoding)
    {
        super(-1, type, txnId, networkId, new Date().getTime(), username, nodeId, siteId, nodeType, name, client);
        this.mimeType = mimeType;
        this.size = size;
        this.encoding = encoding;
    }

    public ContentEventImpl(String type, String username, String networkId, long timestamp, String txnId,  String nodeId,
                String siteId, String nodeType, Client client, String name, String mimeType, long size, String encoding)
    {
        super(-1, type, txnId, networkId, timestamp, username, nodeId, siteId, nodeType, name, client);
        this.mimeType = mimeType;
        this.size = size;
        this.encoding = encoding;
    }
    @Override
    public String getMimeType()
    {
        return this.mimeType;
    }
    @Override
    public long getSize()
    {
        return this.size;
    }
    @Override
    public String getEncoding()
    {
        return this.encoding;
    }

    @Override
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    @Override
    public void setSize(long size)
    {
        this.size = size;
    }

    @Override
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ContentReadEvent [id=").append(this.id).append(", type=").append(this.type)
               .append(", timestamp=").append(this.timestamp).append(", username=")
               .append(this.username).append(", client=").append(this.client)
               .append(", networkId=").append(this.networkId).append(", siteId=")
               .append(this.siteId).append(", txnId=").append(this.txnId).append(", nodeId=")
               .append(this.nodeId).append(", nodeType=").append(this.nodeType)
               .append(", name=").append(this.name)
               .append(", mimeType=").append(this.mimeType).append(", size=")
               .append(this.size).append(", encoding=").append(this.encoding).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.encoding == null) ? 0 : this.encoding.hashCode());
        result = prime * result + ((this.mimeType == null) ? 0 : this.mimeType.hashCode());
        result = prime * result + (int) (this.size ^ (this.size >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ContentEventImpl other = (ContentEventImpl) obj;
        if (this.encoding == null)
        {
            if (other.encoding != null) return false;
        }
        else if (!this.encoding.equals(other.encoding)) return false;
        if (this.mimeType == null)
        {
            if (other.mimeType != null) return false;
        }
        else if (!this.mimeType.equals(other.mimeType)) return false;
        if (this.size != other.size) return false;
        return true;
    }
}