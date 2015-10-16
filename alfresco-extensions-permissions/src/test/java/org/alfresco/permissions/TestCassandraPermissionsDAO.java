/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.permissions.dao.Node;
import org.alfresco.permissions.dao.cassandra.CassandraPermissionsDAO;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class TestCassandraPermissionsDAO
{
	private CassandraPermissionsDAO permissionsDAO;
//	private CCMBridge cluster;

	@Before
	public void before() throws Exception
	{
//		cluster = CCMBridge.create("permstest", 2);
//		cluster.start();
//
//		Session session = Cluster.builder().addContactPoint(CCMBridge.IP_PREFIX + "1")
//			.build()
//			.connect();
//		permissionsDAO = new CassandraPermissionsDAO(session, "permissions", true);

//		permissionsDAO = new CassandraPermissionsDAO("127.0.0.1", "permissions", true);
		permissionsDAO = new CassandraPermissionsDAO("ec2-54-74-112-226.eu-west-1.compute.amazonaws.com", "permissions", true);

		permissionsDAO.addChildPermission("Read", "ReadChildren");
		permissionsDAO.addChildPermission("Read", "ReadProperties");
		permissionsDAO.addChildPermission("Read", "ReadContent");

		{
			permissionsDAO.addChildAuthority("Root", "GROUP_1");
			Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
			List<String> childAuths = childAuthsStream.collect(Collectors.toList());
			assertTrue(childAuths.contains("GROUP_1"));
		}

		{
			permissionsDAO.addChildAuthority("GROUP_1", "GROUP_2");

			Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
			List<String> childAuths = childAuthsStream.collect(Collectors.toList());
			assertTrue(childAuths.contains("GROUP_1"));

			Stream<String> rootContainedAuthsStream = permissionsDAO.getContainedAuthoritiesAsStream("Root");
			List<String> rootContainedAuths = rootContainedAuthsStream.collect(Collectors.toList());
			assertTrue(rootContainedAuths.contains("GROUP_1"));
			assertTrue(rootContainedAuths.contains("GROUP_2"));
		}

		{
			permissionsDAO.addChildAuthority("GROUP_2", "sglover");

			Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
			List<String> childAuths = childAuthsStream.collect(Collectors.toList());
			assertTrue(childAuths.contains("GROUP_1"));

			Stream<String> rootContainedAuthsStream = permissionsDAO.getContainedAuthoritiesAsStream("Root");
			List<String> rootContainedAuths = rootContainedAuthsStream.collect(Collectors.toList());
			assertTrue(rootContainedAuths.contains("GROUP_1"));
			assertTrue(rootContainedAuths.contains("GROUP_2"));
			assertTrue(rootContainedAuths.contains("sglover"));
		}

		{
			permissionsDAO.addChildAuthority("Root", "GROUP_3");

			Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
			List<String> childAuths = childAuthsStream.collect(Collectors.toList());
			assertTrue(childAuths.contains("GROUP_1"));

			Stream<String> rootContainedAuthsStream = permissionsDAO.getContainedAuthoritiesAsStream("Root");
			List<String> rootContainedAuths = rootContainedAuthsStream.collect(Collectors.toList());
			assertTrue(rootContainedAuths.contains("GROUP_1"));
			assertTrue(rootContainedAuths.contains("GROUP_2"));
			assertTrue(rootContainedAuths.contains("sglover"));
			assertTrue(rootContainedAuths.contains("GROUP_3"));
		}

		{
			permissionsDAO.addChildAuthority("GROUP_3", "cknight");

			Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
			List<String> childAuths = childAuthsStream.collect(Collectors.toList());
			assertTrue(childAuths.contains("GROUP_1"));

			Stream<String> rootContainedAuthsStream = permissionsDAO.getContainedAuthoritiesAsStream("Root");
			List<String> rootContainedAuths = rootContainedAuthsStream.collect(Collectors.toList());
			assertTrue(rootContainedAuths.contains("GROUP_1"));
			assertTrue(rootContainedAuths.contains("GROUP_2"));
			assertTrue(rootContainedAuths.contains("sglover"));
			assertTrue(rootContainedAuths.contains("GROUP_3"));
			assertTrue(rootContainedAuths.contains("cknight"));
		}
	}

//	@Test
//	public void test1() throws Exception
//	{
//		permissionsDAO.setPermissions("parent1", "v1", "child2", "v1", PermissionsBuilder
//				.start("ReadChildren", "GROUP_1")
//				.add("ReadChildren", "sglover")
//				.get());
//
//		{
//			List<String> children = permissionsDAO.getChildren("parent1", "v1", "sglover");
//			assertTrue(children.contains("child2"));
//		}
//
//		{
//			List<String> children = permissionsDAO.getChildren("parent1", "v1", "GROUP_2");
//			assertEquals(0, children.size());
//		}
//	}

	@Test
	public void test1() throws Exception
	{
		permissionsDAO.setPermission("parent1", "v1", "Children", "Read", "GROUP_1", "child2", "v1");

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "sglover", 0, 10);
			assertTrue(children.contains(Node.withNodeId("child2")));
		}

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "GROUP_2", 0, 10);
			assertTrue(children.contains(Node.withNodeId("child2")));
		}
	}

//	@Test
	public void test2() throws Exception
	{
		permissionsDAO.setPermission("parent1", "v1", "Children", "Read", "GROUP_2", "child2", "v1");

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "GROUP_1", 0, 10);
			assertEquals(0, children.size());
		}

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "GROUP_2", 0, 10);
			assertTrue(children.contains(Node.withNodeId("child2")));
		}
	}
}
