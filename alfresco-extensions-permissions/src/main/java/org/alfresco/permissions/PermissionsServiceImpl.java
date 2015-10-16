/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 
 * @author sglover
 *
 */
public class PermissionsServiceImpl
{
	private AtomicLong maxParentGroupId = new AtomicLong(0);
	private Map<String, Long> maxGroupIdByParentGroupName = new ConcurrentHashMap<>();
	private Map<String, Long> groupsToIds = new ConcurrentHashMap<>();

	public long addGroup(String parentGroupName)
	{
		long parentGroupId = maxParentGroupId.getAndIncrement();
		groupsToIds.put(parentGroupName, parentGroupId);
		return parentGroupId;
	}
	
	public long addGroup(String parentGroupName, String childGroupName)
	{
		long childGroupId = maxGroupIdByParentGroupName.get(parentGroupName) + 1;
		maxGroupIdByParentGroupName.put(parentGroupName, childGroupId);
		groupsToIds.put(childGroupName, childGroupId);
		return childGroupId;
	}

	public long getGroupId(String group)
	{
		return groupsToIds.get(group);
	}

	public List<Range> calculateNodeAclGroupRanges(Acl acl)
	{
		List<Range> ranges = acl.getAces()
				.stream()
				.map(ace -> {
					String group = ace.getGroup();
					long groupId = getGroupId(group);
					return Range.start(groupId).end(groupId / 1000 * 1000 + 999);
				})
				.collect(Collectors.toList());
		return ranges;
	}
}
