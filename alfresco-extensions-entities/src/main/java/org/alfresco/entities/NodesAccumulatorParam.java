/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import org.apache.spark.AccumulatorParam;

public class NodesAccumulatorParam implements AccumulatorParam<Nodes>
{
	private static final long serialVersionUID = 5790626892788482296L;

	@Override
	public Nodes addInPlace(Nodes nodes1, Nodes nodes2)
	{
		nodes1.add(nodes2);
		return nodes1;
	}

	@Override
	public Nodes zero(Nodes nodes)
	{
		return nodes;
	}

	@Override
	public Nodes addAccumulator(Nodes nodes1, Nodes nodes2)
	{
		nodes1.add(nodes2);
		return nodes1;
	}

}
