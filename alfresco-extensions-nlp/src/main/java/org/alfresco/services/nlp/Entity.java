/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author sglover
 *
 * @param <T>
 */
public class Entity<T> implements Serializable
{
	private static final long serialVersionUID = -812303299537246310L;

	private String type;
	private List<EntityLocation> locations = new LinkedList<>();
	private T entity;
	private AtomicLong count = new AtomicLong();

	public Entity(String type, T entity, long count)
    {
	    super();
	    this.type = type;
	    this.entity = entity;
	    this.count.set(count);
    }

	public Entity(String type, T entity)
    {
	    super();
	    this.type = type;
	    this.entity = entity;
    }

	public String getType()
	{
		return type;
	}

	public void addLocation(EntityLocation entityLocation)
	{
//		EntityLocation entityLocation = new EntityLocation(startOffset, endOffset, probability, context);
		locations.add(entityLocation);
		count.incrementAndGet();
	}

	public T getEntity()
	{
		return entity;
	}

	public long getCount()
	{
		return count.longValue();
	}

	public List<EntityLocation> getLocations()
	{
		return locations;
	}

	@Override
    public String toString()
    {
	    return "Entity [type=" + type + ", locations=" + locations
	            + ", entity=" + entity + ", count=" + count + "]";
    }
}
