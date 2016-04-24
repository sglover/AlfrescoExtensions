/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao;

import java.util.Collection;
import java.util.List;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;

/**
 * 
 * @author sglover
 *
 */
public interface EntitiesDAO
{
    Entities getEntities(Node node);

    List<Node> matchingNodes(String type, String name);

//    Entities getEntities(Node node, Set<String> types);

    Collection<Entity<String>> getNames(Node node);
    Collection<Entity<String>> getOrgs(Node node);

//    EntityCounts<String> getEntityCounts(Node node);

//    EntityCounts<String> getNodeMatches(Entities entities);

    void addEntities(Node node, Entities entities);
}
