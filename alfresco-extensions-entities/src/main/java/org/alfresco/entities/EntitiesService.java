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

import org.alfresco.extensions.common.Node;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.nlp.Entity;

/**
 * 
 * @author sglover
 *
 */
public interface EntitiesService
{
	void getEntitiesAsync(final Node node);
	void getEntities(final Node node) throws AuthenticationException, IOException;
//	void getEntities(String txnId, String nodeId, String nodeVersion, String content) throws IOException;
//	void getEntities(String txnId, long nodeInternalId, String nodeId, String nodeVersion) throws IOException, AuthenticationException;
	Collection<Entity<String>> getNames(Node node);
	void calculateSimilarities(String txnId);
	double getSimilarity(Node node1, Node node2);
}
