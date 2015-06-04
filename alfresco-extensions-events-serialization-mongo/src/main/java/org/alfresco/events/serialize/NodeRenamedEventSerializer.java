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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeRenamedEvent;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public class NodeRenamedEventSerializer extends AbstractNodeEventSerializer
{
    @Override
    protected DBObject serializeNodeEvent(NodeEvent nodeEvent)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        buildDBObjectFromNodeEvent(builder, nodeEvent);
        
        if(nodeEvent instanceof NodeRenamedEvent)
        {
            NodeRenamedEvent nodeRenamedEvent = (NodeRenamedEvent)nodeEvent;
            
            List<String> toPaths = nodeRenamedEvent.getToPaths();
            String toPathStr = toPaths.get(0);
            Path toPath = new Path(toPathStr);

            String newName = nodeRenamedEvent.getNewName();

            builder
            	.add(FIELD_PRIMARY_TO_PATH, (toPath != null ? toPath.getArrayPath() : null))
            	.add(FIELD_NODE_NEW_NAME, newName);
        }

        return builder.get();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public Event deSerialize(DBObject dbObject)
    {
        NodeRenamedEvent nodeEvent = new NodeRenamedEvent();
        populateNodeEvent(dbObject, nodeEvent);

        List primaryToPathList = (List)dbObject.get(FIELD_PRIMARY_TO_PATH);
        if(primaryToPathList != null)
        {
	        Path primaryToPath = new Path(primaryToPathList);
	        List<String> toPaths = new ArrayList<>(1);
	        toPaths.add(primaryToPath.getPath());
	        nodeEvent.setToPaths(toPaths);
        }

        String newName = (String)dbObject.get(FIELD_NODE_NEW_NAME);
        nodeEvent.setNewName(newName);

        return nodeEvent;
    }

}
