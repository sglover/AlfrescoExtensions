/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo;

/**
 * Any client that connects to Alfresco, based on FileFilterMode.Client
 * It has additional clients beyond those originally available in FileFilterMode.Client.
 *
 * @author Gethin James
 * @author sglover
 */
public class Client
{
    public Client()
    {
        super();
    }

    public static enum ClientType
    {
        cifs, imap, webdav, nfs, script, webclient, ftp, cmis, admin, aos, cloud, salesforce;
    }

    private ClientType type;
    private String clientId; // alfrescoClientId, used by device sync to identify a specific client
    
    public Client(ClientType type, String clientId)
    {
        super();
        this.type = type;
        this.clientId = clientId;
    }

    public static Client asType(ClientType type)
    {
        return new Client(type, null);
    }

    public ClientType getType()
    {
        return this.type;
    }

    public String getClientId()
    {
        return this.clientId;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Client [type=").append(this.type).append(", clientId=")
                    .append(this.clientId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.clientId == null) ? 0 : this.clientId.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Client other = (Client) obj;
        if (this.clientId == null)
        {
            if (other.clientId != null) return false;
        }
        else if (!this.clientId.equals(other.clientId)) return false;
        if (this.type != other.type) return false;
        return true;
    }
}
