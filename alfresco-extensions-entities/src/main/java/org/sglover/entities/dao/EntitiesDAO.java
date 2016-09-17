/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao;

import java.util.stream.Stream;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;
import org.sglover.nlp.EntityType;

/**
 * 
 * @author sglover
 *
 */
public interface EntitiesDAO
{
    Entities getEntities(Node node);

    Stream<Node> matchingNodes(EntityType type, String name);

    Stream<Entity<String>> getNames(Node node, int skip, int maxItems);
    Stream<Entity<String>> getOrgs(Node node, int skip, int maxItems);

    void addEntities(Node node, Entities entities);
}
