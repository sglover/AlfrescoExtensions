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
 * EventPublisher can be used to broadcast events.
 *
 * @author Gethin James
 * @since 5.0
 */
public interface EventPublisher
{    
    /**
     * Publish the event
     * @param event Event
     */
    void publishEvent(Event event);

    /**
     * Publish the event using an EventPreparator
     * @param prep EventPreparator
     */
    void publishEvent(EventPreparator prep);
}
