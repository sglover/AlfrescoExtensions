/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.entities.values.EntityCounts;
import org.alfresco.entities.values.Node;
import org.alfresco.events.node.types.TransactionCommittedEvent;
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
	void addEntities(String txnId, Node node, Entities entities);
	EntityCounts<String> getEntityCounts(Node node);
//	EntityCounts<String> overlap(String nodeId, String nodeVersion);

	EntityCounts<String> getNodeMatches(Entities entities);

	void txnCommitted(TransactionCommittedEvent event);

	List<Entities> getEntities();
	List<Entities> unprocessedEntites();

	List<Entities> getEntitiesForTxn(String txnId);
}
