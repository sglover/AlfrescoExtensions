/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao.titan;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alfresco.permissions.dao.Node;
import org.alfresco.permissions.dao.PermissionsDAO;
import org.alfresco.permissions.dao.Properties;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;

/**
 * 
 * @author sglover
 * 
 *  Pre-requisites: Cassandra: cd /Users/sglover/dev/apache-cassandra-2.1.2; bin/cassandra -f
 *  ElasticSearch: cd /Users/sglover/dev/elasticsearch-1.4.4; bin/elasticsearch -d
 */
public class TitanPermissionsDAO implements PermissionsDAO
{
    public static final String INDEX_NAME = "search";

    private TitanGraph graph;

    public TitanPermissionsDAO(List<TitanConfig> titanConfigs)
    {
        buildTitan(titanConfigs);

        addChildPermission("Read", "ReadChildren");
        addChildPermission("Read", "ReadProperties");
        addChildPermission("Read", "ReadContent");
    }

    public interface TitanConfig
    {
        void apply(TitanFactory.Builder config);
    }

    public static class LocalCassandraConfig implements TitanConfig
    {
        private String cassandraYamlPath;

        public LocalCassandraConfig(String cassandraYamlPath)
        {
            this.cassandraYamlPath = cassandraYamlPath;
        }

        public void apply(TitanFactory.Builder config)
        {
            config.set("storage.backend", "embeddedcassandra");
            config.set("storage.conf-file", cassandraYamlPath);
        }
    }

    public static class RemoteCassandraConfig implements TitanConfig
    {
        private String cassandraHost;
        private boolean cassandraRemoteRecreate;

        public RemoteCassandraConfig(String cassandraHost, boolean recreate)
        {
            this.cassandraHost = cassandraHost;
            this.cassandraRemoteRecreate = recreate;
        }

        public void apply(TitanFactory.Builder config)
        {
            config.set("storage.backend", "cassandrathrift");
            config.set("storage.hostname", cassandraHost);

            Cluster cluster = Cluster.builder().addContactPoint(cassandraHost)
                    .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                    .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                    .build();
            Session cassandraSession = cluster.connect();

            if (cassandraRemoteRecreate)
            {
                KeyspaceMetadata keySpaceMetadata = cassandraSession.getCluster().getMetadata()
                        .getKeyspace("titan");
                if (keySpaceMetadata != null)
                {
                    cassandraSession.execute("DROP KEYSPACE titan;");
                }
            }
        }
    }

    public static class LocalElasticSearchConfig implements TitanConfig
    {
        private String directory;

        public LocalElasticSearchConfig(String directory)
        {
            this.directory = directory;
        }

        public void apply(TitanFactory.Builder config)
        {
            config.set("index." + INDEX_NAME + ".backend", "elasticsearch");
            config.set("index." + INDEX_NAME + ".directory", directory + File.separator + "es");
            config.set("index." + INDEX_NAME + ".elasticsearch.local-mode", true);
            config.set("index." + INDEX_NAME + ".elasticsearch.client-only", false);
        }
    }

    public static class RemoteElasticSearchConfig implements TitanConfig
    {
        private String hostname;
        private Client client;
        private boolean deleteIndex;

        public RemoteElasticSearchConfig(String hostname, boolean deleteIndex)
        {
            this.hostname = hostname;
            this.deleteIndex = deleteIndex;

            org.elasticsearch.node.Node node = nodeBuilder()
                    ./* clusterName("elk"). */node();
            this.client = node.client();
        }

        private void deleteIndex(String name) throws IOException
        {
            if (indexExists(name))
            {
                DeleteIndexResponse delete = client.admin().indices()
                        .delete(new DeleteIndexRequest(name)).actionGet();
                if (!delete.isAcknowledged())
                {
                    throw new RuntimeException("Index wasn't deleted");
                }
            } else
            {
                // logger.debug("Index " + name + " not deleted because it does
                // not exist");
            }
        }

