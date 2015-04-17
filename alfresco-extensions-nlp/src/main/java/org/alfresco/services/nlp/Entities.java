/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * @author sglover
 *
 */
public class Entities implements Serializable
{
	private static final long serialVersionUID = -3458489259159059095L;

	private String nodeId;
	private String nodeVersion;

	private Map<String, Entity<String>> locations;
	private Map<String, Entity<String>> names;
	private Map<String, Entity<String>> orgs;
	private Map<String, Entity<String>> dates;
	private Map<String, Entity<String>> money;
	private Map<String, Entity<String>> misc;

	private Entities(String nodeId, String nodeVersion)
	{
		this();
		this.nodeId = nodeId;
		this.nodeVersion = nodeVersion;
	}

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

	public static Entities empty(String nodeId, String nodeVersion)
	{
		Entities entities = new Entities(nodeId, nodeVersion);
		return entities;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public String getNodeVersion()
	{
		return nodeVersion;
	}

	public Collection<Entity<String>> getLocations()
	{
		return locations.values();
	}

	public Entities addLocation(String name, EntityLocation location)
	{
		Entity<String> locationEntity = locations.get(name);
		if(locationEntity == null)
		{
			locationEntity = new Entity<String>("location", name);
			locations.put(name, locationEntity);
		}

		if(location != null)
		{
			locationEntity.addLocation(location);
		}

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

	public Entities addName(String name, EntityLocation location)
	{
		Entity<String> nameEntity = names.get(name);
		if(nameEntity == null)
		{
			nameEntity = new Entity<String>("name", name);
			names.put(name, nameEntity);
		}

		if(location != null)
		{
			nameEntity.addLocation(location);
		}

		return this;
	}

	public Entities addName(String name, String context, double probability)
	{
		long beginOffset = context.indexOf(name);
		long endOffset = beginOffset + name.length();
		EntityLocation location = new EntityLocation(beginOffset, endOffset, probability, context);
		addName(name, location);

		return this;
	}

	public Entities addName(String name)
	{
		addName(name, null);

		return this;
	}

	public Collection<Entity<String>> getOrgs()
	{
		return orgs.values();
	}
	
	public Entities addOrg(String name, EntityLocation location)
	{
		Entity<String> orgEntity = orgs.get(name);
		if(orgEntity == null)
		{
			orgEntity = new Entity<String>("org", name);
			orgs.put(name, orgEntity);
		}

		if(location != null)
		{
			orgEntity.addLocation(location);
		}

		return this;
	}

	public Entities addOrg(String name, String context, double probability)
	{
		long beginOffset = context.indexOf(name);
		long endOffset = beginOffset + name.length();
		EntityLocation location = new EntityLocation(beginOffset, endOffset, probability, context);
		addOrg(name, location);

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
				addName(name, location);
				break;
			}
			case "location":
			{
				addLocation(name, location);
				break;
			}
			case "org":
			{
				addOrg(name, location);
				break;
			}
			case "misc":
			{
				addMisc(name, location);
				break;
			}
			case "money":
			{
				addMoney(name, location);
				break;
			}
			case "date":
			{
				addMoney(name, location);
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

	public void addDate(String name, EntityLocation location)
	{
		Entity<String> dateEntity = dates.get(name);
		if(dateEntity == null)
		{
			dateEntity = new Entity<String>("date", name);
			dates.put(name, dateEntity);
		}

		if(location != null)
		{
			dateEntity.addLocation(location);
		}
	}

	public Collection<Entity<String>> getMoney()
	{
		return money.values();
	}

	public void addMoney(String name, EntityLocation location)
	{
		Entity<String> moneyEntity = money.get(name);
		if(moneyEntity == null)
		{
			moneyEntity = new Entity<String>("money", name);
			money.put(name, moneyEntity);
		}

		if(location != null)
		{
			moneyEntity.addLocation(location);
		}
	}

	public Collection<Entity<String>> getMisc()
	{
		return misc.values();
	}

	public void addMisc(String name, EntityLocation location)
	{
		Entity<String> miscEntity = misc.get(name);
		if(miscEntity == null)
		{
			miscEntity = new Entity<String>("misc", name);
			misc.put(name, miscEntity);
		}

		if(location != null)
		{
			miscEntity.addLocation(location);
		}
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

	private Set<String> getAsSet(final Collection<Entity<String>> entities)
	{
		final Set<String> set = new AbstractSet<String>()
		{
			@Override
            public Iterator<String> iterator()
            {
				final Iterator<Entity<String>> entitiesIt = entities.iterator();
				final Iterator<String> it = new Iterator<String>()
				{
					@Override
		            public boolean hasNext()
		            {
		                return entitiesIt.hasNext();
		            }

					@Override
		            public String next()
		            {
						Entity<String> entity = entitiesIt.next();
		                return entity.getEntity();
		            }

					@Override
		            public void remove()
		            {
						throw new UnsupportedOperationException();
		            }
				};
                return it;
            }

			@Override
            public int size()
            {
                return entities.size();
            }
		};

		return set;
	}

	public Set<String> getOrgsAsSet()
	{
		final Collection<Entity<String>> orgs = getOrgs();
		return getAsSet(orgs);
	}

	public Set<String> getLocationsAsSet()
	{
		final Collection<Entity<String>> locations = getLocations();
		return getAsSet(locations);
	}

	public Set<String> getNamesAsSet()
	{
		final Collection<Entity<String>> names = getNames();
		return getAsSet(names);
	}

	public Set<String> getEntitiesAsSet()
	{
		final Set<String> set = new EntitySet();
		return set;
	}

	private class EntitySet extends AbstractSet<String>
	{
		private EntitySet()
		{
		}

		@Override
        public Iterator<String> iterator()
        {
			Iterator<String> it = new EntityIterator();
	        return it;
        }

		@Override
        public int size()
        {
	        return names.size() + locations.size() + orgs.size();
        }
	}

	private class EntityIterator implements Iterator<String>
	{
		private LinkedList<Iterator<Entity<String>>> l = new LinkedList<>();
		private Iterator<Entity<String>> entityIt = null;

		private EntityIterator()
		{
			l.add(names.values().iterator());
			l.add(locations.values().iterator());
			l.add(orgs.values().iterator());
		}

		@Override
        public boolean hasNext()
        {
			while(l.peek() != null && (entityIt == null || !entityIt.hasNext()))
			{
				entityIt = l.pop();
			}

            boolean hasNext = entityIt.hasNext();
            return hasNext;
        }

		@Override
        public String next()
        {
			Entity<String> entity = entityIt.next();
            return entity.getEntity();
        }

		@Override
        public void remove()
        {
			throw new UnsupportedOperationException();
        }
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
