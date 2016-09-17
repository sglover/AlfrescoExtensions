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
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alfresco.permissions.Authority;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.util.concurrent.ListenableFuture;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 
 * @author sglover
 *
 */
public class CassandraPermissionsDAO1
{
    private Cluster cluster;
    private Session session;
    private String keyspace;

    public CassandraPermissionsDAO1(String host, String keyspace, boolean dropKeyspaceOnInit)
    {
        this.keyspace = keyspace;

        this.cluster = Cluster.builder().addContactPoint(host)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy())).build();

        this.session = cluster.connect();

        KeyspaceMetadata keySpaceMetadata = cluster.getMetadata().getKeyspace(keyspace);
        if (dropKeyspaceOnInit && keySpaceMetadata != null)
        {
            session.execute("DROP KEYSPACE " + keyspace + ";");
            keySpaceMetadata = null;
        }

        if (keySpaceMetadata == null)
        {
            session.execute("CREATE KEYSPACE " + keyspace + " WITH replication "
                    + "= {'class':'SimpleStrategy', 'replication_factor':3};");

            session.execute("CREATE TABLE " + keyspace + ".node_children ("
                    + "parent_node_id text, " + "parent_node_version text, " + "permission text, "
                    + "authority_start bigint," + "authority_end bigint," + "child_node_id text,"
                    + "child_node_version text, "
                    + "PRIMARY KEY(parent_node_id, parent_node_version, permission, authority, child_node_id, child_node_version) "
                    + ");");

            session.execute("CREATE TABLE " + keyspace + ".child_permissions ("
                    + "parent_permission text, " + "child_permission text,"
                    + "PRIMARY KEY(parent_permission, child_permission) " + ");");

            session.execute("CREATE TABLE " + keyspace + ".child_authorities ("
                    + "parent_authority text, " + "child_authority text,"
                    + "PRIMARY KEY(parent_authority, child_authority) " + ");");
        }

        this.insertChildPermission = session
                .prepare("insert into " + keyspace + ".child_permissions "
                        + "(parent_permission, child_permission) " + "VALUES(?, ?)");
        this.insertChildAuthority = session.prepare("insert into " + keyspace
                + ".child_authorities " + "(authority, parent_authority, rangeStart, rangeEnd) "
                + "VALUES(?, ?, ?, ?)");
        this.removeChildPermission = session.prepare("delete from " + keyspace
                + ".child_permissions " + "WHERE parent_permission = ? AND child_permission = ?");
        this.removeChildAuthority = session.prepare("delete from " + keyspace
                + ".child_authorities " + "WHERE parent_authority = ? AND child_authority = ?");
        this.getChildPermissions = session.prepare(
                "select * from " + keyspace + ".child_permissions where parent_permission = ?");
        this.getChildAuthorities = session.prepare(
                "select * from " + keyspace + ".child_authorities where parent_authority = ?");
        this.getAuthority = session
                .prepare("select * from " + keyspace + ".child_authorities where authority = ?");
        this.insertNodeChild = session.prepare("insert into " + keyspace
                + ".node_children (parent_node_id, parent_node_version, "
                + "permission, authority_start, authority_end, child_node_id, child_node_version) VALUES(?, ?, ?, ?, ?, ?, ?)");
        this.getChildren = session
                .prepare("SELECT * FROM " + keyspace + ".node_children WHERE parent_node_id=?"
                        + " AND parent_node_version=?" + " AND permission='ReadChildren'"
                        + " AND authority_start<=?" + " AND authority_end<=?;");
    }

    public void drop()
    {
        session.execute("DROP TABLE " + keyspace + ".node_children;");
        session.execute("DROP TABLE " + keyspace + ".child_authorities;");
        session.execute("DROP TABLE " + keyspace + ".child_permissions;");
    }

    // private PreparedStatement getPermissions;
    private PreparedStatement insertNodeChild;
    private PreparedStatement insertChildAuthority;
    private PreparedStatement removeChildAuthority;
    private PreparedStatement insertChildPermission;
    private PreparedStatement removeChildPermission;
    private PreparedStatement getChildPermissions;
    private PreparedStatement getChildAuthorities;
    private PreparedStatement getAuthority;
    private PreparedStatement getChildren;

    public List<String> getChildPermissions(String parentPermission, boolean includeParent)
    {
        List<String> childPermissions = new LinkedList<>();

        if (includeParent)
        {
            childPermissions.add(parentPermission);
        }

        BoundStatement boundStatement = getChildPermissions.bind(parentPermission);
        ResultSet resultSet = session.execute(boundStatement);
        for (Row row : resultSet)
        {
            childPermissions.add(row.getString("child_permission"));
        }
        // Spliterator<Row> spliterator = resultSet.spliterator();

        return childPermissions;
    }

    public Observable<String> getChildAuthorities(String parentAuthority)
    {
        BoundStatement boundStatement = getChildAuthorities.bind(parentAuthority);
        ListenableFuture<ResultSet> resultSetFuture = session.executeAsync(boundStatement);
        Observable<ResultSet> observable = Observable.from(resultSetFuture, Schedulers.io());
        Observable<Row> rowObservable = observable.flatMapIterable(result -> result);
        return rowObservable.map(row -> row.getString("child_authority"));
    }

    public Stream<String> getContainedAuthoritiesAsStream(String parentAuthority)
    {
        return Stream.concat(Stream.of(parentAuthority),
                getChildAuthoritiesAsStream(parentAuthority));
    }

    public Authority getAuthority(String authority)
    {
        final BoundStatement boundStatement = getAuthority.bind(authority);
        final ResultSet resultSet = session.execute(boundStatement);
        Row row = resultSet.one();
        String parentAuthority = row.getString("parent_authority");
        long rangeStart = row.getLong("rangeStart");
        long rangeEnd = row.getLong("rangeEnd");
        Authority auth = new Authority(parentAuthority, rangeStart, rangeEnd);
        return auth;
    }

    public Stream<String> getChildAuthoritiesAsStream(String parentAuthority)
    {
        final BoundStatement boundStatement = getChildAuthorities.bind(parentAuthority);
        final ResultSet resultSet = session.execute(boundStatement);
        final Spliterator<Row> spliterator = resultSet.spliterator();
        Stream<String> stream = StreamSupport.stream(spliterator, false)
                // .onClose(() -> resultSet.) // need to close cursor;
                .flatMap(row -> {
                    String childAuthority = row.getString("child_authority");
                    return Stream.concat(Stream.of(childAuthority),
                            getChildAuthoritiesAsStream(childAuthority));
                });
        return stream;
    }

    public void addChildPermission(String parentPermission, String childPermission)
    {
        BoundStatement insert = insertChildPermission.bind(parentPermission, childPermission);
        session.execute(insert);
    }

    public void addChildAuthority(String parentAuthority, String childAuthority, long rangeStart,
            long rangeEnd)
    {
        BoundStatement insert = insertChildAuthority.bind(parentAuthority, childAuthority,
                rangeStart, rangeEnd);
        session.execute(insert);
    }

    public void removeChildAuthority(String parentAuthority, String childAuthority)
    {
        BoundStatement insert = removeChildAuthority.bind(parentAuthority, childAuthority);
        session.execute(insert);
    }

    public void removeChildPermission(String parentPermission, String childPermission)
    {
        BoundStatement insert = removeChildPermission.bind(childPermission, childPermission);
        session.execute(insert);
    }

    public void setPermission(final String parentNodeId, final String parentNodeVersion,
            final String permission, final String authorityName, final String childNodeId,
            final String childNodeVersion)
    {
        final List<String> permissions = getChildPermissions(permission, true);
        final Authority authority = getAuthority(authorityName);

        permissions.stream().forEach(p -> {
            insertNodeChild = session.prepare("insert into " + keyspace
                    + ".node_children (parent_node_id, parent_node_version, "
                    + "permission, authority_start, authority_end, child_node_id, child_node_version) VALUES(?, ?, ?, ?, ?, ?, ?)");
            BoundStatement insert = insertNodeChild.bind(parentNodeId, parentNodeVersion, p,
                    authority.getRangeStart(), authority.getRangeEnd(), childNodeId,
                    childNodeVersion);
            session.execute(insert);
        });
    }

    public void setPermissions(String parentNodeId, String parentNodeVersion,
            Map<String, String> permissions, String childNodeId, String childNodeVersion)
    {
        permissions.forEach((permission, authority) -> {
            setPermission(parentNodeId, parentNodeVersion, permission, authority, childNodeId,
                    childNodeVersion);
        });
    }

    public Stream<String> getChildren(String parentNodeId, String parentNodeVersion,
            String authorityName)
    {
        Authority authority = getAuthority(authorityName);

        BoundStatement query = getChildren.bind(parentNodeId, parentNodeVersion,
                authority.getRangeStart(), authority.getRangeEnd());
        query.setFetchSize(24);
        ResultSet rs = session.execute(query);
        final Spliterator<Row> spliterator = rs.spliterator();
        Stream<String> childStream = StreamSupport.stream(spliterator, false)
                // .onClose(() -> resultSet.) // need to close cursor;
                .map(row -> {
                    String childNodeId = row.getString("child_node_id");
                    return childNodeId;
                });
        return childStream;
    }
}
