/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.serializer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JSON implements Serializable
{
    private static final long serialVersionUID = 5278154969755802307L;

    private Map<String, Object> map = new HashMap<>();
    
    public Object get(String key)
    {
        return map.get(key);
    }

    public void put(String key, Object val)
    {
        map.put(key, val);
    }
    
    public Set<String> keySet()
    {
        return map.keySet();
    }
    
    public Set<String> keys()
    {
        return new HashSet<>(map.keySet());
    }

    @Override
    public String toString()
    {
        return "JSON [map=" + map + "]";
    }
}
