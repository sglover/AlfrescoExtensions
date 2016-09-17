/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;

/**
 * 
 * @author sglover
 *
 */
public class Model
{
    public enum PropertyType { String, Number };

    private Map<String, PropertyType> properties = new HashMap<>();

    public Model(Iterator<VertexProperty<Object>> properties)
    {
        super();
        while(properties.hasNext())
        {
            VertexProperty<Object> vp = properties.next();
            this.properties.put(vp.key(), PropertyType.valueOf((String)vp.value()));
        }
    }

    public void add(String propertyName, PropertyType propertyType)
    {
        properties.put(propertyName, propertyType);
    }

    public Map<String, PropertyType> getProperties()
    {
        return properties;
    }

    
}