        private boolean indexExists(String name) throws IOException
        {
            boolean exists = false;

            ActionFuture<IndicesExistsResponse> res = client.admin().indices()
                    .exists(new IndicesExistsRequest(name));
            IndicesExistsResponse indicesExistsResp = res.actionGet(2000);
            if (indicesExistsResp.isExists())
            {
                exists = true;
            }

            return exists;
        }

        public void apply(TitanFactory.Builder config)
        {
            config.set("index." + INDEX_NAME + ".backend", "elasticsearch");
            config.set("index." + INDEX_NAME + ".elasticsearch.interface", "TRANSPORT_CLIENT");
            config.set("index." + INDEX_NAME + ".elasticsearch.local-mode", false);
            config.set("index." + INDEX_NAME + ".index-name", "alfresco");
            config.set("index." + INDEX_NAME + ".elasticsearch.cluster-name", "elk");
            // "10.0.0.10,10.0.0.20:7777"
            config.set("index." + INDEX_NAME + ".hostname", hostname + ":9300");

            if (deleteIndex)
            {
                try
                {
                    deleteIndex("alfresco");
                } catch (IOException e)
                {
                    throw new RuntimeException("Unable to remove elastic search index");
                }
            }
        }
    }

    private void buildTitan(List<TitanConfig> titanConfigs)
    {
        TitanFactory.Builder config = TitanFactory.build();
        for (TitanConfig titanConfig : titanConfigs)
        {
            titanConfig.apply(config);
        }

        this.graph = config.open();
        load(INDEX_NAME);
    }

    private void load(String mixedIndexName)
    {
        // Create Schema
        TitanManagement mgmt = graph.openManagement();

        final PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).make();

        VertexLabel authorityVertexLabel = mgmt.makeVertexLabel("AUTHORITY").make();
        TitanGraphIndex byAuthorityName = mgmt.buildIndex("byAuthorityName", Vertex.class)
                .indexOnly(authorityVertexLabel).addKey(name)
                // .unique()
                .buildCompositeIndex();
        mgmt.setConsistency(byAuthorityName, ConsistencyModifier.LOCK);

        VertexLabel permissionVertexLabel = mgmt.makeVertexLabel("PERMISSION").make();
        TitanGraphIndex byPermissionName = mgmt.buildIndex("byPermissionName", Vertex.class)
                .indexOnly(permissionVertexLabel).addKey(name)
                // .unique()
                .buildCompositeIndex();
        mgmt.setConsistency(byPermissionName, ConsistencyModifier.LOCK);

        VertexLabel nodeVertexLabel = mgmt.makeVertexLabel("NODE").make();
        final PropertyKey nodeId = mgmt.makePropertyKey("nodeId").dataType(String.class).make();
        final PropertyKey nodeVersion = mgmt.makePropertyKey("nodeVersion").dataType(String.class)
                .make();
        TitanGraphIndex byNode = mgmt.buildIndex("byNodeId", Vertex.class).addKey(nodeId)
                .addKey(nodeVersion).indexOnly(nodeVertexLabel).buildCompositeIndex();
        // .buildMixedIndex(mixedIndexName);
        mgmt.setConsistency(byNode, ConsistencyModifier.LOCK);

        VertexLabel nodePropertiesVertexLabel = mgmt.makeVertexLabel("PROPERTIES").make();
        TitanGraphIndex propertiesByNode = mgmt.buildIndex("propertiesByNode", Vertex.class)
                .addKey(nodeId).addKey(nodeVersion).indexOnly(nodePropertiesVertexLabel)
                .buildCompositeIndex();
        // .buildMixedIndex(mixedIndexName);
        mgmt.setConsistency(propertiesByNode, ConsistencyModifier.LOCK);

