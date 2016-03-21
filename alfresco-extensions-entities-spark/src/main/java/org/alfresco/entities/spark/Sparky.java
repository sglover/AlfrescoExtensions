/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.spark;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.alfresco.entities.values.Nodes;
import org.alfresco.extensions.common.Node;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.alfresco.services.nlp.EntityTagger;
import org.alfresco.services.nlp.StanfordEntityTagger;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.Accumulable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.bson.BSONObject;

import scala.collection.IndexedSeq;

import com.datastax.driver.core.Session;
import com.datastax.spark.connector.ColumnRef;
import com.datastax.spark.connector.cql.CassandraConnector;
import com.datastax.spark.connector.cql.TableDef;
import com.datastax.spark.connector.japi.CassandraRow;
import com.datastax.spark.connector.japi.RDDAndDStreamCommonJavaFunctions;
import com.datastax.spark.connector.writer.CassandraRowWriter;
import com.datastax.spark.connector.writer.RowWriter;
import com.datastax.spark.connector.writer.RowWriterFactory;
import com.mongodb.hadoop.MongoInputFormat;

public class Sparky implements Serializable
{
    private static final long serialVersionUID = -1486654511409655088L;

    private JavaSparkContext sc;

    public void init()
    {
//        Class<?>[] classes = new Class<?>[]
//        {
//                Nodes.class, Node.class, Entities.class,
//                NodesAccumulableFunction.class, NodesAccumulableParam.class
//        };

        String logFile = "sparky.log";
        SparkConf conf = new SparkConf().setAppName("Alfresco Sparky");
//        this.sc = new JavaSparkContext(conf);

//        SparkConf conf = new SparkConf()
//                .setAppName("Entities")
//                .set("spark.cassandra.connection.host", "127.0.0.1")
////                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
////                .registerKryoClasses(classes)
//                .setMaster("spark://localhost:8299");
        this.sc = new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(logFile).cache();
    }

    public void setup()
    {
        CassandraConnector connector = CassandraConnector.apply(sc.getConf());

        try(Session session = connector.openSession()) 
        {
            session.execute("DROP KEYSPACE IF EXISTS alfresco");
            session.execute("CREATE KEYSPACE alfresco WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
            session.execute("CREATE TABLE java_api.products (id INT PRIMARY KEY, name TEXT, parents LIST<INT>)");
            session.execute("CREATE TABLE java_api.sales (id UUID PRIMARY KEY, product INT, price DECIMAL)");
            session.execute("CREATE TABLE java_api.summaries (product INT PRIMARY KEY, summary DECIMAL)");
        }
    }

    public void shutdown()
    {
        if (sc != null)
        {
            sc.close();
        }
    }

    public void names(String path)
    {
        final EntityTagger entityTagger = StanfordEntityTagger.build();

        JavaRDD<String> text = sc.textFile(path);
        JavaRDD<Entity<String>> names = text.flatMap(new FlatMapFunction<String, Entity<String>>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Iterable<Entity<String>> call(String text) throws Exception
            {
                Entities entities = entityTagger.getEntities(text);
                return entities.getNames();
            }
        });
//        Entity<String> x = new Entity<String>(){};
//        x.getClass().getGenericSuperclass();
        javaFunctions(names).writerBuilder("alfresco", "names", mapToRow(Entity.class));
        JavaRDD<CassandraRow> c = names.map(new Function<Entity<String>, CassandraRow>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public CassandraRow call(Entity<String> entity) throws Exception
            {
                String name = entity.getEntity();
                long count = entity.getCount();
                List<String> columns = Arrays.asList("name", "count");
                IndexedSeq<Object> values = Arrays.asList(name, count);
                com.datastax.spark.connector.japi.CassandraRow row = new CassandraRow(columns, values);
                return row;
            }
        });

        RDDAndDStreamCommonJavaFunctions<CassandraRow>.WriterBuilder wb = javaFunctions(c)
                .writerBuilder("alfresco", "names", new RowWriterFactory<CassandraRow>()
                 {
                    @Override
                    public RowWriter<CassandraRow> rowWriter(TableDef table,
                            IndexedSeq<ColumnRef> columns)
                    {
                        CassandraRowWriter rw = new CassandraRowWriter(table, columns);
                        return rw;
                    }
                 });
        javaFunctions(c).saveToCassandra("alfresco", "names", wb.rowWriterFactory, wb.columnSelector);
    }

    public void a(String path)
	{
        final EntityTagger entityTagger = StanfordEntityTagger.build();

        JavaRDD<String> text = sc.textFile(path);
        JavaRDD<Entity<String>> names = text.flatMap(new FlatMapFunction<String, Entity<String>>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Iterable<Entity<String>> call(String text) throws Exception
            {
                Entities entities = entityTagger.getEntities(text);
                return entities.getNames();
            }
        });

//        final Accumulable<String, String> linesAccumulator = sc.accumulable(
//                "", new LinesAccumulableParam());
//        text.foreach(new LinesAccumulableFunction(linesAccumulator));
//        WriterBuilder builder = javaFunctions(text).writerBuilder("alfresco", "entities", new RowWriterFactory<CassandraRow>()
//        {
//            @Override
//            public RowWriter<CassandraRow> rowWriter(TableDef tableDef, IndexedSeq<ColumnRef> columns)
//            {
//                CassandraRowWriter rw = new CassandraRowWriter(tableDef, columns);
//                return rw;
//            }
//        });
//
//        JavaRDD<CassandraRow> cassandraRowsRDD = javaFunctions(sc).cassandraTable("alfresco", "stuff");
//        JavaRDD<Node> matchingNodes = cassandraRowsRDD.flatMap(new TestFunction(
//                entities));
//        JavaRDD<Entity<String>> productsRDD = sc.parallelize(products);
//        javaFunctions(productsRDD, Entity.class).saveToCassandra("java_api", "products");
	}

    public Nodes matchNodes(final Entities entities)
    {
        Configuration config = new Configuration();
        config.set("mongo.input.uri",
                "mongodb://127.0.0.1:27017/entitiesTest.entities1425034463903");
        config.set("mongo.output.uri",
                "mongodb://127.0.0.1:27017/entitiesTest.entities1425034463903.matches");

        JavaPairRDD<Object, BSONObject> rdd = sc.newAPIHadoopRDD(config,
                MongoInputFormat.class, Object.class, BSONObject.class);

        // JavaPairRDD<Object, BSONObject> nodeEntities = rdd.filter(new
        // Function<Tuple2<Object,BSONObject>, Boolean>()
        // {
        // private static final long serialVersionUID = 1L;
        //
        // @Override
        // public Boolean call(Tuple2<Object, BSONObject> v1) throws Exception
        // {
        // boolean ret = false;
        //
        // String name = (String)v1._2.get("nm");
        // if(entities.hasName(name))
        // {
        // ret = true;
        // }
        // else
        // {
        //
        // }
        //
        // return ret;
        // }
        // });

        JavaRDD<Node> matchingNodes = rdd.flatMap(new MatchNodesFunction(
                entities));

        // sc.accumulator(new Nodes(), new NodesAccumulatorParam());

        final Accumulable<Nodes, Node> nodesAccumulator = sc.accumulable(
                new Nodes(), new NodesAccumulableParam());
        matchingNodes.foreach(new NodesAccumulableFunction(nodesAccumulator));

        // final Accumulator<Nodes> nodesAccumulator = sc.accumulator(new
        // Nodes(), new NodesAccumulatorParam());
        // matchingNodes.foreach(new
        // NodesAccumulatorFunction(nodesAccumulator));

        return nodesAccumulator.value();
        // return null;
    }
}
