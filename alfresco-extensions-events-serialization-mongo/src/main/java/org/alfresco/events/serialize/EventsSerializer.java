/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import static org.alfresco.events.serialize.Fields.FIELD_TYPE;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeMovedEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;
import org.alfresco.events.node.types.NodeRenamedEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;

import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public class EventsSerializer
{
    private Map<String, EventSerializer> serializers = new HashMap<>();

    public EventsSerializer()
    {
    }
    
    public void setSerializers(Map<String, EventSerializer> serializers)
    {
        this.serializers = serializers;
    }

    public DBObject serialize(Event event)
    {
        EventSerializer serializer = serializers.get(event.getType());
        if(serializer == null)
        {
            throw new IllegalArgumentException("Cannot serialize events of type " + event.getType());
        }
        DBObject dbObject = serializer.serialize(event);
        return dbObject;
    }
    
    public Event deSerialize(DBObject dbObject)
    {
        String type = (String)dbObject.get(FIELD_TYPE);
        EventSerializer serializer = serializers.get(type);
        Event event = serializer.deSerialize(dbObject);
        return event;
    }

    // TODO this is used in tests that are not driven by Spring configuration. It needs to be removed
    // after we move over to tests driven by Spring configuration.
    public static EventsSerializer createDefault()
    {
        EventsSerializer eventsSerializer = new EventsSerializer();
        Map<String, EventSerializer> serializers = new HashMap<>();
//        serializers.put(ResetAllEvent.EVENT_TYPE, new ResetAllEventSerializer());
//        serializers.put(ResetSubscriptionsEvent.EVENT_TYPE, new ResetSubscriptionsEventSerializer());
        serializers.put(NodeAddedEvent.EVENT_TYPE, new NodeAddedEventSerializer());
        NodeUpdatedEventSerializer nodeUpdatedEventSerializer = new NodeUpdatedEventSerializer();
        nodeUpdatedEventSerializer.setPropertySerializer(new PropertySerializer());
        serializers.put(NodeUpdatedEvent.EVENT_TYPE, nodeUpdatedEventSerializer);
        serializers.put(NodeRemovedEvent.EVENT_TYPE, new NodeRemovedEventSerializer());
        serializers.put(NodeMovedEvent.EVENT_TYPE, new NodeMovedEventSerializer());
        serializers.put(NodeRenamedEvent.EVENT_TYPE, new NodeRenamedEventSerializer());
        serializers.put(NodeContentPutEvent.EVENT_TYPE, new NodeContentPutEventSerializer());
        eventsSerializer.setSerializers(serializers);
        return eventsSerializer;
    }
}
