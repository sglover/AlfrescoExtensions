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
*
* A TransactionEvent event that is also aware of its ordering.
* 
* @author Gethin James
* @since 5.0
*/
public abstract class TransactionEvent extends RepositoryEventImpl
{
    private static final long serialVersionUID = -3217767152720757859L;

    public TransactionEvent()
    {
        super();
    }

    public TransactionEvent(long seqNumber, String type, String txnId, String networkId, long timestamp,
                String username, Client client)
    {
        super(seqNumber, type, txnId, networkId, timestamp, username, client);
    }

}
