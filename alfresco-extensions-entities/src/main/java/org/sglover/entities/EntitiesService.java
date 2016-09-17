/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Stream;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.nlp.Entity;

/**
 * 
 * @author sglover
 *
 */
public interface EntitiesService
{
    void getEntities(Node node, ReadableByteChannel channel) throws IOException;

    Stream<Entity<String>> getNames(Node node, int skip, int maxItems);
    Stream<Entity<String>> getOrgs(Node node, int skip, int maxItems);

//    void calculateSimilarities(String txnId);

    double getSimilarity(Node node1, Node node2);
}
