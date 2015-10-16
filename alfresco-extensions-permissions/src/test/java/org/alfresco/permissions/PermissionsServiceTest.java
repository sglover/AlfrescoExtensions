/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class PermissionsServiceTest
{
	private PermissionsServiceImpl permissionsService;

	@Before
	public void before() throws Exception
	{
		this.permissionsService = new PermissionsServiceImpl();
	}

	private long startRange(long groupId)
	{
		return groupId / 1000 * 1000;
	}

	@Test
	public void test1() throws Exception
	{
		long site1GroupId = permissionsService.addGroup("Group_site1");
		long site1ManagerGroupId = permissionsService.addGroup("Group_site1_manager");
		assertEquals(site1ManagerGroupId, permissionsService.getGroupId("Group_site1_manager"));

		long expectedStart = startRange(site1GroupId);
		long expectedEnd = expectedStart + 999;
		List<Range> expectedRanges = Arrays.asList(
				Range.start(expectedStart).end(expectedEnd)); 
		List<Range> ranges = permissionsService.calculateNodeAclGroupRanges(Acl
				.acl()
				.addAce(Ace
						.setGroup("Group_site1")
						.setPermission("Read")));
		assertEquals(expectedRanges, ranges);
	}
}
