/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao.titan;

import static org.apache.tinkerpop.gremlin.process.traversal.P.gte;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.events.types.ActivityEvent;
import org.alfresco.permissions.Auths;
import org.alfresco.permissions.Model;
import org.alfresco.permissions.Properties;
import org.alfresco.permissions.Properties.Property;
import org.alfresco.permissions.dao.Event;
import org.alfresco.permissions.dao.Node;
import org.alfresco.permissions.dao.PermissionsDAO;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.Multiplicity;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.configuration.ReadConfiguration;
import com.thinkaurelius.titan.diskstorage.configuration.backend.CommonsConfiguration;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;

/**
 * 
 * @author sglover
 * 
 *  Pre-requisites: ccm start
 *  ElasticSearch: cd /Users/sglover/dev/elasticsearch-1.4.4; bin/elasticsearch -d
 */
@Component
public class TitanPermissionsDAO implements PermissionsDAO
{
    private static Log logger = LogFactory.getLog(TitanPermissionsDAO.class);

    public static final String INDEX_NAME = "repo";

    private Configuration conf;
    private TitanGraph graph;

    private Auths defaultNodeAuths;

    public TitanPermissionsDAO(boolean clear, String configurationFile) throws ConfigurationException, MalformedURLException, URISyntaxException, BackendException
    {
        buildTitanSession(clear, configurationFile);

        defaultNodeAuths = Auths.start("Read", "Root");

        addChildPermission("Read", "ReadChildren");
        addChildPermission("Read", "ReadProperties");
        addChildPermission("Read", "ReadContent");
        addChildPermission("Read", "ReadEvents");
    }

    private static class GroupVertexComparator implements Comparator<Vertex>
    {
        @Override
        public int compare(Vertex o1, Vertex o2)
        {
            String o1Name = (String)o1.property("authName").value();
            String o2Name = (String)o2.property("authName").value();
            return o1Name.compareTo(o2Name);
        }
    }
    private GroupVertexComparator groupVertexComparator = new GroupVertexComparator();

    private static class EventVertexComparator implements Comparator<Vertex>
    {
        @Override
        public int compare(Vertex o1, Vertex o2)
        {
            String eventType1 = (String)o1.property("eventType").orElse("");
            String eventType2 = (String)o2.property("eventType").orElse("");
            int i = eventType1.compareTo(eventType2);
            if(i == 0)
            {
                String username1 = (String)o1.property("username").orElse("");
                String username2 = (String)o2.property("username").orElse("");
                i = username1.compareTo(username2);
            }
            return i;
        }
    }
    private EventVertexComparator eventVertexComparator = new EventVertexComparator();

    private static class NodeComparator implements Comparator<Vertex>
    {
        @Override
        public int compare(Vertex o1, Vertex o2)
        {
            String nodeId1 = (String)o1.property("nid").value();
            String nodeId2 = (String)o2.property("nid").value();
            return nodeId1.compareTo(nodeId2);
        }
    }
    private NodeComparator nodeComparator = new NodeComparator();

    public void clear() throws BackendException, ConfigurationException, MalformedURLException, URISyntaxException
    {
        ReadConfiguration readConfig = new CommonsConfiguration(conf);
        GraphDatabaseConfiguration graphConfig = new GraphDatabaseConfiguration(readConfig);
        graphConfig.getBackend().clearStorage();
    }

    public void close()
    {
        graph.close();
    }

