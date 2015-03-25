/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch.entities;

import java.util.List;

public class ElasticSearchEntity
{
	private String name;
	private List<Object> values;

	public ElasticSearchEntity(String name, List<Object> values)
    {
	    super();
	    this.name = name;
	    this.values = values;
    }

	public String getName()
	{
		return name;
	}
	public List<Object> getValues()
	{
		return values;
	}

	
}
