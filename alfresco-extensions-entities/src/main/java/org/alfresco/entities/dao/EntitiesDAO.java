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
import org.alfresco.extensions.common.Node;
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
	List<Entities> getEntities();
	List<Entities> getEntitiesForTxn(String txnId);
	Collection<Entity<String>> getNames(Node node);
	EntityCounts<String> getEntityCounts(Node node);

	EntityCounts<String> getNodeMatches(Entities entities);

	void addEntities(String txnId, Node node, Entities entities);

//	void txnCommitted(TransactionCommittedEvent event);

	List<Entities> unprocessedEntites();
}
