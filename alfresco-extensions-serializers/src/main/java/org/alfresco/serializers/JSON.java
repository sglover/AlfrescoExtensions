/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

//@JsonSerialize(contentUsing=JSONContentSerializer.class)
//@JsonDeserialize(contentUsing=JSONContentDeserializer.class)
public class JSON implements Serializable
{
    private static final long serialVersionUID = 5278154969755802307L;

    private Map<String, Object> map = new HashMap<>();

    public static JSON from(DBObject dbObject)
    {
        JSON json = new JSON();
        for(String key : dbObject.keySet())
        {
            Object value = dbObject.get(key);
            json.put(key, value);
        }
        return json;
    }

    public static class JSONContentSerializer extends JsonSerializer<JSON>
    {
		@Override
        public void serialize(JSON value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException
        {
			jgen.writeStartObject();
			for(String key : value.keys())
			{
				Object val = value.get(key);
				jgen.writeObjectField(key, val);
			}
	        jgen.writeEndObject();
        }
    }
    
    public static class JSONContentDeserializer extends JsonDeserializer<JSON>
    {

		@Override
        public JSON deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException
        {
			// TODO
			JSON json = new JSON();
			jp.nextToken();
	        return json;
        }
    }
    
    public Map<String, Object> getMap()
	{
		return map;
	}

	public void setMap(Map<String, Object> map)
	{
		this.map = map;
	}

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

    public XContentBuilder getXContent() throws IOException
    {
        XContentBuilder builder = jsonBuilder();
        for(Map.Entry<String, Object> entry : map.entrySet())
        {
            builder.field(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    public DBObject getDBObject()
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for(Map.Entry<String, Object> entry : map.entrySet())
        {
            builder.append(entry.getKey(), entry.getValue());
        }
        return builder.get();
    }

    @Override
    public String toString()
    {
        return "JSON [map=" + map + "]";
    }
}
