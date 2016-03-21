/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao.cassandra;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alfresco.permissions.CollectionUtils;
import org.alfresco.permissions.dao.Node;
import org.alfresco.permissions.dao.PermissionsDAO;
import org.alfresco.permissions.dao.Properties;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

/**
 * 
 * @author sglover
 *
 */
public class CassandraPermissionsDAO implements PermissionsDAO
{
    private Session session;
    private String keyspace;

    private static Session buildSession(String host)
    {
        Cluster cluster = Cluster
                .builder()
                .addContactPoint(host)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(
                        new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                .build();
        Session session = cluster.connect();
        return session;
    }

    public CassandraPermissionsDAO(String host, String keyspace,
            boolean dropKeyspaceOnInit)
    {
        this(CassandraPermissionsDAO.buildSession(host), keyspace,
                dropKeyspaceOnInit);
    }

    public CassandraPermissionsDAO(Session session, String keyspace,
            boolean dropKeyspaceOnInit)
    {
        this.keyspace = keyspace;
        this.session = session;

        KeyspaceMetadata keySpaceMetadata = session.getCluster().getMetadata()
                .getKeyspace(keyspace);
        if (dropKeyspaceOnInit && keySpaceMetadata != null)
        {
            session.execute("DROP KEYSPACE " + keyspace + ";");
            keySpaceMetadata = null;
        }

        if (keySpaceMetadata == null)
        {
            session.execute("CREATE KEYSPACE " + keyspace
                    + " WITH replication "
                    + "= {'class':'SimpleStrategy', 'replication_factor':3};");

            session.execute("CREATE TABLE "
                    + keyspace
                    + ".node_assocs ("
                    + "source_node_id text, "
                    + "source_node_version text, "
                    + "assoc_type text, "
                    + "permission text, "
                    + "authority text,"
                    + "target_node_id text,"
                    + "target_node_version text, "
                    + "PRIMARY KEY(source_node_id, source_node_version, assoc_type, permission, authority, target_node_id, target_node_version) "
                    + ");");

            session.execute("CREATE TABLE " + keyspace + ".child_permissions ("
                    + "parent_permission text, " + "child_permission text,"
                    + "PRIMARY KEY(parent_permission, child_permission) "
                    + ");");

            session.execute("CREATE TABLE " + keyspace + ".child_authorities ("
                    + "parent_authority text, " + "level int,"
                    + "child_authority text,"
                    + "PRIMARY KEY(parent_authority, level, child_authority) "
                    + ");");

            session.execute("CREATE TABLE " + keyspace
                    + ".parent_authorities (" + "child_authority text, "
                    + "level int," + "parent_authority text,"
                    + "PRIMARY KEY(child_authority, level, parent_authority) "
                    + ");");
        }

        this.insertChildPermission = session.prepare("insert into " + keyspace
                + ".child_permissions "
                + "(parent_permission, child_permission) " + "VALUES(?, ?)");
        this.insertChildAuthority = session.prepare("insert into " + keyspace
                + ".child_authorities "
                + "(parent_authority, level, child_authority) "
                + "VALUES(?, ?, ?)");
        this.removeChildPermission = session.prepare("delete from " + keyspace
                + ".child_permissions "
                + "WHERE parent_permission = ? AND child_permission = ?");
        this.removeChildAuthority = session
                .prepare("delete from "
                        + keyspace
                        + ".child_authorities "
                        + "WHERE parent_authority = ? AND level = ? AND child_authority = ?");
        this.getChildPermissions = session.prepare("select * from " + keyspace
                + ".child_permissions where parent_permission = ?");
        this.getDescendantAuthorities = session
                .prepare("select * from "
                        + keyspace
                        + ".child_authorities where parent_authority = ? AND level > 0");
        this.getChildAuthorities = session
                .prepare("select * from "
                        + keyspace
                        + ".child_authorities where parent_authority = ? AND level = 1");
        this.insertNodeAssoc = session
                .prepare("insert into "
                        + keyspace
                        + ".node_assocs (source_node_id, source_node_version, "
                        + "assoc_type, "
                        + "permission, authority, target_node_id, target_node_version) VALUES(?, ?, ?, ?, ?, ?, ?)");
        this.insertParentAuthority = session.prepare("insert into " + keyspace
                + ".parent_authorities "
                + "(child_authority, level, parent_authority) "
                + "VALUES(?, ?, ?)");
        this.getParentAuthorities = session.prepare("select * from " + keyspace
                + ".parent_authorities "
                + "WHERE child_authority = ? AND level=1");
        this.getAncestorAuthorities = session.prepare("select * from "
                + keyspace + ".parent_authorities "
                + "WHERE child_authority = ?");
        this.removeParentAuthority = session
                .prepare("delete from "
                        + keyspace
                        + ".parent_authorities "
                        + "WHERE child_authority = ? AND level = ? AND parent_authority = ?");
        this.getNodeAssocs = session.prepare("SELECT * FROM " + keyspace
                + ".node_assocs WHERE source_node_id=?"
                + " AND source_node_version=?" + " AND assoc_type=?"
                + " AND permission='ReadChildren'" + " AND authority=?"
                + " LIMIT ?");
    }

    public void drop()
    {
        session.execute("DROP TABLE " + keyspace + ".node_assocs;");
        session.execute("DROP TABLE " + keyspace + ".parent_authorities;");
        session.execute("DROP TABLE " + keyspace + ".child_authorities;");
        session.execute("DROP TABLE " + keyspace + ".child_permissions;");
    }

    private PreparedStatement insertNodeAssoc;
    private PreparedStatement getNodeAssocs;
    private PreparedStatement insertChildAuthority;
    private PreparedStatement removeChildAuthority;
    private PreparedStatement insertParentAuthority;
    private PreparedStatement getParentAuthorities;
    private PreparedStatement getAncestorAuthorities;
    private PreparedStatement removeParentAuthority;
    private PreparedStatement insertChildPermission;
    private PreparedStatement removeChildPermission;
    private PreparedStatement getChildPermissions;
    private PreparedStatement getDescendantAuthorities;
    private PreparedStatement getChildAuthorities;

    @Override
    public Stream<String> getContainedAuthoritiesAsStream(String parentAuthority)
    {
        final BoundStatement boundStatement = getDescendantAuthorities
                .bind(parentAuthority);
        final ResultSet resultSet = session.execute(boundStatement);
        final Spliterator<Row> spliterator = resultSet.spliterator();
        Stream<String> stream = StreamSupport.stream(spliterator, false)
        // .onClose(() -> resultSet.) // need to close cursor;
                .map(row -> {
                    String childAuthority = row.getString("child_authority");
                    return childAuthority;
                });
        return stream;
    }

    @Override
    public Stream<String> getChildAuthoritiesAsStream(String parentAuthority)
    {
        final BoundStatement boundStatement = getChildAuthorities
                .bind(parentAuthority);
        final ResultSet resultSet = session.execute(boundStatement);
        final Spliterator<Row> spliterator = resultSet.spliterator();
        Stream<String> stream = StreamSupport.stream(spliterator, false)
        // .onClose(() -> resultSet.) // need to close cursor;
                .map(row -> {
                    String childAuthority = row.getString("child_authority");
                    return childAuthority;
                });
        return stream;
    }

    public Stream<String> getParentAuthorities(String childAuthority)
    {
        BoundStatement query = getParentAuthorities.bind(childAuthority);
        ResultSet rs = session.execute(query);
        Spliterator<Row> spliterator = rs.spliterator();
        Stream<String> stream = StreamSupport.stream(spliterator, false)
        // .onClose(() -> resultSet.) // need to close cursor;
                .map(row -> {
                    String parentAuthority = row.getString("parent_authority");
                    return parentAuthority;
                });
        return stream;
    }

    public Stream<String> getAncestorAuthorities(String childAuthority)
    {
        BoundStatement query = getAncestorAuthorities.bind(childAuthority);
        ResultSet rs = session.execute(query);
        Spliterator<Row> spliterator = rs.spliterator();
        Stream<String> stream = StreamSupport.stream(spliterator, false)
        // .onClose(() -> resultSet.) // need to close cursor;
                .map(row -> {
                    String parentAuthority = row.getString("parent_authority");
                    return parentAuthority;
                });
        return stream;
    }

    @Override
    public void addChildAuthority(String parentAuthority, String childAuthority)
    {
        Stream<Map.Entry<Integer, String>> ancestorAuthorities = CollectionUtils
                .zipWithIndex(Stream.concat(Stream.of(parentAuthority),
                        getAncestorAuthorities(parentAuthority)), 1);
        ancestorAuthorities.forEach(authLevel -> {
            String ancestorAuthority = authLevel.getValue();
            int level = authLevel.getKey();
            BoundStatement childInsert = insertChildAuthority.bind(
                    ancestorAuthority, level, childAuthority);
            session.execute(childInsert);

            BoundStatement parentInsert = insertParentAuthority.bind(
                    childAuthority, level, ancestorAuthority);
            session.execute(parentInsert);
        });
        //
        // BoundStatement parentInsert =
        // insertParentAuthority.bind(childAuthority, parentAuthority);
        // session.execute(parentInsert);
    }

    @Override
    public void removeChildAuthority(String parentAuthority,
            String childAuthority)
    {
        Stream<Map.Entry<Integer, String>> ancestorAuthorities = CollectionUtils
                .zipWithIndex(Stream.concat(Stream.of(parentAuthority),
                        getParentAuthorities(parentAuthority)), 1);
        ancestorAuthorities.forEach(authLevel -> {
            String ancestorAuthority = authLevel.getValue();
            int level = authLevel.getKey();
            BoundStatement childDelete = removeChildAuthority.bind(
                    ancestorAuthority, level, childAuthority);
            session.execute(childDelete);
            BoundStatement insert = removeParentAuthority.bind(childAuthority,
                    level, parentAuthority);
            session.execute(insert);
        });
    }

    @Override
    public void setPermission(final String sourceNodeId,
            final String parentNodeVersion, final String assocType,
            final String permission, final String authority,
            final String targetNodeId, final String childNodeVersion)
    {
        final List<String> permissions = new LinkedList<>();
        permissions.add(permission);
        permissions.addAll(getChildPermissions(permission));

        Stream<String> authoritiesStream = Stream.concat(Stream.of(authority),
                getContainedAuthoritiesAsStream(authority));
        authoritiesStream.forEach(childAuthority -> {
            permissions.stream().forEach(
                    p -> {
                        setSinglePermission(sourceNodeId, parentNodeVersion,
                                assocType, p, childAuthority, targetNodeId,
                                childNodeVersion);
                    });
        });
    }

    public void setSinglePermission(String parentNodeId,
            String parentNodeVersion, String assocType, String permission,
            String authority, String childNodeId, String childNodeVersion)
    {
        BoundStatement insert = insertNodeAssoc.bind(parentNodeId,
                parentNodeVersion, assocType, permission, authority,
                childNodeId, childNodeVersion);
        session.execute(insert);
    }

    @Override
    // TODO skip
    public List<Node> getChildren(String parentNodeId,
            String parentNodeVersion, String authority, int skip, int limit)
    {
        List<Node> children = new LinkedList<>();

        BoundStatement boundStatement = getNodeAssocs.bind(parentNodeId,
                parentNodeVersion, "Children", authority, limit);
        // boundStatement.setFetchSize(24);
        ResultSet resultSet = session.execute(boundStatement);
        for (Row row : resultSet)
        {
            String childNodeId = row.getString("target_node_id");
            String childNodeVersion = row.getString("target_node_version");
            Node node = new Node(childNodeId, childNodeVersion);
            children.add(node);
        }

        return children;
    }

    @Override
    public void addChildPermission(String parentPermission,
            String childPermission)
    {
        BoundStatement insert = insertChildPermission.bind(parentPermission,
                childPermission);
        session.execute(insert);
    }

    public void removeChildPermission(String parentPermission,
            String childPermission)
    {
        BoundStatement insert = removeChildPermission.bind(childPermission,
                childPermission);
        session.execute(insert);
    }

    public List<String> getChildPermissions(String parentPermission)
    {
        List<String> childPermissions = new LinkedList<>();

        BoundStatement boundStatement = getChildPermissions
                .bind(parentPermission);
        ResultSet resultSet = session.execute(boundStatement);
        for (Row row : resultSet)
        {
            childPermissions.add(row.getString("child_permission"));
        }

        return childPermissions;
    }

    @Override
    public void addPermission(String permission)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getContainedPermissions(String permission)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPropertiesPermission(String nodeId, String nodeVersion,
            String authority)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Optional<Properties> getNodeProperties(String nodeId,
            String nodeVersion, String permission, String authority)
    {
        // TODO Auto-generated method stub
        return null;
    }

    // Observable<String> childAuthorities = getChildAuthorities(authority);
    // final Map<String, String> permissions = new HashMap<>();
    // childAuthorities.subscribe(new Observer<String>()
    // {
    // @Override
    // public void onCompleted()
    // {
    // setPermissions(parentNodeId, parentNodeId, permissions, childNodeId,
    // childNodeVersion);
    // }
    //
    // @Override
    // public void onError(Throwable e)
    // {
    // // TODO
    // }
    //
    // @Override
    // public void onNext(String childAuthority)
    // {
    // for(String childPermission : childPermissions)
    // {
    // permissions.put(childPermission, childAuthority);
    // }
    // }
    // });
}
