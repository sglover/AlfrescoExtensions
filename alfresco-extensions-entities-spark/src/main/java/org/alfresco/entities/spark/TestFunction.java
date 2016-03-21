/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.spark;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.extensions.common.Node;
import org.alfresco.services.nlp.Entities;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.bson.BSONObject;

import scala.Tuple2;

/**
 * 
 * @author sglover
 *
 */
public class TestFunction implements FlatMapFunction<Tuple2<Object, BSONObject>, Node>
{
	private static final long serialVersionUID = 2539879123053439307L;

	private Entities entities;

	public TestFunction(Entities entities)
	{
		this.entities = entities;
	}
		
	@Override
    public Iterable<Node> call(Tuple2<Object, BSONObject> t)
            throws Exception
    {
		List<Node> ret = new LinkedList<>();

		String type = (String)t._2.get("t");
		switch(type)
		{
		case "name":
		{
			String name = (String)t._2.get("nm");
			if(name != null)
			{
				if(entities.hasName(name))
				{
					String nodeId = (String)t._2.get("n");
					String nodeVersion = (String)t._2.get("v");
					Node node = new Node(nodeId, nodeVersion);
					ret.add(node);
				}
			}
			break;
		}
		case "org":
		{
			String name = (String)t._2.get("nm");
			if(name != null)
			{
				if(entities.hasOrg(name))
				{
					String nodeId = (String)t._2.get("n");
					String nodeVersion = (String)t._2.get("v");
					Node node = new Node(nodeId, nodeVersion);
					ret.add(node);
				}
			}
			break;
		}
		}

		return ret;
	}
}