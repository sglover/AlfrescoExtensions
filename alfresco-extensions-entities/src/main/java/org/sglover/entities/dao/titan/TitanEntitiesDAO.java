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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.alfresco.extensions.titan.TitanDBSession;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;
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
import com.thinkaurelius.titan.core.schema.RelationTypeIndex;
import com.thinkaurelius.titan.core.schema.SchemaStatus;
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
    protected TitanDBSession titanSession;

    protected TitanGraph graph;

    public TitanEntitiesDAO()
    {
    }

    public TitanEntitiesDAO(TitanDBSession titanSession) throws ConfigurationException, MalformedURLException, URISyntaxException, BackendException
    {
        this.titanSession = titanSession;
        init();
    }

    @PostConstruct
    public void init()
    {
        this.graph = titanSession.getGraph();

        graph.tx().rollback();

        TitanManagement mgmt = graph.openManagement();

        try
        {
            // Create Schema
            VertexLabel nameEntityVertexLabel = mgmt.getVertexLabel("NameEntity");
            if(nameEntityVertexLabel == null)
            {
                nameEntityVertexLabel = mgmt.makeVertexLabel("NameEntity").make();
            }

            VertexLabel locationEntityVertexLabel = mgmt.getVertexLabel("LocationEntity");
            if(locationEntityVertexLabel == null)
            {
                locationEntityVertexLabel = mgmt.makeVertexLabel("LocationEntity").make();
            }

            VertexLabel miscEntityVertexLabel = mgmt.getVertexLabel("MiscEntity");
            if(miscEntityVertexLabel == null)
            {
                miscEntityVertexLabel = mgmt.makeVertexLabel("MiscEntity").make();
            }

            VertexLabel orgEntityVertexLabel = mgmt.getVertexLabel("OrgEntity");
            if(orgEntityVertexLabel == null)
            {
                orgEntityVertexLabel = mgmt.makeVertexLabel("OrgEntity").make();
            }

            VertexLabel dateEntityVertexLabel = mgmt.getVertexLabel("DateEntity");
            if(dateEntityVertexLabel == null)
            {
                dateEntityVertexLabel = mgmt.makeVertexLabel("DateEntity").make();
            }

            VertexLabel timeEntityVertexLabel = mgmt.getVertexLabel("TimeEntity");
            if(timeEntityVertexLabel == null)
            {
                timeEntityVertexLabel = mgmt.makeVertexLabel("TimeEntity").make();
            }

            VertexLabel numberEntityVertexLabel = mgmt.getVertexLabel("NumberEntity");
            if(numberEntityVertexLabel == null)
            {
                numberEntityVertexLabel = mgmt.makeVertexLabel("NumberEntity").make();
            }

            VertexLabel durationEntityVertexLabel = mgmt.getVertexLabel("DurationEntity");
            if(durationEntityVertexLabel == null)
            {
                durationEntityVertexLabel = mgmt.makeVertexLabel("DurationEntity").make();
            }

            PropertyKey entityTs = mgmt.getPropertyKey("entityTs");
            if(entityTs == null)
            {
                entityTs = mgmt.makePropertyKey("entityTs").dataType(Long.class).make();
            }

            PropertyKey strValue = mgmt.getPropertyKey("strValue");
            if(strValue == null)
            {
                strValue = mgmt.makePropertyKey("strValue").dataType(String.class).make();
            }

            EdgeLabel nodeNameEntityLabel = mgmt.getEdgeLabel("NodeNameEntity");
            if(nodeNameEntityLabel == null)
            {
                nodeNameEntityLabel = mgmt.makeEdgeLabel("NodeNameEntity").make();
            }

            EdgeLabel nodeLocationEntityLabel = mgmt.getEdgeLabel("NodeLocationEntity");
            if(nodeLocationEntityLabel == null)
            {
                nodeLocationEntityLabel = mgmt.makeEdgeLabel("NodeLocationEntity").make();
            }

            EdgeLabel nodeMiscEntityLabel = mgmt.getEdgeLabel("NodeMiscEntity");
            if(nodeMiscEntityLabel == null)
            {
                nodeMiscEntityLabel = mgmt.makeEdgeLabel("NodeMiscEntity").make();
            }

            EdgeLabel nodeOrgEntityLabel = mgmt.getEdgeLabel("NodeOrgEntity");
            if(nodeOrgEntityLabel == null)
            {
                nodeOrgEntityLabel = mgmt.makeEdgeLabel("NodeOrgEntity").make();
            }

            EdgeLabel nodeDateEntityLabel = mgmt.getEdgeLabel("NodeDateEntity");
            if(nodeDateEntityLabel == null)
            {
                nodeDateEntityLabel = mgmt.makeEdgeLabel("NodeDateEntity").make();
            }

            EdgeLabel nodeTimeEntityLabel = mgmt.getEdgeLabel("NodeTimeEntity");
            if(nodeTimeEntityLabel == null)
            {
                nodeTimeEntityLabel = mgmt.makeEdgeLabel("NodeTimeEntity").make();
            }

            EdgeLabel nodeNumberEntityLabel = mgmt.getEdgeLabel("NodeNumberEntity");
            if(nodeNumberEntityLabel == null)
            {
                nodeNumberEntityLabel = mgmt.makeEdgeLabel("NodeNumberEntity").make();
            }

            EdgeLabel nodeDurationEntityLabel = mgmt.getEdgeLabel("NodeDurationEntity");
            if(nodeDurationEntityLabel == null)
            {
                nodeDurationEntityLabel = mgmt.makeEdgeLabel("NodeDurationEntity").make();
            }

            TitanGraphIndex byNameEntity = mgmt.getGraphIndex("byNameEntity");
            if(byNameEntity == null)
            {
                byNameEntity = mgmt.buildIndex("byNameEntity", Vertex.class)
                    .indexOnly(nameEntityVertexLabel)
                    .addKey(strValue)
                    .unique()
                    .buildCompositeIndex();
                mgmt.setConsistency(byNameEntity, ConsistencyModifier.DEFAULT);
            }

            TitanGraphIndex byLocationEntity = mgmt.getGraphIndex("byLocationEntity");
            if(byLocationEntity == null)
            {
                byLocationEntity = mgmt.buildIndex("byLocationEntity", Vertex.class)
                    .indexOnly(locationEntityVertexLabel)
                    .addKey(strValue)
                    .unique()
                    .buildCompositeIndex();
                mgmt.setConsistency(byLocationEntity, ConsistencyModifier.DEFAULT);
            }

            TitanGraphIndex byMiscEntity = mgmt.getGraphIndex("byMiscEntity");
            if(byMiscEntity == null)
            {
                byMiscEntity = mgmt.buildIndex("byMiscEntity", Vertex.class)
                    .indexOnly(miscEntityVertexLabel)
                    .addKey(strValue)
                    .unique()
                    .buildCompositeIndex();
                mgmt.setConsistency(byMiscEntity, ConsistencyModifier.DEFAULT);
            }

            TitanGraphIndex byOrgEntity = mgmt.getGraphIndex("byOrgEntity");
            if(byOrgEntity == null)
            {
                byOrgEntity = mgmt.buildIndex("byOrgEntity", Vertex.class)
                    .indexOnly(orgEntityVertexLabel)
                    .addKey(strValue)
                    .unique()
                    .buildCompositeIndex();
                mgmt.setConsistency(byOrgEntity, ConsistencyModifier.DEFAULT);
            }

            getOrCreateEdgeIndex(mgmt, "NodeNameEntity", "nodeNameEntity", Arrays.asList("entityTs"));
            getOrCreateEdgeIndex(mgmt, "NodeLocationEntity", "nodeLocationEntity", Arrays.asList("entityTs"));
            getOrCreateEdgeIndex(mgmt, "NodeOrgEntity", "nodeOrgEntity", Arrays.asList("entityTs"));
            getOrCreateEdgeIndex(mgmt, "NodeMiscEntity", "nodeMiscEntity", Arrays.asList("entityTs"));
            getOrCreateEdgeIndex(mgmt, "NodeDurationEntity", "nodeDurationEntity", Arrays.asList("entityTs"));
            getOrCreateEdgeIndex(mgmt, "NodeDateEntity", "nodeDateEntity", Arrays.asList("entityTs"));
            getOrCreateEdgeIndex(mgmt, "NodeTimeEntity", "nodeTimeEntity", Arrays.asList("entityTs"));
            getOrCreateEdgeIndex(mgmt, "NodeNumberEntity", "nodeNumberEntity", Arrays.asList("entityTs"));
        }
        finally
        {
            graph.tx().commit();
            if(mgmt.isOpen())
            {
                mgmt.commit();
            }
        }
    }

    private void getOrCreateEdgeIndex(TitanManagement mgmt, String edgeLabel, String indexName, List<String> keyNames)
    {
        String name = edgeLabel + "." + indexName;

        EdgeLabel edgeEntityLabel = mgmt.getEdgeLabel(edgeLabel);
        RelationTypeIndex entityIndex = mgmt.getRelationIndex(edgeEntityLabel, indexName);
        logger.info(name + " index " + entityIndex);

        if(entityIndex == null)
        {
            logger.info("Creating " + name + " index");

            List<PropertyKey> keys = new ArrayList<>(keyNames.size());
            for(String keyName : keyNames)
            {
                PropertyKey key = mgmt.getPropertyKey(keyName);
                keys.add(key);
            }

            entityIndex = mgmt.buildEdgeIndex(edgeEntityLabel, indexName, Direction.OUT, 
                    keys.toArray(new PropertyKey[0]));

            logger.info("Created " + name + " index " + entityIndex);
        }

        SchemaStatus status = entityIndex.getIndexStatus();
        logger.info(name + " status = " + status);
    }

    @Override
    public Entities getEntities(Node node)
    {
        return graph.traversal().V()
                .hasLabel("NODE")
                .has("nid", node.getNodeId())
                .has("nv", node.getNodeVersion())
                .out("NodeNameEntity", "NodeLocationEntity", "NodeOrgEntity", "NodeMiscEntity")
                .hasLabel("NameEntity", "LocationEntity", "OrgEntity", "MiscEntity")
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
    public Stream<Entity<String>> getNames(Node node, int skip, int maxItems)
    {
        return graph.traversal().V()
            .hasLabel("NODE")
            .has("nid", node.getNodeId())
            .has("nv", node.getNodeVersion())
            .outE("NodeNameEntity")
            .order()
                .by("entityTs", Order.incr)
            .range(skip, skip + maxItems)
            .inV()
            .map(ne -> {
                String value = (String)ne.get().property("strValue").value();
                return new Entity<String>(EntityType.names, value);
            })
            .toStream();
    }

    @Override
    public Stream<Entity<String>> getOrgs(Node node, int skip, int maxItems)
    {
        return graph.traversal().V()
                .hasLabel("NODE")
                .has("nid", node.getNodeId())
                .has("nv", node.getNodeVersion())
                .outE("NodeOrgEntity")
                .order()
                    .by("entityTs", Order.incr)
                .range(skip, skip + maxItems)
                .inV()
                .map(ne -> {
                    String value = (String)ne.get().property("entityValue").value();
                    return new Entity<String>(EntityType.orgs, value);
                })
                .toStream();
    }

    private Vertex getOrAddNameEntity(String name)
    {
        return graph.traversal().V()
            .hasLabel("NameEntity")
            .has("strValue", name)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding name entity " + name);

                Vertex ev = graph.addVertex("NameEntity");
                ev.property("strValue", name);
                return ev;
            });
    }

    private Vertex getOrAddLocationEntity(String location)
    {
        return graph.traversal().V()
            .hasLabel("LocationEntity")
            .has("strValue", location)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding location entity " + location);

                Vertex ev = graph.addVertex("LocationEntity");
                ev.property("strValue", location);
                return ev;
            });
    }

    private Vertex getOrAddMiscEntity(String misc)
    {
        return graph.traversal().V()
            .hasLabel("MiscEntity")
            .has("strValue", misc)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding misc entity " + misc);

                Vertex ev = graph.addVertex("MiscEntity");
                ev.property("strValue", misc);
                return ev;
            });
    }

    private Vertex getOrAddOrgEntity(String org)
    {
        return graph.traversal().V()
            .hasLabel("OrgEntity")
            .has("strValue", org)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding org entity " + org);

                Vertex ev = graph.addVertex("OrgEntity");
                ev.property("strValue", org);
                return ev;
            });
    }

    private Vertex getOrAddDateEntity(String date)
    {
        return graph.traversal().V()
            .hasLabel("DateEntity")
            .has("strValue", date)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding date entity " + date);

                Vertex ev = graph.addVertex("DateEntity");
                ev.property("dateValue", date);
                return ev;
            });
    }

    private Vertex getOrAddTimeEntity(String time)
    {
        return graph.traversal().V()
            .hasLabel("TimeEntity")
            .has("strValue", time)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding time entity " + time);

                Vertex ev = graph.addVertex("TimeEntity");
                ev.property("strValue", time);
                return ev;
            });
    }

    private Vertex getOrAddNumberEntity(String number)
    {
        return graph.traversal().V()
            .hasLabel("NumberEntity")
            .has("strValue", number)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding number entity " + number);

                Vertex ev = graph.addVertex("NumberEntity");
                ev.property("strValue", number);
                return ev;
            });
    }

    private Vertex getOrAddDurationEntity(String duration)
    {
        return graph.traversal().V()
            .hasLabel("DurationEntity")
            .has("strValue", duration)
            .tryNext()
            .orElseGet(() -> {
                logger.info("Adding duration entity " + duration);

                Vertex ev = graph.addVertex("DurationEntity");
                ev.property("strValue", duration);
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

                    nv.addEdge("NodeNameEntity",
                            getOrAddNameEntity(name.getEntity()),
                            "entityTs", System.currentTimeMillis());
                });

                entities.getLocations().stream().forEach(name -> {
                    logger.info("Entity " + name + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NodeLocationEntity", getOrAddLocationEntity(name.getEntity()),
                            "entityTs", System.currentTimeMillis());
                });

                entities.getMisc().stream().forEach(name -> {
                    logger.info("Entity " + name + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NodeMiscEntity", getOrAddMiscEntity(name.getEntity()),
                            "entityTs", System.currentTimeMillis());
                });

                entities.getOrgs().stream().forEach(name -> {
                    logger.info("Entity " + name + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NodeOrgEntity", getOrAddOrgEntity(name.getEntity()),
                            "entityTs", System.currentTimeMillis());
                });

                entities.getDates().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NodeDateEntity", getOrAddDateEntity(date.getEntity()),
                            "entityTs", System.currentTimeMillis());
                });

                entities.getTimes().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NodeTimeEntity", getOrAddTimeEntity(date.getEntity()),
                            "entityTs", System.currentTimeMillis());
                });

                entities.getNumbers().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NodeNumberEntity", getOrAddNumberEntity(date.getEntity()),
                            "entityTs", System.currentTimeMillis());
                });

                entities.getDurations().stream().forEach(date -> {
                    logger.info("Entity " + date + " for " + node.getNodeId() + "." + node.getNodeVersion());

                    nv.addEdge("NodeDurationEntity", getOrAddDurationEntity(date.getEntity()),
                            "entityTs", System.currentTimeMillis());
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
