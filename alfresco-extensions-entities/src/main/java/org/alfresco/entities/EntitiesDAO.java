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
	Entities getEntities(Node node, Set<String> types);
	Collection<Entity<String>> getNames(Node node);
	void addEntities(Node node, Entities entities);
	EntityCounts<String> getEntityCounts(Node node);
//	EntityCounts<String> overlap(String nodeId, String nodeVersion);

	List<Entities> getEntities();
	List<Entities> unprocessedEntites();

	void saveSimilarity(Node node1, Node node2, double similarity);
	double getSimilarity(Node node1, Node node2);
}