    private void buildTitanSession(boolean clear, String configurationFile) throws ConfigurationException, MalformedURLException, URISyntaxException, BackendException
    {
        if(configurationFile == null)
        {
            configurationFile = "conf/repo.properties";
        }
        URL url = getClass().getClassLoader().
                getResource(configurationFile).toURI().toURL();
        this.conf = new PropertiesConfiguration(url);

        if(clear)
        {
            clear();
        }

        this.graph = TitanFactory.open(conf);

        // Create Schema
        TitanManagement mgmt = graph.openManagement();

        VertexLabel authorityVertexLabel = mgmt.getVertexLabel("AUTHORITY");
        if(authorityVertexLabel == null)
        {
            authorityVertexLabel = mgmt.makeVertexLabel("AUTHORITY").make();
        }

        PropertyKey authName = mgmt.getPropertyKey("authName");
        if(authName == null)
        {
            authName = mgmt.makePropertyKey("authName").dataType(String.class).make();

            TitanGraphIndex byAuthorityName = mgmt.getGraphIndex("byAuthorityName");
            if(byAuthorityName == null)
            {
                byAuthorityName = mgmt.buildIndex("byAuthorityName", Vertex.class)
                    .indexOnly(authorityVertexLabel).addKey(authName)
                    .unique()
                    .buildCompositeIndex();
//                mgmt.setConsistency(byAuthorityName, ConsistencyModifier.LOCK);
                mgmt.setConsistency(byAuthorityName, ConsistencyModifier.DEFAULT);
            }
        }

        VertexLabel permissionVertexLabel = mgmt.getVertexLabel("PERMISSION");
        if(permissionVertexLabel == null)
        {
            permissionVertexLabel = mgmt.makeVertexLabel("PERMISSION").make();
        }

        PropertyKey permName = mgmt.getPropertyKey("permName");
        if(permName == null)
        {
            permName = mgmt.makePropertyKey("permName").dataType(String.class).make();

            TitanGraphIndex byPermName = mgmt.getGraphIndex("byPermName");
            if(byPermName == null)
            {
                byPermName = mgmt.buildIndex("byPermName", Vertex.class)
                    .indexOnly(permissionVertexLabel).addKey(permName)
                    .unique()
                    .buildCompositeIndex();
//                mgmt.setConsistency(byPermName, ConsistencyModifier.LOCK);
                mgmt.setConsistency(byPermName, ConsistencyModifier.DEFAULT);
            }
        }

        VertexLabel nodeVertexLabel = mgmt.getVertexLabel("NODE");
        if(nodeVertexLabel == null)
        {
            nodeVertexLabel = mgmt.makeVertexLabel("NODE").make();
        }

        PropertyKey nid = mgmt.getPropertyKey("nid");
        if(nid == null)
        {
            nid = mgmt.makePropertyKey("nid").dataType(String.class).make();

            TitanGraphIndex byNid = mgmt.getGraphIndex("byNid");
            if(byNid == null)
            {
                byNid = mgmt.buildIndex("byNid", Vertex.class)
                    .indexOnly(nodeVertexLabel).addKey(nid)
                    .buildCompositeIndex();
//                mgmt.setConsistency(byNodeId, ConsistencyModifier.LOCK);
                mgmt.setConsistency(byNid, ConsistencyModifier.DEFAULT);
            }
        }

        PropertyKey nv = mgmt.getPropertyKey("nv");
        if(nv == null)
        {
            nv = mgmt.makePropertyKey("nv").dataType(Integer.class).make();

            TitanGraphIndex byNV = mgmt.getGraphIndex("byNV");
            if(byNV == null)
            {
                byNV = mgmt.buildIndex("byNV", Vertex.class)
                    .indexOnly(nodeVertexLabel).addKey(nv)
                    .buildCompositeIndex();
                mgmt.setConsistency(byNV, ConsistencyModifier.DEFAULT);
            }
        }

        TitanGraphIndex byNidAndNv = mgmt.getGraphIndex("byNidAndNv");
        if(byNidAndNv == null)
        {
            byNidAndNv = mgmt.buildIndex("byNidAndNv", Vertex.class)
                .indexOnly(nodeVertexLabel)
                .addKey(nid)
                .addKey(nv)
                .buildCompositeIndex();
            mgmt.setConsistency(byNidAndNv, ConsistencyModifier.DEFAULT);
        }

        PropertyKey isLatest = mgmt.getPropertyKey("isLatest");
        if(isLatest == null)
        {
            isLatest = mgmt.makePropertyKey("isLatest").dataType(Boolean.class).make();

            TitanGraphIndex byNidAndLatest = mgmt.getGraphIndex("byNidAndLatest");
            if(byNidAndLatest == null)
            {
                byNidAndLatest = mgmt.buildIndex("byNidAndLatest", Vertex.class)
                    .indexOnly(nodeVertexLabel)
                    .addKey(nid)
                    .addKey(isLatest)
                    .buildCompositeIndex();
                mgmt.setConsistency(byNidAndLatest, ConsistencyModifier.DEFAULT);
            }
        }

        VertexLabel eventVertexLabel = mgmt.getVertexLabel("EVENT");
        if(eventVertexLabel == null)
        {
            eventVertexLabel = mgmt.makeVertexLabel("EVENT").make();

            PropertyKey username = mgmt.getPropertyKey("username");
            if(username == null)
            {
                username = mgmt.makePropertyKey("username").dataType(String.class).make();

                TitanGraphIndex byUsername = mgmt.getGraphIndex("byUsername");
                if(byUsername == null)
                {
                    byUsername = mgmt.buildIndex("byUsername", Vertex.class)
                        .indexOnly(eventVertexLabel).addKey(username)
                        .buildCompositeIndex();
                    mgmt.setConsistency(byUsername, ConsistencyModifier.DEFAULT);
                }
            }

            PropertyKey eventType = mgmt.getPropertyKey("eventType");
            if(eventType == null)
            {
                eventType = mgmt.makePropertyKey("eventType").dataType(String.class).make();

                TitanGraphIndex byEventType = mgmt.getGraphIndex("byEventType");
                if(byEventType == null)
                {
                    byEventType = mgmt.buildIndex("byEventType", Vertex.class)
                        .indexOnly(eventVertexLabel).addKey(username)
                        .buildCompositeIndex();
                    mgmt.setConsistency(byEventType, ConsistencyModifier.DEFAULT);
                }
            }

            PropertyKey ts = mgmt.getPropertyKey("ts");
            if(ts == null)
            {
                ts = mgmt.makePropertyKey("ts").dataType(Date.class).make();

                TitanGraphIndex byTs = mgmt.getGraphIndex("byTs");
                if(byTs == null)
                {
                    byTs = mgmt.buildIndex("byTs", Vertex.class)
                        .indexOnly(eventVertexLabel).addKey(ts)
                        .buildCompositeIndex();
                    mgmt.setConsistency(byTs, ConsistencyModifier.DEFAULT);
                }
            }
        }

        EdgeLabel nodePropertiesEdgeLabel = mgmt.getEdgeLabel("properties");
        if(nodePropertiesEdgeLabel == null)
        {
            nodePropertiesEdgeLabel = mgmt.makeEdgeLabel("properties").make();
        }

        EdgeLabel childrenEdgeLabel = mgmt.getEdgeLabel("child");
        if(childrenEdgeLabel == null)
        {
            childrenEdgeLabel = mgmt.makeEdgeLabel("child").multiplicity(Multiplicity.MULTI).make();
        }

        PropertyKey permission = mgmt.getPropertyKey("permission");
        if(permission == null)
        {
            permission = mgmt.makePropertyKey("permission").dataType(String.class)
                    .make();
        }

        PropertyKey authority = mgmt.getPropertyKey("authority");
        if(authority == null)
        {
            authority = mgmt.makePropertyKey("authority").dataType(String.class)
                .make();
        }

        TitanGraphIndex byAuthAndPer = mgmt.getGraphIndex("byAuthAndPer");
        if(byAuthAndPer == null)
        {
            byAuthAndPer = mgmt.buildIndex("byAuthAndPer", Edge.class).addKey(permission).addKey(authority)
                    .indexOnly(childrenEdgeLabel).buildCompositeIndex();
        }

        mgmt.commit();

        graph.tx().submit(new Function<Graph, Vertex>() {
            public Vertex apply(Graph g)
            {
                Vertex rootAuth  = g.traversal().V()
                        .hasLabel("AUTHORITY")
                        .has("authName", "Root")
                        .tryNext()
                        .orElseGet(() -> {
                            Vertex p = g.addVertex(T.label, "AUTHORITY", "authName", "Root");
                            return p;
                        });
                return rootAuth;
            }
        })
        .exponentialBackoff(5);
    }

