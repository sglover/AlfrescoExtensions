/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeCheckOutCancelledEvent;
import org.alfresco.events.node.types.NodeEvent;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public class NodeCheckOutCancelledEventSerializer extends AbstractNodeEventSerializer
{
    @Override
    protected DBObject serializeNodeEvent(NodeEvent nodeEvent)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        buildDBObjectFromNodeEvent(builder, nodeEvent);
        return builder.get();
    }

    @Override
    public Event deSerialize(DBObject dbObject)
    {
        NodeCheckOutCancelledEvent nodeEvent = new NodeCheckOutCancelledEvent();
        populateNodeEvent(dbObject, nodeEvent);
        return nodeEvent;
    }

}
