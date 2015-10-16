/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.values;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.extensions.common.Node;

public class Nodes implements Serializable
{
	private static final long serialVersionUID = 8255317966237159993L;

	private List<Node> nodes = new LinkedList<>();

	public void add(Node node)
	{
		nodes.add(node);
	}

	public List<Node> getNodes()
	{
		return nodes;
	}

	public void add(Nodes nodes)
	{
		this.nodes.addAll(nodes.getNodes());
	}

	@Override
    public String toString()
    {
        return "Nodes [nodes=" + nodes + "]";
    }
}
