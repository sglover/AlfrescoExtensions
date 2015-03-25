/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic utility class for use with the Jackson json library.
 *
 * @author Gethin James
 */
public class JsonUtil
{
    private static ObjectMapper mapper = new ObjectMapper();
    
    public static String writeData(Map<String, ?> data)
    {
        try
        {
            return mapper.writeValueAsString(data);
        }
        catch (JsonProcessingException error)
        {
            // do nothing
            return "{}";
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public static Map<String, ?> readData(String data)
    {
        try
        {
            return mapper.readValue(data.getBytes(), Map.class);
        }
        catch (IOException error)
        {
            // do nothing
            return null;
        }
    }
}
