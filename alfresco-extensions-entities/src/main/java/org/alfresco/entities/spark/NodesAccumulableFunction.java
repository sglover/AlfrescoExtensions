/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.spark;

import org.alfresco.entities.values.Node;
import org.alfresco.entities.values.Nodes;
import org.apache.spark.Accumulable;
import org.apache.spark.api.java.function.VoidFunction;

/**
 * 
 * @author sglover
 *
 */
public class NodesAccumulableFunction implements VoidFunction<Node>
{
    private static final long serialVersionUID = -6018327238356710468L;

	private Accumulable<Nodes, Node> nodesAccumulator;

    public NodesAccumulableFunction(Accumulable<Nodes, Node> nodesAccumulator)
    {
    	this.nodesAccumulator = nodesAccumulator;
    }

	public void call(Node node) throws Exception
	{
		nodesAccumulator.add(node);
	}
}
