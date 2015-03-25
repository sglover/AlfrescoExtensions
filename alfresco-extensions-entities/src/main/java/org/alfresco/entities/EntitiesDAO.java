/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;

/**
 * 
 * @author sglover
 *
 */
public interface EntitiesDAO
{
	List<Node> matchingNodes(String type, String name);
	Entities getEntities(long nodeId, long nodeVersion, Set<String> types);
	Collection<Entity<String>> getNames(long nodeInternalId, long nodeVersion);
	void addEntities(long nodeInternalId, long nodeVersion, Entities entities);
	EntityCounts<String> getEntityCounts(long nodeInternalId, long nodeVersion);
	EntityCounts<String> overlap(long nodeInternalId, long nodeVersion);
}
