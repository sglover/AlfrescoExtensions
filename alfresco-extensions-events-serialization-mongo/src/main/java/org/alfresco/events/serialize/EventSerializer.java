/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import org.alfresco.events.node.types.Event;

import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public interface EventSerializer
{
    DBObject serialize(Event event);
    Event deSerialize(DBObject dbObject);
}
