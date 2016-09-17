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
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;

/**
 * 
 * @author sglover
 *
 */
public class Properties
{
    public static class Property
    {
        private String name;
        private Object value;

        public Property(String name, Object value)
        {
            super();
            this.name = name;
            this.value = value;
        }
        public String getName()
        {
            return name;
        }
        public Object getValue()
        {
            return value;
        }
    }

    private Map<String, Object> properties = new HashMap<>();

    public static Properties empty()
    {
        return new Properties();
    }

    public Properties()
    {
    }

    public void add(String key, Object value)
    {
        properties.put(key, value);
    }

    public Properties(List<Property> properties)
    {
        for(Property property : properties)
        {
            this.properties.put(property.getName(), property.getValue());
        }
    }

    public static Properties fromVertex(Iterator<VertexProperty<Object>> properties)
    {
        Properties props = new Properties();
        while(properties.hasNext())
        {
            VertexProperty<Object> vp = properties.next();
            props.add(vp.key(), vp.value());
        }
        return props;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }
}
