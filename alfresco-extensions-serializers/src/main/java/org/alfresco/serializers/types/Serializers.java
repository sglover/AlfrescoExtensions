/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers.types;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;

/**
 * 
 * @author sglover
 *
 */
public class Serializers implements SerializerRegistry
{
	private Map<String, Serializer> serializers = new ConcurrentHashMap<>();

	@Override
	public void registerSerializer(String type, Serializer serializer)
	{
		serializers.put(type, serializer);
	}

	@Override
	public Serializer getSerializer(String type)
	{
		Serializer serializer = serializers.get(type);
		return serializer;
	}

    @SuppressWarnings("unchecked")
    @Override
	public Object serialize(Object value)
    {
    	Object ret = null;

        if(value != null)
        {
	    	if (value instanceof Map<?, ?>)
	        {
	            Map<Serializable, Serializable> map = (Map<Serializable, Serializable>) value;
	            // Persist the individual entries
	            for (Map.Entry<Serializable, Serializable> entry : map.entrySet())
	            {
	                // Recurse for each value
	                Serializable mapKey = entry.getKey();
	                Serializable mapValue = entry.getValue();

	                Object serializedKey = serialize(mapKey);
	                Object serializedValue = serialize(mapValue);

	                String key = null;
	                if(serializedKey instanceof String)
	                {
	                	key = (String)serializedKey;
	                }
	                else
	                {
	                	key = serializedKey.toString();
	                }
	            	BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
	                builder.add(key, serializedValue);
	                ret = builder.get();
	            }
	        }
	        else if (value instanceof Collection<?>)
	        {
                Collection<Serializable> collection = (Collection<Serializable>) value;

            	BasicDBList values = new BasicDBList();
	
	            // Persist the individual entries
	            for (Serializable collectionValue : collection)
	            {
	
	                Object mValue = serialize(collectionValue);
	                values.add(mValue);
	            }
	
	            ret = values;
	        }
	        else
	        {
	        	String valueName = value.getClass().getName();
	    		Serializer serializer = getSerializer(valueName);
	    		if(serializer != null)
	    		{
		    		ret = serializer.serialize(value);
	    		}
				else
				{
					ret = value;
				}
	        }
        }

    	return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
	public <T> T deserialize(Class<T> c, Object value)
    {
    	T ret = null;

        if(value != null)
        {
        	String valueName = c.getName();
    		Serializer serializer = getSerializer(valueName);
    		if(serializer != null)
    		{
	    		ret = (T)serializer.deSerialize(value);
    		}
			else
			{
				ret = (T)value;
			}
        }

    	return ret;
    }
}
