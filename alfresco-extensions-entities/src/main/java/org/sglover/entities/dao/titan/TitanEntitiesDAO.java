/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.sglover.entities.dao.titan;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.alfresco.extensions.titan.TitanSession;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.EntitiesDAO;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;
import org.sglover.nlp.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.diskstorage.BackendException;

/**
 * 
 * @author sglover
 *
 */
@Component(value="titanEntitiesDAO")
public class TitanEntitiesDAO implements EntitiesDAO
{
    private static Log logger = LogFactory.getLog(TitanEntitiesDAO.class);

    @Autowired
    protected TitanSession titanSession;

    protected TitanGraph graph;

    public TitanEntitiesDAO()
    {
    }

    public TitanEntitiesDAO(TitanSession titanSession) throws ConfigurationException, MalformedURLException, URISyntaxException, BackendException
    {
        this.titanSession = titanSession;
        init();
    }

    @PostConstruct
    public void init()
    {
        this.graph = titanSession.getGraph();

        // Create Schema
        TitanManagement mgmt = graph.openManagement();

        VertexLabel entityVertexLabel = mgmt.getVertexLabel("ENTITY");
        if(entityVertexLabel == null)
        {
            entityVertexLabel = mgmt.makeVertexLabel("ENTITY").make();
        }



        PropertyKey entityTs = mgmt.getPropertyKey("entityTs");
        if(entityTs == null)
        {
            entityTs = mgmt.makePropertyKey("entityTs").dataType(Long.class).make();
        }

        EdgeLabel nodeEntityEdgeLabel = mgmt.getEdgeLabel("NODEENTITY");
        if(nodeEntityEdgeLabel == null)
        {
            nodeEntityEdgeLabel = mgmt.makeEdgeLabel("NODEENTITY").make();

            TitanGraphIndex byEntityTs = mgmt.getGraphIndex("byEntityTs");
            if(byEntityTs == null)
            {
                byEntityTs = mgmt.buildIndex("byEntityTs", Edge.class)
                        .addKey(entityTs)
                        .indexOnly(nodeEntityEdgeLabel)
                        .buildCompositeIndex();
            }
        }




        PropertyKey entityType = mgmt.getPropertyKey("entityType");
        if(entityType == null)
        {
            // TODO add signature
            entityType = mgmt.makePropertyKey("entityType").dataType(String.class).make();
        }

        PropertyKey entityValue = mgmt.getPropertyKey("entityValue");
        if(entityValue == null)
        {
            entityValue = mgmt.makePropertyKey("entityValue").dataType(String.class).make();
        }

        TitanGraphIndex byEntity = mgmt.getGraphIndex("byEntity");
        if(byEntity == null)
        {
            byEntity = mgmt.buildIndex("byEntity", Vertex.class)
                .indexOnly(entityVertexLabel)
                .addKey(entityType)
                .addKey(entityValue)
                .unique()
                .buildCompositeIndex();
            mgmt.setConsistency(byEntity, ConsistencyModifier.DEFAULT);
        }

        mgmt.commit();
    }

    @Override
    public Entities getEntities(Node node)
    {
        return graph.traversal().V()
                .hasLabel("NODE")
                .has("nid", node.getNodeId())
                .has("nv", node.getNodeVersion())
                .out("NODEENTITY")
                .hasLabel("ENTITY")
                .map(ne -> {
                    String entityTypeStr = (String)ne.get().property("entityType").value();
                    EntityType entityType = EntityType.valueOf(entityTypeStr);
                    String value = (String)ne.get().property("entityValue").value();
                    return new Entity<String>(entityType, value);
                })
                .fold(Entities.empty(), (es, e) ->
                {
                    switch(e.getType())
                    {
                    case names:
                        es.addName(e.getEntity());
                        break;
                    case orgs:
                        es.addOrg(e.getEntity());
                        break;
                    case locations:
                        es.addLocation(e.getEntity());
                        break;
                    case money:
                        es.addMoney(e.getEntity());
                        break;
                    case misc:
                        es.addMisc(e.getEntity());
                        break;
                    case dates:
                        es.addDate(e.getEntity());
                        break;
                    }
                   return es;
                })
                .next();
    }

