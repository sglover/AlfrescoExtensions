/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import static org.alfresco.events.serialize.Fields.FIELD_CHECKED_OUT_NODE_ID;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeCheckedOutEvent;
import org.alfresco.events.node.types.NodeEvent;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public class NodeCheckedOutEventSerializer extends AbstractNodeEventSerializer
{
    @Override
    protected DBObject serializeNodeEvent(NodeEvent nodeEvent)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        buildDBObjectFromNodeEvent(builder, nodeEvent);

        if(nodeEvent instanceof NodeCheckedOutEvent)
        {
        	NodeCheckedOutEvent nodeCheckedOutEvent = (NodeCheckedOutEvent)nodeEvent;
	        builder
	        	.add(FIELD_CHECKED_OUT_NODE_ID, nodeCheckedOutEvent.getCheckedOutNodeId());
        }

        return builder.get();
    }

    @Override
    public Event deSerialize(DBObject dbObject)
    {
        NodeCheckedOutEvent nodeEvent = new NodeCheckedOutEvent();
        populateNodeEvent(dbObject, nodeEvent);
        String checkedOutNodeId = (String)dbObject.get(FIELD_CHECKED_OUT_NODE_ID);
        nodeEvent.setCheckedOutNodeId(checkedOutNodeId);
        return nodeEvent;
    }

}