    private String makeNodeId(String nodeId, int nodeVersion)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeId);
        sb.append(".");
        sb.append(String.valueOf(nodeVersion));
        return sb.toString();
    }

    @Override
    public void addPermission(String permission)
    {
        try
        {
            graph.addVertex("PERMISSION").property("permName", permission);
        }
        finally
        {
            graph.tx().commit();
        }
    }

    @Override
    public List<String> getContainedPermissions(String permission)
    {
        return graph.traversal().V().hasLabel("PERMISSION")
                .has("permName", permission)
                .tryNext()
                .map(parent -> {
                    Stream<String> containedPermissions = graph.traversal().V(parent)
                            .repeat(out("child"))
                            .map(v -> (String) v.get().property("permName").value())
                            .toStream();
                    return containedPermissions;
                })
                .orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    @Override
    public void addChildPermission(String parentPermission, String childPermission)
    {
        try
        {
            Vertex parent = graph.traversal().V().hasLabel("PERMISSION")
                    .has("permName", parentPermission)
                    .tryNext()
                    .orElseGet(() -> {
                        TitanVertex p = graph.addVertex(T.label, "PERMISSION", "permName", parentPermission);
                        return p;
                    });

            Vertex child = graph.traversal().V().hasLabel("PERMISSION")
                    .has("permName", childPermission)
                    .tryNext()
                    .orElseGet(() -> {
                        TitanVertex p = graph.addVertex(T.label, "PERMISSION", "permName", childPermission);
                        return p;
                    });

            parent.addEdge("child", child);
        } finally
        {
            graph.tx().commit();
        }
    }

    @Override
    public void addAuthority(String authority)
    {
        graph.tx().submit(new Function<Graph, Vertex>() {
            public Vertex apply(Graph g)
            {
                Vertex auth = g.addVertex("AUTHORITY");
                auth.property("authName", authority);

                return auth;
            }
        })
        .exponentialBackoff(5);
    }

    @Override
    public void addChildAuthority(String parentAuthority, String childAuthority)
    {
        graph.tx().submit(new Function<Graph, Edge>() {
            public Edge apply(Graph g)
            {
                Vertex parent = g.traversal().V()
                        .hasLabel("AUTHORITY")
                        .has("authName", parentAuthority)
                        .tryNext()
                        .orElseGet(() -> {
                            Vertex p = g.addVertex("AUTHORITY");
                            p.property("authName", parentAuthority);
                            return p;
                        });

                Vertex child = g.traversal().V()
                        .hasLabel("AUTHORITY")
                        .has("authName", childAuthority)
                        .tryNext()
                        .orElseGet(() -> {
                            Vertex p = g.addVertex("AUTHORITY");
                            p.property("authName", childAuthority);
                            return p;
                        });

                Edge edge = parent.addEdge("child", child);

                return edge;
            }
        })
        .exponentialBackoff(5);
    }

    @Override
    public void removeChildAuthority(String parentAuthority, String childAuthority)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public long countChildAuthorities(String parentAuthority)
    {
        graph.traversal().V()
            .hasLabel("AUTHORITY")
            .has("authName", parentAuthority);
        long count = graph.traversal().V()
                .hasLabel("AUTHORITY")
                .has("authName", parentAuthority)
                .out("child")
                .count()
                .next();
        return count;
    }

    @Override
    public Stream<String> getChildAuthoritiesAsStream(String parentAuthority)
    {
        return getChildAuthoritiesAsStream(parentAuthority, null, null);
    }

    @Override
    public Stream<String> getChildAuthoritiesAsStream(String parentAuthority, Integer skip, Integer limit)
    {
        if(skip == null)
        {
            skip = 0;
        }

        if(limit == null)
        {
            limit = Integer.MAX_VALUE;
        }

        try
        {
            Stream<String> childAuths = graph.traversal().V()
                    .hasLabel("AUTHORITY")
                    .has("authName", parentAuthority)
                    .out("child")
                    .order().by(groupVertexComparator)
                    .range(skip, limit)
                    .map(e -> {
                        return (String) e.get().property("authName").value();
                    })
                    .toStream();
            return childAuths;
        }
        finally
        {
            graph.tx().commit();
        }
    }

    @Override
    public Stream<String> getContainedAuthoritiesAsStream(String parentAuthority)
    {
        return getContainedAuthoritiesAsStream(parentAuthority, null, null);
    }

    @Override
    public Stream<String> getContainedAuthoritiesAsStream(String parentAuthority, Integer skip, Integer limit)
    {
        if(skip == null)
        {
            skip = 0;
        }

        if(limit == null)
        {
            limit = Integer.MAX_VALUE;
        }

        Stream<String> stream = graph.traversal()
                .V().hasLabel("AUTHORITY").has("authName", parentAuthority)
                .repeat(out("child")).times(10)
                .emit()
                .order().by(groupVertexComparator)
                .range(skip, limit)
                .map(e -> {
                    return (String) e.get().property("authName").value();
                })
                .toStream();
        return stream;
    }

    public boolean hasAuthority(String parentAuthority, String childAuthority)
    {
        boolean hasAuthority = graph.traversal().V()
                .hasLabel("AUTHORITY")
                .has("authName", parentAuthority)
                .repeat(out("child"))
                .emit(has("authName", childAuthority))
                .until(has("authName", childAuthority).or(outE().count().is(0)))
                .tryNext()
                .isPresent();
        return hasAuthority;
    }

    private String nodeToString(Vertex nodeVertex)
    {
        StringBuilder sb = new StringBuilder();

        String nodeId = makeNodeId(nodeVertex.<String>property("nid").orElse("<unknown>"),
                nodeVertex.<Integer>property("nv").orElse(-1));
        sb.append(nodeId);

        return sb.toString();
    }

    private List<Node> getAssocs(String parentNodeId, int parentNodeVersion, String assocType,
            String permission, String authority, final int skip, final int limit)
    {
        return graph.traversal().V()
            .hasLabel("NODE")
            .has("nid", parentNodeId)
            .has("nv", parentNodeVersion)
            .and(out(permission)
                .repeat(out("child")) // auth vertex
                .until(has("authName", authority))
//                .sideEffect(v -> {
//                    logger.debug("Permission " + permission + " ok for auth " + authority
//                            + " for node " + makeNodeId(parentNodeId, parentNodeVersion));
//                })
            )
            .out(assocType)
//            .sideEffect(v -> {
//                logger.info("getAssocs node " + v.get().id() + ":" + v.get().property("nid").value());
//            })
            .and(out("Read")
                    .repeat(out("child")) // auth vertex
                    .until(has("authName", authority))
//                    .sideEffect(v -> {
//                        logger.info("getAssocs for " + assocType
//                                + ", auth = " + authority
//                                + ", child auth "
//                                + v.get().id()
//                                + ":"
//                                + v.get().property("authName"));
//                    })
             )
//            .sideEffect(v -> {
//                logger.info("Permission ok for child " + nodeToString(v.get()));
//            })
            .order().by(nodeComparator)
            .range(skip, limit)
            .map(v -> {
                Vertex nodeVertex = v.get();

                Optional<Properties> properties = graph.traversal().V(nodeVertex)
                    .out("ReadProperties")
                    .repeat(out("child")) // auth vertex
                    .until(has("authName", authority))
                    .tryNext()
                    // we have a ReadProperties permission, construct properties
                    .map(pv -> getNodePropertiesImpl(nodeVertex))
                    .orElse(Optional.empty());

                String nodeId = (String)nodeVertex.property("nid").value();
                int nodeVersion = (Integer)nodeVertex.property("nv").value();

                return Node.withNodeId(nodeId).withNodeVersion(nodeVersion).withProperties(properties);
             })
            .toStream()
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Properties> getNodeProperties(String nodeId, int nodeVersion,
            String permission, String authority)
    {
        Optional<Properties> properties = graph.traversal().V()
                .hasLabel("NODE")
                .has("nid", nodeId)
                .has("nv", nodeVersion)
                .tryNext()
                .map(nodeVertex -> getNodePropertiesImpl(nodeVertex))
                .get();
//        Optional<Properties> properties = nodeOpt.flatMap(node -> {
//            return graph.traversal().V(node).outE("PROPERTIES").has("authority", authority)
//                    .has("permission", permission).inV().range(0, 1)
//                    .map(v -> Properties.fromVertex(v.get().properties())).tryNext();
//        });

        return properties;
    }

    @Override
    public List<Node> getChildren(String parentNodeId, int parentNodeVersion, String authority,
            final int skip, final int limit)
    {
        return getAssocs(parentNodeId, parentNodeVersion, "child", "ReadChildren", authority, skip,
                limit);
    }

//    @Override
//    public void setPropertiesPermission(String nodeId, int nodeVersion, String authority)
//    {
//        final List<String> containedPerms = getContainedPermissions("ReadProperties");
//        containedPerms.add("ReadProperties");
//
//        Vertex source = graph.traversal().V()
//                .hasLabel("NODE")
//                .has("nid", nodeId)
//                .has("nv", nodeVersion)
//                .map(v -> {
//                    try (Stream<String> containedAuths = Stream.concat(Stream.of(authority),
//                            getContainedAuthoritiesAsStream(authority, null, null)))
//                    {
//                        containedAuths
//                                .flatMap(auth -> containedPerms.stream()
//                                        .<AuthAndPerm> map(perm -> new AuthAndPerm(auth, perm)))
//                                .forEach(authAndPerm -> {
//                                    Edge edge = source.addEdge("Permissions", target);
//                                    edge.property("permission", authAndPerm.getPermission());
//                                    edge.property("authority", authAndPerm.getAuthority());
//                                });
//                    }
//                })
//                .tryNext()
//                .orElseGet(() -> {
//                    TitanVertex p = graph.addVertex(T.label, "NODE", "nodeId", nodeId,
//                            "nodeVersion", nodeVersion);
//                    return p;
//                });
//        // source.property(key, value);
//
//        Vertex target = graph.traversal().V().hasLabel("PROPERTIES").has("nodeId", nodeId)
//                .has("nodeVersion", nodeVersion).tryNext().orElseGet(() -> {
//                    TitanVertex p = graph.addVertex(T.label, "NODE", "nodeId", nodeId,
//                            "nodeVersion", nodeVersion);
//                    return p;
//                });
//
//        try (Stream<String> containedAuths = Stream.concat(Stream.of(authority),
//                getContainedAuthoritiesAsStream(authority, null, null)))
//        {
//            containedAuths
//                    .flatMap(auth -> containedPerms.stream()
//                            .<AuthAndPerm> map(perm -> new AuthAndPerm(auth, perm)))
//                    .forEach(authAndPerm -> {
//                        Edge edge = source.addEdge("Permissions", target);
//                        edge.property("permission", authAndPerm.getPermission());
//                        edge.property("authority", authAndPerm.getAuthority());
//                    });
//        }
//    }

    private Vertex addNodeImpl(Graph g, String nodeId, int nodeVersion, Auths auths,
            Properties properties, boolean latest)
    {
        String nid = makeNodeId(nodeId, nodeVersion);
        Vertex nodeVertex = g.addVertex("NODE");
        nodeVertex.property("nid", nodeId);
        nodeVertex.property("nv", nodeVersion);
        nodeVertex.property("latest", latest);
        for(Map.Entry<String, Object> property : properties.getProperties().entrySet())
        {
            nodeVertex.property(property.getKey(), property.getValue());
        }

        auths.getAuths().entrySet().stream().forEach(e -> {
            String perm = e.getKey();
            final List<String> containedPerms = getContainedPermissions(perm);
            containedPerms.add(perm);

            String authName = e.getValue();

            g.traversal().V()
                .has("AUTHORITY", "authName", authName)
                .tryNext()
                .ifPresent(auth -> {
                    containedPerms.stream().forEach(p -> {
                        logger.debug("Add perm " + p + " for auth " + authName
                                + " for node " + nid);
                        nodeVertex.addEdge(p, auth);
                    });
                });
        });

        return nodeVertex;
    }

    private Optional<Properties> getNodePropertiesImpl(Vertex nodeVertex)
    {
        Optional<Properties> properties = graph.traversal().V(nodeVertex)
                .out("model")
                .map(mv -> {
                    Model model = new Model(mv.get().properties());
                    List<Property> propsList = model.getProperties().entrySet().stream()
                            .flatMap(p -> {
                                String propertyName = p.getKey();
                                VertexProperty<Object> vp = nodeVertex.property(propertyName);
                                return vp.isPresent() ?
                                        Stream.of(new Property(propertyName, vp.value())) :
                                            Stream.empty();
                            })
                            .collect(Collectors.toList());
                    Properties props = new Properties(propsList);
                    return props;
                })
              .tryNext();
        return properties;
    }

    @Override
    public void addNode(String nodeId, int nodeVersion, Auths auths, Properties properties)
    {
        graph.tx().submit(new Function<Graph, Vertex>() {
            public Vertex apply(Graph g)
            {
                return addNodeImpl(g, nodeId, nodeVersion, auths, properties,true);
            }
        })
        .exponentialBackoff(5);
    }

    @Override
    public boolean deleteNode(String nodeId, int nodeVersion)
    {
        return graph.tx().submit(new Function<Graph, Boolean>() {
            public Boolean apply(Graph g)
            {
                return g.traversal().V()
                .hasLabel("NODE")
                .has("nid", nodeId)
                .has("nv", nodeVersion)
                .tryNext()
                .map(nv -> {
                    nv.remove();
                    return true;
                })
                .orElse(false);
            }
        })
        .exponentialBackoff(5);
    }

    @Override
    public void versionNode(String nodeId, Auths auths)
    {
        graph.tx().submit(new Function<Graph, Vertex>() {
            public Vertex apply(Graph g)
            {
                Optional<Vertex> head = graph.traversal().V()
                    .hasLabel("NODE")
                    .has("nid", nodeId)
                    .has("latest", true)
                    .map(v -> {
                        Vertex nodeVertex = v.get();

                        int nodeVersion = (Integer)nodeVertex.property("nv").value();
                        Properties properties = Properties.fromVertex(nodeVertex.properties());
                        Vertex newHead = addNodeImpl(g, nodeId, nodeVersion + 1, auths,
                                properties, true);
                        Edge edge = newHead.addEdge("EDGE", v.get());
                        edge.property("assocType", "version");

                        nodeVertex.property("latest", false);

                        return newHead;
                    })
                    .tryNext();
                return head.orElseThrow(() -> new IllegalArgumentException("Invalid node " + nodeId));
            }
        })
        .exponentialBackoff(5);
    }

    @Override
    public void addAssoc(String sourceNodeId, int sourceNodeVersion, String assocType,
            String targetNodeId, int targetNodeVersion)
    {
        graph.tx().submit(new Function<Graph, Void>() {
            public Void apply(Graph g)
            {
                g.traversal().V()
                        .hasLabel("NODE")
                        .has("nid", sourceNodeId)
                        .has("nv", sourceNodeVersion)
                        .sideEffect(nv -> {
                            logger.debug("addAssoc found source " + nodeToString(nv.get()));
                        })
                        .tryNext()
                        .ifPresent(source -> {
                            g.traversal().V()
                                .hasLabel("NODE")
                                .has("nid", targetNodeId)
                                .has("nv", targetNodeVersion)
                                .sideEffect(nv -> {
                                    logger.debug("addAssoc found target " + nodeToString(nv.get()));
                                })
                                .tryNext()
                                .ifPresent(target -> source.addEdge(assocType, target));
                         });

                return null;
            }
        })
        .exponentialBackoff(5);
    }

    @Override
    public void addEvent(String nodeId, int nodeVersion, ActivityEvent event)
    {
        graph.tx().submit(new Function<Graph, Void>() {
            public Void apply(Graph g)
            {
                Vertex nodeVertex = g.traversal().V()
                        .hasLabel("NODE")
                        .has("nid", nodeId)
                        .has("nv", nodeVersion)
                        .tryNext()
                        .orElseGet(() -> {
                            return addNodeImpl(g, nodeId, nodeVersion, defaultNodeAuths,
                                    Properties.empty(), true);
                        });

                logger.info("Add event " + event + " for node "
                        + makeNodeId(nodeId, nodeVersion) + ", " + nodeVertex.id());

                Vertex eventV = g.addVertex(T.label, "EVENT");
                eventV.property("username", event.getUsername());
                eventV.property("eventType", event.getType());
                eventV.property("ts", event.getTimestamp());
                nodeVertex.addEdge("event", eventV, "eventType", event.getType());

                return null;
            }
        })
        .exponentialBackoff(5);
    }

    @Override
    public Stream<Event> getEvents(long minTs, Integer skip, Integer limit)
    {
        if(skip == null)
        {
            skip = 0;
        }

        if(limit == null)
        {
            limit = Integer.MAX_VALUE;
        }

        try
        {
            Stream<Event> events = graph.traversal().V()
                    .hasLabel("EVENT")
                    .has("ts", gte(minTs))
//                    .and(out("ReadEvents")
//                            .repeat(out("child")) // auth vertex
//                            .until(v -> v.get().property("authName").value().equals(authority)
//                                    || !v.get().edges(Direction.OUT, "AUTHORITY").hasNext()))
//                    .out("event")
//                    .sideEffect(ev -> {
//                        logger.debug("Event " + ev.get().label() + ", " + ev.get().properties());
//                     })
                    .order().by(eventVertexComparator)
                    .map(evt -> {
                        String eventType = (String)evt.get().property("eventType").orElse("Unknown event type");
                        String username = (String)evt.get().property("username").orElse("Unknown username");
                        Date ts = (Date)evt.get().property("ts").orElse("Unknown ts");
                        return new Event(eventType, username, ts);
                     })
                    .range(skip, limit)
                    .toStream();
            return events;
        }
        finally
        {
            graph.tx().commit();
        }
    }

    @Override
    public Stream<Event> getEvents(String nodeId, int nodeVersion, Integer skip, Integer limit,
            String authority)
    {
        if(skip == null)
        {
            skip = 0;
        }

        if(limit == null)
        {
            limit = Integer.MAX_VALUE;
        }

        try
        {
            Stream<Event> events = graph.traversal().V()
                    .hasLabel("NODE")
                    .has("nid", nodeId)
                    .has("nv", nodeVersion)
                    .sideEffect(nv -> {
                        logger.debug("Get events for " + makeNodeId(nodeId, nodeVersion)
                            + ", " + nv.get().id());
                    })
                    .and(out("ReadEvents")
                            .repeat(out("child")) // auth vertex
                            .until(v -> v.get().property("authName").value().equals(authority)
                                    || !v.get().edges(Direction.OUT, "AUTHORITY").hasNext()))
                    .out("event")
                    .sideEffect(ev -> {
                        logger.debug("Event " + ev.get().label() + ", " + ev.get().properties());
                     })
                    .order().by(eventVertexComparator)
                    .map(evt -> {
                        String eventType = (String)evt.get().property("eventType").orElse("Unknown event type");
                        String username = (String)evt.get().property("username").orElse("Unknown username");
                        Date ts = (Date)evt.get().property("ts").orElse("Unknown ts");
                        return new Event(eventType, username, ts);
                     })
                    .range(skip, limit)
                    .toStream();
            return events;
        }
        finally
        {
            graph.tx().commit();
        }
    }
}
