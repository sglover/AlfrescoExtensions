/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import static org.alfresco.events.serialize.Fields.FIELD_EVENT_TIMESTAMP;
import static org.alfresco.events.serialize.Fields.FIELD_ID;
import static org.alfresco.events.serialize.Fields.FIELD_TYPE;
import static org.alfresco.events.serialize.Fields.FIELD_USER_ID;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.EventImpl;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public abstract class AbstractEventSerializer implements EventSerializer
{
    protected void populateEvent(DBObject dbObject, EventImpl event)
    {
        String userId = (String)dbObject.get(FIELD_USER_ID);
        event.setUsername(userId);
        String type = (String)dbObject.get(FIELD_TYPE);
        event.setType(type);
        Long eventTime = (Long)dbObject.get(FIELD_EVENT_TIMESTAMP);
        event.setTimestamp(eventTime);
    }

    protected void buildDBObjectFromEvent(BasicDBObjectBuilder builder, Event event)
    {
        builder
        .add(FIELD_EVENT_TIMESTAMP, event.getTimestamp())
        .add(FIELD_ID, event.getId())
        .add(FIELD_TYPE, event.getType().toString())
        .add(FIELD_USER_ID, event.getUsername());
    }
}
