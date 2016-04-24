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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.permissions.dao.Node;
import org.alfresco.permissions.dao.titan.TitanPermissionsDAO;
import org.alfresco.permissions.dao.titan.TitanPermissionsDAO.RemoteCassandraConfig;
import org.alfresco.permissions.dao.titan.TitanPermissionsDAO.RemoteElasticSearchConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 *         avg write (ms) 0.324283 total write (ms) 3243.1587 100 children in
 *         time (ms) 21.16297 100 children in time (ms) 14.405273 100 children
 *         in time (ms) 15.215483 100 children in time (ms) 13.529972 100
 *         children in time (ms) 10.233615 1 children in time (ms) 8.228028 1
 *         children in time (ms) 7.112936 1 children in time (ms) 6.339148 1
 *         children in time (ms) 6.151559 1 children in time (ms) 6.736978
 */
public class StressTestTitanPermissionsDAO
{
    private TitanPermissionsDAO permissionsDAO;

    @Before
    public void before() throws Exception
    {
        permissionsDAO = new TitanPermissionsDAO(
                Arrays.asList(new RemoteCassandraConfig("localhost", true),
                        new RemoteElasticSearchConfig("localhost", true)));
    }

    // @Test
    // public void test1() throws Exception
    // {
    // permissionsDAO.setPermissions("parent1", "v1", "child2", "v1",
    // PermissionsBuilder
    // .start("ReadChildren", "GROUP_1")
    // .add("ReadChildren", "sglover")
    // .get());
    //
    // {
    // List<String> children = permissionsDAO.getChildren("parent1", "v1",
    // "sglover");
    // assertTrue(children.contains("child2"));
    // }
    //
    // {
    // List<String> children = permissionsDAO.getChildren("parent1", "v1",
    // "GROUP_2");
    // assertEquals(0, children.size());
    // }
    // }

    private void setup1()
    {
        {
            permissionsDAO.addChildAuthority("Root", "GROUP_1");
            Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
            List<String> childAuths = childAuthsStream.collect(Collectors.toList());
            assertTrue(childAuths.contains("GROUP_1"));
        }

        {
            permissionsDAO.addChildAuthority("GROUP_1", "GROUP_2");
            Stream<String> childAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
            List<String> childAuths = childAuthsStream.collect(Collectors.toList());
            assertTrue(childAuths.contains("GROUP_1"));
            assertTrue(childAuths.contains("GROUP_2"));
        }

        {
            permissionsDAO.addChildAuthority("GROUP_2", "sglover");
            Stream<String> childAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
            List<String> childAuths = childAuthsStream.collect(Collectors.toList());
            assertTrue(childAuths.contains("GROUP_1"));
            assertTrue(childAuths.contains("GROUP_2"));
            assertTrue(childAuths.contains("sglover"));
        }

        {
            permissionsDAO.addChildAuthority("Root", "GROUP_3");
            Stream<String> childAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
            List<String> childAuths = childAuthsStream.collect(Collectors.toList());
            assertTrue(childAuths.contains("GROUP_1"));
            assertTrue(childAuths.contains("GROUP_2"));
            assertTrue(childAuths.contains("sglover"));
            assertTrue(childAuths.contains("GROUP_3"));
        }

        {
            permissionsDAO.addChildAuthority("GROUP_3", "cknight");
            Stream<String> childAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
            List<String> childAuths = childAuthsStream.collect(Collectors.toList());
            assertTrue(childAuths.contains("GROUP_1"));
            assertTrue(childAuths.contains("GROUP_2"));
            assertTrue(childAuths.contains("sglover"));
            assertTrue(childAuths.contains("GROUP_3"));
            assertTrue(childAuths.contains("cknight"));
        }
    }

    @Test
    public void test1() throws Exception
    {
        long total = 0;

        setup1();

        int numChildren = 500;

        {
            for (int i = 0; i < numChildren; i++)
            {
                if (i % 100 == 0)
                {
                    System.out.print(".");
                }
                long start = System.nanoTime();
                permissionsDAO.setPermission("parent1", "v1", "child", "Read", "GROUP_1",
                        "child" + i, "v1");
                long end = System.nanoTime();
                total += (end - start);
            }
            long start = System.nanoTime();
            permissionsDAO.setPermission("parent1", "v1", "child", "Read", "cknight",
                    "child" + (numChildren + 1), "v1");
            long end = System.nanoTime();
            total += (end - start);
        }

        System.out.println("avg write (ms) " + (total / 10001 / 1000000.0f));
        System.out.println("total write (ms) " + (total / 1000000.0f));

        for (int i = 0; i < 5; i++)
        {
            long start = System.nanoTime();
            List<Node> children = permissionsDAO.getChildren("parent1", "v1", "sglover", 0, 100);
            long end = System.nanoTime();
            assertEquals(100, children.size());
            System.out.println(
                    children.size() + " children in time (ms) " + (end - start) / 1000000.0f);
        }

        for (int i = 0; i < 5; i++)
        {
            long start = System.nanoTime();
            List<Node> children = permissionsDAO.getChildren("parent1", "v1", "cknight", 0, 10);
            long end = System.nanoTime();
            assertEquals(1, children.size());
            assertTrue(children
                    .contains(Node.withNodeId("child" + (numChildren + 1)).withNodeVersion("v1")));
            System.out.println(
                    children.size() + " children in time (ms) " + (end - start) / 1000000.0f);
        }
    }

    // @Test
    public void test2() throws Exception
    {
        // 5000 top level authorities
        // for(int i = 0; i < 500; i++)
        // {
        // System.out.println("i=" + i);
        // permissionsDAO.addChildAuthority("Root", "GROUP_" + i);
        //
        // // each has 50 children
        // for(int j = 0; j < 50; j++)
        // {
        // permissionsDAO.addChildAuthority("GROUP_" + i, "GROUP_" + i + "_" +
        // j);
        //
        // // each has 50 children
        // for(int k = 0; k < 50; k++)
        // {
        // permissionsDAO.addChildAuthority("GROUP_" + i + "_" + j, "GROUP_" + i
        // + "_" + k);
        // }
        // }
        // }

        long start = System.nanoTime();
        Stream<String> authStream = permissionsDAO.getContainedAuthoritiesAsStream("GROUP_1");
        long end = System.nanoTime();
        System.out.println((end - start) / 1000000000.0f + ", " + authStream.count());
    }
}
