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
 * An implementation of EventPublisher that delegates to another implementation
 *
 * @author Gethin James
 * @since 5.0
 */
public class DelegatingEventPublisher implements EventPublisher {

    EventPublisher delegate;
    
    @Override
    public void publishEvent(Event event)
    {
        if (delegate!=null) delegate.publishEvent(event);
    }

    @Override
    public void publishEvent(EventPreparator prep)
    {
        if (delegate!=null) delegate.publishEvent(prep);
    }

    /**
     * Register an EventPublisher to do the work
     * @param delegate EventPublisher
     */
    public void registerDelegate(EventPublisher delegate)
    {
        this.delegate = delegate;
    }
    
    /**
     * UnRegister an EventPublisher
     */
    public void unregisterDelegate()
    {
        this.delegate = null;
    }
	
}