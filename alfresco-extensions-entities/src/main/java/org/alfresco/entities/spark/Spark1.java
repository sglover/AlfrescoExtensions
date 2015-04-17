/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.spark;

import java.io.Serializable;

import org.alfresco.entities.values.Node;
import org.alfresco.entities.values.Nodes;
import org.alfresco.services.nlp.Entities;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.Accumulable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.BSONObject;

import com.mongodb.hadoop.MongoInputFormat;

public class Spark1 implements Serializable
{
	private static final long serialVersionUID = -1486654511409655088L;

	private JavaSparkContext sc;

	public void init()
	{
		Class<?>[] classes = new Class<?>[] { Nodes.class, Node.class, Entities.class, NodesAccumulableFunction.class,
				NodesAccumulableParam.class};
		SparkConf conf = new SparkConf()
			.setAppName("Entities")
			.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
			.registerKryoClasses(classes)
			.setMaster("spark://localhost:8299");
		this.sc = new JavaSparkContext(conf);
	}

	public void shutdown()
	{
		if(sc != null)
		{
			sc.close();
		}
	}

//	public void match(final long nodeInternalId, final long nodeVersion)
//	{
//		Configuration config = new Configuration();
//		config.set("mongo.input.uri", "mongodb://127.0.0.1:27017/entities");
//		config.set("mongo.output.uri", "mongodb://127.0.0.1:27017/entities.output");
//		JavaPairRDD<Object, BSONObject> rdd = sc.newAPIHadoopRDD(config, MongoInputFormat.class, Object.class, BSONObject.class);
//
//		JavaPairRDD<Object, BSONObject> nodeEntities = rdd.filter(new Function<Tuple2<Object,BSONObject>, Boolean>()
//		{
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Boolean call(Tuple2<Object, BSONObject> v1) throws Exception
//			{
//				return v1._2.get("n").equals(nodeInternalId) && v1._2.get("v").equals(nodeVersion);
//			}
//		});
//	}
	
	public Nodes matchNodes(final Entities entities)
	{
		Configuration config = new Configuration();
		config.set("mongo.input.uri", "mongodb://127.0.0.1:27017/entitiesTest.entities1425034463903");
		config.set("mongo.output.uri", "mongodb://127.0.0.1:27017/entitiesTest.entities1425034463903.matches");

		JavaPairRDD<Object, BSONObject> rdd = sc.newAPIHadoopRDD(config, MongoInputFormat.class, Object.class, BSONObject.class);

//		JavaPairRDD<Object, BSONObject> nodeEntities = rdd.filter(new Function<Tuple2<Object,BSONObject>, Boolean>()
//		{
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Boolean call(Tuple2<Object, BSONObject> v1) throws Exception
//			{
//				boolean ret = false;
//
//				String name = (String)v1._2.get("nm");
//				if(entities.hasName(name))
//				{
//					ret = true;
//				}
//				else
//				{
//					
//				}
//
//				return ret;
//			}
//		});

		JavaRDD<Node> matchingNodes = rdd.flatMap(new MatchNodesFunction(entities));

//		sc.accumulator(new Nodes(), new NodesAccumulatorParam());

		final Accumulable<Nodes, Node> nodesAccumulator = sc.accumulable(new Nodes(), new NodesAccumulableParam());
		matchingNodes.foreach(new NodesAccumulableFunction(nodesAccumulator));

//		final Accumulator<Nodes> nodesAccumulator = sc.accumulator(new Nodes(), new NodesAccumulatorParam());
//		matchingNodes.foreach(new NodesAccumulatorFunction(nodesAccumulator));

	    return nodesAccumulator.value();
//		return null;
	}
}