        EdgeLabel childrenEdgeLabel = mgmt.makeEdgeLabel("child").make();
        final PropertyKey permission = mgmt.makePropertyKey("permission").dataType(String.class)
                .make();
        final PropertyKey authority = mgmt.makePropertyKey("authority").dataType(String.class)
                .make();
        mgmt.buildIndex("byAuthAndPer", Edge.class).addKey(permission).addKey(authority)
                .indexOnly(childrenEdgeLabel).buildCompositeIndex();

        mgmt.commit();

        try
        {
            graph.addVertex(T.label, "AUTHORITY", "name", "Root");
        } finally
        {
            graph.tx().commit();
        }
    }

    // @Override
    // public void addChildPermission(String parentPermission, String
    // childPermission)
    // {
    //// TitanTransaction tx = graph.newTransaction();
    //
    // List<Vertex> l = graph.traversal().V().hasLabel("PERMISSION").has("name",
    // parentPermission).toList();
    // Vertex parent = null;
    // if(l.size() > 0)
    // {
    // parent = l.get(0);
    // }
    // else
    // {
    // parent = graph.addVertex(T.label, "PERMISSION", "name",
    // parentPermission);
    // }
    // }

    @Override
    public void addPermission(String permission)
    {
        try
        {
            graph.addVertex(T.label, "PERMISSION", "name", permission);
        } finally
        {
            graph.tx().commit();
        }
    }

    @Override
    public List<String> getContainedPermissions(String permission)
    {
        Optional<Vertex> parentOpt = graph.traversal().V().hasLabel("PERMISSION")
                .has("name", permission).tryNext();
        Stream<String> s = parentOpt.flatMap(parent -> {
            GraphTraversal<Vertex, Vertex> containedPermissions = graph.traversal().V(parent)
                    .repeat(__.out("child")).emit();
            Stream<String> stream = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(containedPermissions,
                            Spliterator.ORDERED), false)
                    .map(v -> (String) v.property("name").value());
            return Optional.of(stream);
        }).orElse(Stream.empty());
        return s.collect(Collectors.toList());
    }

    @Override
    public void addChildPermission(String parentPermission, String childPermission)
    {
        try
        {
            Optional<Vertex> parentOpt = graph.traversal().V().hasLabel("PERMISSION")
                    .has("name", parentPermission).tryNext();
            Vertex parent = parentOpt.orElseGet(() -> {
                TitanVertex p = graph.addVertex(T.label, "PERMISSION", "name", parentPermission);
                return p;
            });

            TitanVertex child = graph.addVertex(T.label, "PERMISSION", "name", childPermission);

            parent.addEdge("child", child);
        } finally
        {
            graph.tx().commit();
        }

        // List<Vertex> l =
        // tx.traversal().V().hasLabel("PERMISSION").has("name",
        // parentPermission).toList();
        // Vertex parent = null;
        // if(l.size() > 0)
        // {
        // parent = l.get(0);
        // }
        // else
        // {
        // parent = tx.addVertex(T.label, "PERMISSION", "name",
        // parentPermission);
        // }

        // Iterator it = graph.query().has("type", "PERMISSION").has("name",
        // parentPermission).vertices().iterator();
        // Vertex parent;
        // if(!it.hasNext())
        // {
        // parent = tx.addVertex(T.label, parentPermission, "name",
        // parentPermission, "type", "PERMISSION");
        // }
        // else
        // {
        // parent = (Vertex)it.next();
        // }
        // Optional<Vertex> parentOpt = graph.query().has("type",
        // "PERMISSION").has("name",
        // parentPermission).vertices().iterator().tryNext();
        // Vertex parent = parentOpt.orElseGet(()->
        // {
        // TitanVertex p = tx.addVertex(T.label, parentPermission, "name",
        // parentPermission, "type", "PERMISSION");
        //// TitanVertex p = graph.addVertex(T.label, parentPermission, "name",
        // parentPermission, "type", "PERMISSION");
        // return p;
        // });

        // TitanVertex child = tx.addVertex(T.label, childPermission, "name",
        // childPermission, "type", "PERMISSION");
    }

    @Override
    public void addChildAuthority(String parentAuthority, String childAuthority)
    {
        // Vertex parent = g.V().has("type", "Authority").has("name",
        // parentAuthority).next();

        try
        {
            Optional<Vertex> parentOpt = graph.traversal().V().hasLabel("AUTHORITY")
                    .has("name", parentAuthority).tryNext();
            Vertex parent = parentOpt.orElseGet(() -> {
                TitanVertex p = graph.addVertex(T.label, "AUTHORITY", "name", parentAuthority);
                return p;
            });
            // TitanVertex parent = tx.query().has("name",
            // parentAuthority).vertices().iterator().next();

            TitanVertex child = graph.addVertex(T.label, "AUTHORITY", "name", childAuthority);

            parent.addEdge("child", child);
        } finally
        {
            graph.tx().commit();
        }
    }

    @Override
    public void removeChildAuthority(String parentAuthority, String childAuthority)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Stream<String> getChildAuthoritiesAsStream(String parentAuthority)
    {
        try
        {
            Vertex parent = graph.traversal().V().hasLabel("AUTHORITY").has("name", parentAuthority)
                    .next();

            Iterator<Vertex> vertices = parent.vertices(Direction.OUT, "child");

            Stream<String> stream = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(vertices, Spliterator.ORDERED),
                            false)
                    .map(v -> {
                        return (String) v.property("name").value();
                    });
            return stream;
        } finally
        {
            graph.tx().commit();
        }
    }

    @Override
    public Stream<String> getContainedAuthoritiesAsStream(String parentAuthority)
    {
        Vertex group1 = graph.traversal().V().hasLabel("AUTHORITY").has("name", parentAuthority)
                .next();

        GraphTraversal<Vertex, Vertex> containedAuthorities = graph.traversal().V(group1)
                .repeat(__.out("child")).emit();
        Stream<String> stream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(containedAuthorities, Spliterator.ORDERED),
                false).map(v -> {
                    return (String) v.property("name").value();
                });
        return stream;
    }

    private List<Node> getAssocs(String parentNodeId, String parentNodeVersion, String assocType,
            String permission, String authority, final int skip, final int limit)
    {
        Optional<Vertex> parentNodeOpt = graph.traversal().V().hasLabel("NODE")
                .has("nodeId", parentNodeId).has("nodeVersion", parentNodeVersion).tryNext();
        List<Node> assocs = parentNodeOpt.flatMap(parentNode -> Optional
                .of(graph.traversal().V(parentNode).outE(assocType).has("authority", authority)
                        .has("permission", permission).inV().range(skip, limit)
                        // .as("x")
                        // .emit(t ->
                        // {
                        // String val = (String)t.get().property(permission +
                        // "." + authority).orElse(null);
                        // return (val != null && val.equals("ALLOWED"));
                        // })
                        // .has(permission + "." + authority, "ALLOWED")
                        .emit()
                        .map(t -> new Node((String) t.get().property("nodeId").value(),
                                (String) t.get().property("nodeVersion").value()))
                        // .outE("Permissions")
                        // .has("authority", "ReadProperties")
                        // .has("permission", permission)
                        // .inV()
                        // .emit()
                        // .map(t -> new
                        // Node((String)t.get().property("nodeId").value(),
                        // (String)t.get().property("nodeVersion").value()))
                        .toList()))
                .get();
        return assocs;
    }

    @Override
    public Optional<Properties> getNodeProperties(String nodeId, String nodeVersion,
            String permission, String authority)
    {
        Optional<Vertex> nodeOpt = graph.traversal().V().hasLabel("NODE").has("nodeId", nodeId)
                .has("nodeVersion", nodeVersion).tryNext();
        Optional<Properties> properties = nodeOpt.flatMap(node -> {
            return graph.traversal().V(node).outE("properties").has("authority", authority)
                    .has("permission", permission).inV().range(0, 1)
                    .map(v -> new Properties(v.get().properties())).tryNext();
        });

        return properties;
    }

    @Override
    public List<Node> getChildren(String parentNodeId, String parentNodeVersion, String authority,
            final int skip, final int limit)
    {
        return getAssocs(parentNodeId, parentNodeVersion, "child", "ReadChildren", authority, skip,
                limit);
    }

    private static class AuthAndPerm
    {
        private String authority;
        private String permission;

        public AuthAndPerm(String authority, String permission)
        {
            super();
            this.authority = authority;
            this.permission = permission;
        }

        public String getAuthority()
        {
            return authority;
        }

        public String getPermission()
        {
            return permission;
        }
    }

    @Override
    public void setPropertiesPermission(String nodeId, String nodeVersion, String authority)
    {
        final List<String> containedPerms = getContainedPermissions("ReadProperties");
        containedPerms.add("ReadProperties");

        Vertex source = graph.traversal().V().hasLabel("NODE").has("nodeId", nodeId)
                .has("nodeVersion", nodeVersion).tryNext().orElseGet(() -> {
                    TitanVertex p = graph.addVertex(T.label, "NODE", "nodeId", nodeId,
                            "nodeVersion", nodeVersion);
                    return p;
                });
        // source.property(key, value);

        Vertex target = graph.traversal().V().hasLabel("PROPERTIES").has("nodeId", nodeId)
                .has("nodeVersion", nodeVersion).tryNext().orElseGet(() -> {
                    TitanVertex p = graph.addVertex(T.label, "NODE", "nodeId", nodeId,
                            "nodeVersion", nodeVersion);
                    return p;
                });

        try (Stream<String> containedAuths = Stream.concat(Stream.of(authority),
                getContainedAuthoritiesAsStream(authority)))
        {
            containedAuths
                    .flatMap(auth -> containedPerms.stream()
                            .<AuthAndPerm> map(perm -> new AuthAndPerm(auth, perm)))
                    .forEach(authAndPerm -> {
                        Edge edge = source.addEdge("Permissions", target);
                        edge.property("permission", authAndPerm.getPermission());
                        edge.property("authority", authAndPerm.getAuthority());
                    });
        }
        ;
    }

    @Override
    public void setPermission(String sourceNodeId, String sourceNodeVersion, String assocType,
            String permission, String authority, String targetNodeId, String targetNodeVersion)
    {
        final List<String> containedPerms = getContainedPermissions(permission);
        containedPerms.add(permission);

        Vertex source = graph.traversal().V().hasLabel("NODE").has("nodeId", sourceNodeId)
                .has("nodeVersion", sourceNodeVersion).tryNext().orElseGet(() -> {
                    TitanVertex p = graph.addVertex(T.label, "NODE", "nodeId", sourceNodeId,
                            "nodeVersion", sourceNodeVersion);
                    return p;
                });

        Vertex target = graph.traversal().V().hasLabel("NODE").has("nodeId", targetNodeId)
                .has("nodeVersion", targetNodeVersion).tryNext().orElseGet(() -> {
                    TitanVertex p = graph.addVertex(T.label, "NODE", "nodeId", targetNodeId,
                            "nodeVersion", targetNodeVersion);
                    return p;
                });

        try (Stream<String> containedAuths = Stream.concat(Stream.of(authority),
                getContainedAuthoritiesAsStream(authority)))
        {
            containedAuths
                    .flatMap(auth -> containedPerms.stream()
                            .<AuthAndPerm> map(perm -> new AuthAndPerm(auth, perm)))
                    .forEach(authAndPerm -> {
                        Edge edge = source.addEdge(assocType, target);
                        edge.property("permission", authAndPerm.getPermission());
                        edge.property("authority", authAndPerm.getAuthority());
                    });
        }
        ;
    }
}
