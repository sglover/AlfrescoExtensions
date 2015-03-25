/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author sglover
 *
 */
public class Entities implements Serializable
{
	private static final long serialVersionUID = -3458489259159059095L;

	private Map<String, Entity<String>> locations;
	private Map<String, Entity<String>> names;
	private Map<String, Entity<String>> orgs;
	private Map<String, Entity<String>> dates;
	private Map<String, Entity<String>> money;
	private Map<String, Entity<String>> misc;

	private Entities()
	{
		locations = new TreeMap<String, Entity<String>>();
		names = new TreeMap<String, Entity<String>>();
		orgs = new TreeMap<String, Entity<String>>();
		dates = new TreeMap<String, Entity<String>>();
		money = new TreeMap<String, Entity<String>>();
		misc = new TreeMap<String, Entity<String>>();
	}

	public static Entities empty()
	{
		Entities entities = new Entities();
		return entities;
	}

	public Collection<Entity<String>> getLocations()
	{
		return locations.values();
	}

	public Entities addLocation(String name, String context, long beginOffset, long endOffset, double probability)
	{
		Entity<String> locationEntity = locations.get(name);
		if(locationEntity == null)
		{
			locationEntity = new Entity<String>("location", name);
			locations.put(name, locationEntity);
		}
		locationEntity.addLocation(beginOffset, endOffset, probability, context);

		return this;
	}

	public Collection<Entity<String>> getNames()
	{
		return names.values();
	}

	public boolean hasName(String name)
	{
		return names.containsKey(name);
	}

	public boolean hasOrg(String org)
	{
		return orgs.containsKey(org);
	}

	public Entities addName(String name, String context, long beginOffset, long endOffset, double probability)
	{
		Entity<String> nameEntity = names.get(name);
		if(nameEntity == null)
		{
			nameEntity = new Entity<String>("name", name);
			names.put(name, nameEntity);
		}
		nameEntity.addLocation(beginOffset, endOffset, probability, context);

		return this;
	}

	public Entities addName(String name, String context, double probability)
	{
		long beginOffset = context.indexOf(name);
		long endOffset = beginOffset + name.length(); 
		addName(name, context, beginOffset, endOffset, probability);

		return this;
	}

	public Collection<Entity<String>> getOrgs()
	{
		return orgs.values();
	}
	
	public Entities addOrg(String name, String context, long beginOffset, long endOffset, double probability)
	{
		Entity<String> orgEntity = orgs.get(name);
		if(orgEntity == null)
		{
			orgEntity = new Entity<String>("org", name);
			orgs.put(name, orgEntity);
		}
		orgEntity.addLocation(beginOffset, endOffset, probability, context);

		return this;
	}

	public Entities addOrg(String name, String context, double probability)
	{
		long beginOffset = context.indexOf(name);
		long endOffset = beginOffset + name.length(); 
		addOrg(name, context, beginOffset, endOffset, probability);

		return this;
	}

	public Entities addEntity(Entity<String> entity)
	{
		String name = entity.getEntity();
		String type = entity.getType();
		List<EntityLocation> locations = entity.getLocations();
		for(EntityLocation location : locations)
		{
			switch(type)
			{
			case "name":
			{
				String context = location.getContext();
				long beginOffset = location.getStartOffset();
				long endOffset = location.getEndOffset();
				double probability = location.getProbability();

				addName(name, context, beginOffset, endOffset, probability);
				break;
			}
			case "location":
			{
				String context = location.getContext();
				long beginOffset = location.getStartOffset();
				long endOffset = location.getEndOffset();
				double probability = location.getProbability();

				addLocation(name, context, beginOffset, endOffset, probability);
				break;
			}
			case "org":
			{
				String context = location.getContext();
				long beginOffset = location.getStartOffset();
				long endOffset = location.getEndOffset();
				double probability = location.getProbability();

				addOrg(name, context, beginOffset, endOffset, probability);
				break;
			}
			case "misc":
			{
				String context = location.getContext();
				long beginOffset = location.getStartOffset();
				long endOffset = location.getEndOffset();
				double probability = location.getProbability();

				addMisc(name, context, beginOffset, endOffset, probability);
				break;
			}
			case "money":
			{
				String context = location.getContext();
				long beginOffset = location.getStartOffset();
				long endOffset = location.getEndOffset();
				double probability = location.getProbability();

				addMoney(name, context, beginOffset, endOffset, probability);
				break;
			}
			case "date":
			{
				String context = location.getContext();
				long beginOffset = location.getStartOffset();
				long endOffset = location.getEndOffset();
				double probability = location.getProbability();

				addMoney(name, context, beginOffset, endOffset, probability);
				break;
			}
			default:
				// TODO
			}
		}

		return this;
	}

	public Collection<Entity<String>> getDates()
	{
		return dates.values();
	}

	public void addDate(String name, String context, long beginOffset, long endOffset, double probability)
	{
		Entity<String> dateEntity = dates.get(name);
		if(dateEntity == null)
		{
			dateEntity = new Entity<String>("date", name);
			dates.put(name, dateEntity);
		}
		dateEntity.addLocation(beginOffset, endOffset, probability, context);
	}

	public Collection<Entity<String>> getMoney()
	{
		return money.values();
	}

	public void addMoney(String name, String context, long beginOffset, long endOffset, double probability)
	{
		Entity<String> moneyEntity = money.get(name);
		if(moneyEntity == null)
		{
			moneyEntity = new Entity<String>("money", name);
			money.put(name, moneyEntity);
		}
		moneyEntity.addLocation(beginOffset, endOffset, probability, context);
	}

	public Collection<Entity<String>> getMisc()
	{
		return misc.values();
	}

	public void addMisc(String name, String context, long beginOffset, long endOffset, double probability)
	{
		Entity<String> miscEntity = misc.get(name);
		if(miscEntity == null)
		{
			miscEntity = new Entity<String>("misc", name);
			misc.put(name, miscEntity);
		}
		miscEntity.addLocation(beginOffset, endOffset, probability, context);
	}

	private void appendEntities(String entityName, StringBuilder sb, Collection<Entity<String>> entities)
	{
		sb.append(entityName);
		sb.append(":\n");
        for(Entity<String> entity : entities)
        {
            sb.append("      ");
            sb.append(entity.getEntity());
            sb.append(" (");
            sb.append(entity.getCount());
            sb.append(") | ");
            sb.append(" (");
            sb.append(entity.getLocations());
            sb.append(") ");
    		sb.append("\n");
        }
		sb.append("\n");
	}

	public String toString()
	{
        StringBuilder sb = new StringBuilder();
        appendEntities("Names", sb, getNames());
        appendEntities("Locations", sb, getLocations());
        appendEntities("Orgs", sb, getOrgs());
        appendEntities("Misc", sb, getMisc());
        appendEntities("Money", sb, getMoney());
        appendEntities("Dates", sb, getDates());
        return sb.toString();
	}
}