    @Override
    public Stream<Node> matchingNodes(EntityType entityType, String entityValue)
    {
        return graph.traversal().V()
                .hasLabel("ENTITY")
                .has("entityType", entityType.toString())
                .has("entityValue", entityValue)
                .in("NODEENTITY")
                .map(n -> {
                    String nodeId = (String)n.get().property("nid").value();
                    Long nodeVersion = (Long)n.get().property("nv").value();
                    return Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
                })
                .toStream();
    }

    @Override
    public Stream<Entity<String>> getNames(Node node)
    {
        return graph.traversal().V()
            .hasLabel("NODE")
            .has("nid", node.getNodeId())
            .has("nv", node.getNodeVersion())
            .outE("NODEENTITY")
            .has("type", "nameEntity")
            .inV()
            .order()
                .by("entityValue", Order.incr)
            .map(ne -> {
                String value = (String)ne.get().property("entityValue").value();
                return new Entity<String>(EntityType.names, value);
            })
            .toStream();
    }

    @Override
    public Stream<Entity<String>> getOrgs(Node node)
    {
        return graph.traversal().V()
                .hasLabel("NODE")
                .has("nid", node.getNodeId())
                .has("nv", node.getNodeVersion())
                .out("orgEntity")
                .map(ne -> {
                    String value = (String)ne.get().property("entityValue").value();
                    return new Entity<String>(EntityType.orgs, value);
                })
                .toStream();
    }

    private Vertex getOrAddEntity(EntityType entityType, Object entityValue)
    {
        return graph.traversal().V()
            .hasLabel("ENTITY")
            .has("entityType", entityType.toString())
            .has("entityValue", entityValue)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding entity " + entityType.toString() + "(" + entityValue.toString() + ")");

                Vertex ev = graph.addVertex("ENTITY");
                ev.property("entityType", entityType.toString());
                ev.property("entityValue", entityValue.toString());
                return ev;
            });
    }

    private void addEntitiesImpl(Graph g, Node node, Entities entities)
    {
        g.traversal().V()
            .hasLabel("NODE")
            .has("nid", node.getNodeId())
            .has("nv", node.getNodeVersion())
            .tryNext()
            .ifPresent(nv -> {
                entities.getNames().stream().forEach(name -> {
                    logger.info("Entity " + name + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(name.getType(), name.getEntity()),
                            "ts", System.currentTimeMillis());
                });

                entities.getLocations().stream().forEach(name -> {
                    logger.info("Entity " + name + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(name.getType(), name.getEntity()),
                            "ts", System.currentTimeMillis());
                });

                entities.getMisc().stream().forEach(name -> {
                    logger.info("Entity " + name + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(name.getType(), name.getEntity()),
                            "ts", System.currentTimeMillis());
                });

                entities.getOrgs().stream().forEach(name -> {
                    logger.info("Entity " + name + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(name.getType(), name.getEntity()),
                            "ts", System.currentTimeMillis());
                });

                entities.getDates().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(date.getType(), date.getEntity()),
                            "ts", System.currentTimeMillis());
                });

                entities.getTimes().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(date.getType(), date.getEntity()),
                            "ts", System.currentTimeMillis());
                });

                entities.getNumbers().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(date.getType(), date.getEntity()),
                            "ts", System.currentTimeMillis());
                });

                entities.getDurations().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NODEENTITY",
                            getOrAddEntity(date.getType(), date.getEntity()),
                            "ts", System.currentTimeMillis());
                });
            });
    }

    @Override
    public void addEntities(Node node, Entities entities)
    {
        graph.tx().submit(new Function<Graph, Void>() {
            public Void apply(Graph g)
            {
                addEntitiesImpl(g, node, entities);
                return null;
            }
        })
        .exponentialBackoff(5);
    }

}
