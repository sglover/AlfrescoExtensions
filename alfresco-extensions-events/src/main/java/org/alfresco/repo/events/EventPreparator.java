/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import org.alfresco.events.node.types.Event;


/**
* Creates and prepares event information.
*
* The primary reason for this interface is to allow for deferred creation
* of the Event.  If a NoOpEventPublisher is being used then the prepareEvent()
* method will never get called.
*
* As of Java 8 a Lambda expression could be used as the implementation of 
* this FunctionalInterface
*
* @author Gethin James
* @since 5.0
**/

//@FunctionalInterface
public interface EventPreparator
{
    public Event prepareEvent(String user, String networkId, String transactionId);
}
