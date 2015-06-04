/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import static org.alfresco.events.serialize.Fields.FIELD_NODE_NEW_NAME;
import static org.alfresco.events.serialize.Fields.FIELD_PRIMARY_TO_PATH;
import static org.alfresco.events.serialize.Fields.FIELD_TO_PARENT_NODE_IDS;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeMovedEvent;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public class NodeMovedEventSerializer extends AbstractNodeEventSerializer
{
    @Override
    protected DBObject serializeNodeEvent(NodeEvent nodeEvent)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        buildDBObjectFromNodeEvent(builder, nodeEvent);
        
        if(nodeEvent instanceof NodeMovedEvent)
        {
            NodeMovedEvent nodeMovedEvent = (NodeMovedEvent)nodeEvent;
            List<String> toPaths = nodeMovedEvent.getToPaths();
            String toPathStr = toPaths.get(0);
            Path toPath = new Path(toPathStr);
            String newName = nodeMovedEvent.getNewName();

            builder
                .add(FIELD_TO_PARENT_NODE_IDS, nodeMovedEvent.getToParentNodeIds())
                .add(FIELD_PRIMARY_TO_PATH, (toPath != null ? toPath.getArrayPath() : null))
                .add(FIELD_NODE_NEW_NAME, newName);
        }

        return builder.get();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Event deSerialize(DBObject dbObject)
    {
        NodeMovedEvent nodeEvent = new NodeMovedEvent();
        populateNodeEvent(dbObject, nodeEvent);

        String newName = (String)dbObject.get(FIELD_NODE_NEW_NAME);
        nodeEvent.setNewName(newName);

        List primaryToPathList = (List)dbObject.get(FIELD_PRIMARY_TO_PATH);
        if(primaryToPathList != null)
        {
	        Path primaryToPath = new Path(primaryToPathList);
	        List<String> toPaths = new ArrayList<>(1);
	        toPaths.add(primaryToPath.getPath());
	        nodeEvent.setToPaths(toPaths);
        }

        List<List<String>> toParentNodeIds = (List)dbObject.get(FIELD_TO_PARENT_NODE_IDS);
        nodeEvent.setToParentNodeIds(toParentNodeIds);

        return nodeEvent;
    }

}
