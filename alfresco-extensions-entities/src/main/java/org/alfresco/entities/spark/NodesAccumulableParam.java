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
import org.apache.spark.AccumulableParam;

/**
 * 
 * @author sglover
 *
 */
public class NodesAccumulableParam implements AccumulableParam<Nodes, Node>
{
	private static final long serialVersionUID = 294606275996035580L;

	public Nodes addAccumulator(Nodes nodes, Node node)
	{
		nodes.add(node);
		return nodes;
	}

	public Nodes addInPlace(Nodes nodes1, Nodes nodes2)
	{
		nodes1.add(nodes2);
		return nodes1;
	}

	public Nodes zero(Nodes nodes)
	{
		return nodes;
	}
}
