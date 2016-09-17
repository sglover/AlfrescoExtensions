/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.alfresco.events.types.ActivityEvent;
import org.alfresco.permissions.Auths;
import org.alfresco.permissions.Properties;

/**
 * 
 * @author sglover
 *
 */
public interface PermissionsDAO
{
    boolean deleteNode(String nodeId, int nodeVersion);

    void addEvent(String nodeId, int nodeVersion, ActivityEvent event);

    void addPermission(String permission);

    void addAuthority(String authority);

    boolean hasAuthority(String parentAuthority, String childAuthority);

    void addNode(String nodeId, int nodeVersion, Auths auths, Properties properties);

    void addChildPermission(String parentPermission, String childPermission);

    long countChildAuthorities(String parentAuthority);

    List<String> getContainedPermissions(String permission);

    void addChildAuthority(String parentAuthority, String childAuthority);

    void removeChildAuthority(String parentAuthority, String childAuthority);

    void addAssoc(final String sourceNodeId, final int parentNodeVersion, final String assocType,
            final String targetNodeId, final int childNodeVersion);

    Stream<String> getChildAuthoritiesAsStream(String parentAuthority);

    Stream<String> getChildAuthoritiesAsStream(String parentAuthority, Integer skip, Integer limit);

    Stream<String> getContainedAuthoritiesAsStream(String parentAuthority);

    Stream<String> getContainedAuthoritiesAsStream(String parentAuthority, Integer skip,
            Integer limit);

    List<Node> getChildren(String parentNodeId, int parentNodeVersion, String authority, int skip,
            int limit);

    Optional<Properties> getNodeProperties(String nodeId, int nodeVersion, String permission,
            String authority);

    Stream<Event> getEvents(long minTs, Integer skip, Integer limit);

    Stream<Event> getEvents(String nodeId, int nodeVersion, Integer skip, Integer limit,
            String authority);

    void versionNode(String nodeId, Auths auths);
}
