/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.values;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sglover.nlp.Entity;

public class EntityCounts<T>
{
	private String type;
	private List<Entity<T>> entities = new LinkedList<>();

	public void addEntity(Entity<T> entity)
	{
		entities.add(entity);
	}

	public String getType()
	{
		return type;
	}

	public Iterator<Entity<T>> getEntities()
	{
		return entities.iterator();
	}

	@Override
    public String toString()
    {
	    return "EntityCounts [type=" + type + ", entities=" + entities + "]";
    }
}
