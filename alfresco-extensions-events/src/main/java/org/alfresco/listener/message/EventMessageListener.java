/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.listener.message;

import org.alfresco.events.node.types.Event;


/**
 * Something that listens to Alfresco events.
 * Basic interface to implement when listening to Events.
 *
 * @author Gethin James
 * @since 5.0
 */
public interface EventMessageListener {

	void onEvent(Event event);
}
