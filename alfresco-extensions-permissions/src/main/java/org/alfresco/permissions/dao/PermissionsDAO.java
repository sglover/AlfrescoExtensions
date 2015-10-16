/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 
 * @author sglover
 *
 */
public interface PermissionsDAO
{
	void addPermission(String permission);
	void addChildPermission(String parentPermission, String childPermission);
	List<String> getContainedPermissions(String permission);
	void addChildAuthority(String parentAuthority, String childAuthority);
	void removeChildAuthority(String parentAuthority, String childAuthority);
	void setPropertiesPermission(String nodeId, String nodeVersion, String authority);
	void setPermission(final String sourceNodeId, final String parentNodeVersion, final String assocType,
			final String permission, final String authority,
			final String targetNodeId, final String childNodeVersion);
	Stream<String> getChildAuthoritiesAsStream(String parentAuthority);
	Stream<String> getContainedAuthoritiesAsStream(String parentAuthority);
	List<Node> getChildren(String parentNodeId, String parentNodeVersion, String authority, int skip, int limit);
	Optional<Properties> getNodeProperties(String nodeId, String nodeVersion, String permission, String authority);
}
