/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.io.IOException;
import java.util.Collection;

import org.alfresco.entities.values.Node;
import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.TransactionCommittedEvent;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.nlp.Entity;

/**
 * 
 * @author sglover
 *
 */
public interface EntitiesService
{
	Collection<Entity<String>> getNames(Node node);
	void getEntitiesForEvent(Event event) throws IOException, AuthenticationException;
//	void getEntitiesForEventAsync(NodeContentPutEvent nodeEvent) throws AuthenticationException, IOException;
//	void getEntitiesForEvent(NodeContentPutEvent nodeEvent) throws AuthenticationException, IOException;
//	void getEntitiesForEvent(NodeUpdatedEvent nodeEvent) throws IOException;
//	void getEntitiesForEvent(NodeAddedEvent nodeEvent) throws IOException;
	double getSimilarity(Node node1, Node node2);
	void txnCommitted(TransactionCommittedEvent event);
}
