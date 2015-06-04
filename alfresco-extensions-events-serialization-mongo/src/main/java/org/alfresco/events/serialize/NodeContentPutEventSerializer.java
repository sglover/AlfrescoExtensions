/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import static org.alfresco.events.serialize.Fields.FIELD_ENCODING;
import static org.alfresco.events.serialize.Fields.FIELD_MIME_TYPE;
import static org.alfresco.events.serialize.Fields.FIELD_SIZE;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public class NodeContentPutEventSerializer extends AbstractNodeEventSerializer
{
    @Override
    protected DBObject serializeNodeEvent(NodeEvent nodeEvent)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        buildDBObjectFromNodeEvent(builder, nodeEvent);

        if(nodeEvent instanceof NodeContentPutEvent)
        {
            NodeContentPutEvent nodeCPEvent = (NodeContentPutEvent)nodeEvent;
            builder
            .add(FIELD_SIZE, nodeCPEvent.getSize())
            .add(FIELD_ENCODING, nodeCPEvent.getEncoding())
            .add(FIELD_MIME_TYPE, nodeCPEvent.getMimeType());
        }

        return builder.get();
    }

    @Override
    public Event deSerialize(DBObject dbObject)
    {
        NodeContentPutEvent nodeEvent = new NodeContentPutEvent();
        populateNodeEvent(dbObject, nodeEvent);
        Long size = (Long)dbObject.get(FIELD_SIZE);
        nodeEvent.setSize(size);
        String encoding = (String)dbObject.get(FIELD_ENCODING);
        nodeEvent.setEncoding(encoding);
        String mimeType = (String)dbObject.get(FIELD_MIME_TYPE);
        nodeEvent.setMimeType(mimeType);
        return nodeEvent;
    }
}
