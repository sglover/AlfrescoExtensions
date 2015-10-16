/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

/**
 * 
 * @author sglover
 *
 */
public enum IndexType
{
	monitoring
    {
    	public String getName()
    	{
    		return "monitoring";
    	}
    },

	sync
    {
    	public String getName()
    	{
    		return "sync";
    	}
    },

    doc
    {
    	public String getName()
    	{
    		return "doc";
    	}
    },
    
    node
    {
    	public String getName()
    	{
    		return "node";
    	}
    },

    content
    {
    	public String getName()
    	{
    		return "content";
    	}
    },
    
    event
    {
    	public String getName()
    	{
    		return "event";
    	}
    };
    
	public abstract String getName();
}
