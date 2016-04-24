/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.values;

import org.sglover.alfrescoextensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public class Similarity
{
	private Node node;
	private double similarity;

	public Similarity(Node node, double similarity)
    {
	    super();
	    this.node = node;
	    this.similarity = similarity;
    }

	public Node getNode()
	{
		return node;
	}

	public double getSimilarity()
	{
		return similarity;
	}
}
