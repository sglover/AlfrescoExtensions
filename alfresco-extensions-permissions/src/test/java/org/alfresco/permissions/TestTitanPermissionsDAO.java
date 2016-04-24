/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.permissions.dao.Node;
import org.alfresco.permissions.dao.titan.TitanPermissionsDAO;
import org.alfresco.permissions.dao.titan.TitanPermissionsDAO.RemoteCassandraConfig;
import org.alfresco.permissions.dao.titan.TitanPermissionsDAO.RemoteElasticSearchConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class TestTitanPermissionsDAO
{
	private TitanPermissionsDAO permissionsDAO;
//	private String directory;
//	private String yamlFile = "embedded-cassandra.yaml";

	@Before
	public void before() throws Exception
	{
//		this.directory = "/Users/sglover/tmp/" + System.currentTimeMillis();

//		EmbeddedCassandraServerHelper.startEmbeddedCassandra(yamlFile, directory + "/cassandra");

		permissionsDAO = new TitanPermissionsDAO(Arrays.asList(
				new RemoteCassandraConfig("localhost", true),
				new RemoteElasticSearchConfig("127.0.0.1", true)
				));

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

	@After
	public void after() throws Exception
	{
//		EmbeddedCassandraServerHelper.startEmbeddedCassandra(yamlFile, directory + "/cassandra", 5000);
	}

	@Test
	public void test1() throws Exception
	{
		permissionsDAO.setPermission("parent1", "v1", "child", "Read", "GROUP_1", "child2", "v1");

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "sglover", 0, 10);
			assertTrue(children.contains(Node.withNodeId("child2").withNodeVersion("v1")));
		}

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "GROUP_2", 0, 10);
			assertTrue(children.contains(Node.withNodeId("child2").withNodeVersion("v1")));
		}
	}

	@Test
	public void test2() throws Exception
	{
		permissionsDAO.setPropertiesPermission("parent1", "v1", "GROUP_1");

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "sglover", 0, 10);
			assertTrue(children.contains(Node.withNodeId("child2").withNodeVersion("v1")));
		}

		{
			List<Node> children = permissionsDAO.getChildren("parent1", "v1", "GROUP_2", 0, 10);
			assertTrue(children.contains(Node.withNodeId("child2").withNodeVersion("v1")));
		}
	}
}
