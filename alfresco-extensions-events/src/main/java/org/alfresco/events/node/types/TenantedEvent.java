/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

/**
 * An event that occurs in the context of an Alfresco tenant/aka network
 * 
 * @author Gethin James
 */
public interface TenantedEvent extends Event
{
    public String getNetworkId();

}