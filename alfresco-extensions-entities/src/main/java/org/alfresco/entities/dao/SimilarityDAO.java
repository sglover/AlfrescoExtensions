/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.dao;

import java.util.List;

import org.alfresco.entities.values.Node;
import org.alfresco.entities.values.Similarity;

/**
 * 
 * @author sglover
 *
 */
public interface SimilarityDAO
{
	void saveSimilarity(Node node1, Node node2, double similarity);
	double getSimilarity(Node node1, Node node2);
	List<Similarity> getSimilar(Node node);
}
