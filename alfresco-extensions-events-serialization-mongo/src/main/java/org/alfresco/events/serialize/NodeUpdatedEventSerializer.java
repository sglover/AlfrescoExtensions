/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
import org.alfresco.events.node.types.Property;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public class NodeUpdatedEventSerializer extends AbstractNodeEventSerializer
{
	private PropertySerializer propertySerializer;

    public void setPropertySerializer(PropertySerializer propertySerializer)
    {
		this.propertySerializer = propertySerializer;
	}

	@Override
    protected DBObject serializeNodeEvent(NodeEvent nodeEvent)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        buildDBObjectFromNodeEvent(builder, nodeEvent);
        if(nodeEvent instanceof NodeUpdatedEvent)
        {
        	NodeUpdatedEvent nodeUpdatedEvent = (NodeUpdatedEvent)nodeEvent;

        	Set<String> aspectsAdded = nodeUpdatedEvent.getAspectsAdded();
        	if(aspectsAdded != null)
        	{
        		builder.add("aspectsAdded", aspectsAdded);
        	}

        	Set<String> aspectsRemoved = nodeUpdatedEvent.getAspectsRemoved();
        	if(aspectsRemoved != null)
        	{
        		builder.add("aspectsRemoved", aspectsRemoved);
        	}

        	Map<String, Property> propsAdded = nodeUpdatedEvent.getPropertiesAdded();
        	if(propsAdded != null)
        	{
	        	List<DBObject> propertiesAdded = new ArrayList<DBObject>(propsAdded.size());
	        	for(Map.Entry<String, Property> entry : propsAdded.entrySet())
	        	{
	        		Property prop = entry.getValue();
	        		DBObject propDbObject = propertySerializer.serialize(prop);
	        		propertiesAdded.add(propDbObject);
	        	}
	        	builder.add("propertiesAdded", propertiesAdded);
        	}

        	Map<String, Property> propsChanged = nodeUpdatedEvent.getPropertiesChanged();
        	if(propsChanged != null)
        	{
	        	List<DBObject> propertiesChanged = new ArrayList<DBObject>(propsChanged.size());
	        	for(Map.Entry<String, Property> entry : propsChanged.entrySet())
	        	{
	        		Property prop = entry.getValue();
	        		DBObject propDbObject = propertySerializer.serialize(prop);
	        		propertiesChanged.add(propDbObject);
	        	}
	        	builder.add("propertiesChanged", propertiesChanged);
        	}

        	Set<String> propsRemoved = nodeUpdatedEvent.getPropertiesRemoved();
        	if(propsRemoved != null)
        	{
        		builder.add("propertiesRemoved", propsRemoved);
        	}
        }
        return builder.get();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public Event deSerialize(DBObject dbObject)
    {
        NodeUpdatedEvent nodeEvent = new NodeUpdatedEvent();
        populateNodeEvent(dbObject, nodeEvent);

        Set<String> aspectsAdded = new HashSet((List)dbObject.get("aspectsAdded"));
        nodeEvent.setAspectsAdded(aspectsAdded);

        Set<String> aspectsRemoved = new HashSet((List)dbObject.get("aspectsRemoved"));
        nodeEvent.setAspectsRemoved(aspectsRemoved);

        Set<String> propertiesRemoved = new HashSet((List)dbObject.get("propertiesRemoved"));
        nodeEvent.setPropertiesRemoved(propertiesRemoved);

        List<DBObject> dbPropertiesAdded = (List<DBObject>)dbObject.get("propertiesAdded");
        Map<String, Property> propertiesAdded = new HashMap<String, Property>();
        for(DBObject propDBObject : dbPropertiesAdded)
        {
        	Property property = propertySerializer.deserialize(propDBObject);
        	propertiesAdded.put(property.getName(), property);
        }
        nodeEvent.setPropertiesAdded(propertiesAdded);

        List<DBObject> dbPropertiesChanged = (List<DBObject>)dbObject.get("propertiesChanged");
        Map<String, Property> propertiesChanged = new HashMap<String, Property>();
        for(DBObject propDBObject : dbPropertiesChanged)
        {
        	Property property = propertySerializer.deserialize(propDBObject);
        	propertiesChanged.put(property.getName(), property);
        }
        nodeEvent.setPropertiesChanged(propertiesChanged);

        return nodeEvent;
    }

}
