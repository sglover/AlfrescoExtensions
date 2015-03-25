/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.apache.spark.Accumulable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;

/**
 * 
 * @author sglover
 *
 */
public class SparkService implements Serializable
{
	private static final long serialVersionUID = 216988455735396538L;

	private EntitiesDAO entitiesDAO;
	private JavaSparkContext sc;

	public void setEntitiesDAO(EntitiesDAO entitiesDAO)
	{
		this.entitiesDAO = entitiesDAO; 
	}

//	private static class NodeAccumulator extends Accumulable<List<Node>, Node>
//	{
//		private static final long serialVersionUID = 4915738007074662937L;
//
//        public NodeAccumulator(List<Node> initialValue,
//                AccumulableParam<List<Node>, Node> param)
//        {
//	        super(initialValue, param);
//	        // TODO Auto-generated constructor stub
//        }
//	}

	public void init()
	{
		SparkConf conf = new SparkConf().setAppName("Entities").setMaster("spark://localhost:7077");
		this.sc = new JavaSparkContext(conf);
	}

	public void shutdown()
	{
		sc.close();
	}

	public Nodes matchingNodes(long nodeInternalId, long nodeVersion)
	{
	    JavaRDD<Node> initialNodes = sc.parallelize(Arrays.asList(new Node(1l, 1l)));

	    JavaRDD<Entity<String>> names = initialNodes.flatMap(new FlatMapFunction<Node, Entity<String>>()
		{
	    	private static final long serialVersionUID = 1L;

			public Iterable<Entity<String>> call(Node node) throws Exception
	    	{
				long nodeInternalId = node.getNodeInternalId();
				long nodeVersion = node.getNodeVersion();
				Entities entities = entitiesDAO.getEntities(nodeInternalId, nodeVersion, Collections.singleton("name"));
				return entities.getNames();
	    	}
		});

	    JavaRDD<Node> matchingNodes = names.flatMap(new FlatMapFunction<Entity<String>, Node>()
		{
	    	private static final long serialVersionUID = 1L;

			public Iterable<Node> call(Entity<String> entity) throws Exception
	    	{
				String type = entity.getType();
				String name = entity.getEntity();
				return entitiesDAO.matchingNodes(type, name);
	    	}
		});

	    Nodes nodes = new Nodes();
    	final Accumulable<Nodes, Node> nodesAccumulator = sc.accumulable(nodes, new NodesAccumulableParam());

	    matchingNodes.foreach(new VoidFunction<Node>()
	    {
            private static final long serialVersionUID = 1L;

			public void call(Node node) throws Exception
	    	{
				nodesAccumulator.add(node);
	    	}
	    });

	    return nodesAccumulator.value();
	}
}
