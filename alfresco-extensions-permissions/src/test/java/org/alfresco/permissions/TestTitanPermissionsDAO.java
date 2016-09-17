/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import static org.alfresco.permissions.Timer.time;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.events.types.ActivityEvent;
import org.alfresco.permissions.Timer.Task;
import org.alfresco.permissions.dao.Event;
import org.alfresco.permissions.dao.Node;
import org.alfresco.permissions.dao.titan.TitanPermissionsDAO;
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

    @Before
    public void before() throws Exception
    {
        permissionsDAO = new TitanPermissionsDAO(true, null);

        time(new Task<Void>()
        {
            @Override
            public String message()
            {
                return "Add " + 10*10*10 + " auths";
            }

            @Override
            public Void execute()
            {
                permissionsDAO.addChildAuthority("Root", "GROUP_100");
                for(int i = 0; i < 10; i++)
                {
                    permissionsDAO.addChildAuthority("GROUP_100", "GROUP_100_" + i);

                    for(int j = 0; j < 10; j++)
                    {
                        permissionsDAO.addChildAuthority("GROUP_100_" + i, "GROUP_100_" + i + "_" + j);

                        for(int k = 0; k < 10; k++)
                        {
                            permissionsDAO.addChildAuthority("GROUP_100_" + i + "_" + j,
                                    "GROUP_100_" + i + "_" + j + "_" + k);
                        }
                    }
                }

                permissionsDAO.addChildAuthority("GROUP_100_0_1_2", "sglover");

                return null;
            }
        });

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

            Stream<String> rootContainedAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
            List<String> rootContainedAuths = rootContainedAuthsStream.collect(Collectors.toList());
            assertTrue(rootContainedAuths.contains("GROUP_1"));
            assertTrue(rootContainedAuths.contains("GROUP_2"));
        }

        {
            permissionsDAO.addChildAuthority("GROUP_2", "sglover");

            Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
            List<String> childAuths = childAuthsStream.collect(Collectors.toList());
            assertTrue(childAuths.contains("GROUP_1"));

            Stream<String> rootContainedAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
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

            Stream<String> rootContainedAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
            List<String> rootContainedAuths = rootContainedAuthsStream.collect(Collectors.toList());
            assertTrue(rootContainedAuths.contains("GROUP_1"));
            assertTrue(rootContainedAuths.contains("GROUP_2"));
            assertTrue(rootContainedAuths.contains("sglover"));
            assertTrue(rootContainedAuths.contains("GROUP_3"));
        }

        assertTrue(permissionsDAO.hasAuthority("GROUP_1", "sglover"));

        {
            permissionsDAO.addChildAuthority("GROUP_3", "cknight");

            Stream<String> childAuthsStream = permissionsDAO.getChildAuthoritiesAsStream("Root");
            List<String> childAuths = childAuthsStream.collect(Collectors.toList());
            assertTrue(childAuths.contains("GROUP_1"));

            Stream<String> rootContainedAuthsStream = permissionsDAO
                    .getContainedAuthoritiesAsStream("Root");
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
        permissionsDAO.close();
        // EmbeddedCassandraServerHelper.startEmbeddedCassandra(yamlFile,
        // directory + "/cassandra", 5000);
    }

    @Test
    public void test1() throws Exception
    {
//        permissionsDAO.addAssoc("parent1", 1, "child", "Read", "GROUP_1", "child2", 1);
        permissionsDAO.addNode("parent1", 1, Auths.start("ReadChildren", "GROUP_1"), Properties.empty());
        permissionsDAO.addNode("child2", 1, Auths.start("ReadChildren", "GROUP_1"), Properties.empty());
        permissionsDAO.addAssoc("parent1", 1, "child", "child2", 1);

        {
            List<Node> children = permissionsDAO.getChildren("parent1", 1, "sglover", 0, 10);
            assertTrue(children.contains(Node.withNodeId("child2").withNodeVersion(1)));
        }

        {
            List<Node> children = permissionsDAO.getChildren("parent1", 1, "GROUP_2", 0, 10);
            assertTrue(children.contains(Node.withNodeId("child2").withNodeVersion(1)));
        }
    }

    @Test
    public void test3() throws Exception
    {
        permissionsDAO.addNode("1", 1, Auths.start("ReadChildren", "GROUP_1"), Properties.empty());
        permissionsDAO.addNode("2", 1, Auths.start("ReadChildren", "GROUP_1"), Properties.empty());
        permissionsDAO.addAssoc("1", 1, "child", "2", 1);

        {
            List<Node> children = permissionsDAO.getChildren("1", 1, "sglover", 0, 10);
            assertTrue(children.contains(Node.withNodeId("2").withNodeVersion(1)));
        }

    }

    @Test
    public void testRead1PercentChildren() throws Exception
    {
        time(new Task<Void>()
        {
            int k = 0;

            @Override
            public String message()
            {
                return "Add 1000 nodes " + k;
            }

            @Override
            public Void execute()
            {
                permissionsDAO.addNode("1", 1, Auths
                        .start("ReadChildren", "GROUP_100").add("ReadProperties", "GROUP_100"),
                        Properties.empty());
                Auths auths1 = Auths
                    .start("ReadChildren", "GROUP_3").add("Read", "GROUP_1");
                Auths auths2 = Auths
                        .start("ReadChildren", "GROUP_3").add("Read", "GROUP_3");
                for(int i = 0; i < 1000; i++)
                {
                    int j = i + 2;
                    if(i % 100 == 0)
                    {
                        k++;
                        // sglover can read only 1% of child nodes
                        permissionsDAO.addNode(String.valueOf(j), 1, auths1, Properties.empty());
                    }
                    else
                    {
                        permissionsDAO.addNode(String.valueOf(j), 1, auths2, Properties.empty());
                    }
                    permissionsDAO.addAssoc("1", 1, "child", String.valueOf(j), 1);
                }

                return null;
            }
        });

        assertFalse(permissionsDAO.hasAuthority("GROUP_3", "sglover"));

        List<Node> children = time(new Task<List<Node>>()
        {
            @Override
            public String message()
            {
                return "Get 1000 (with 1% read permission) children";
            }

            @Override
            public List<Node> execute()
            {
                List<Node> children = permissionsDAO.getChildren("1", 1, "sglover", 0, 100);
                return children;
            }
        });
        assertEquals(10, children.size());
        assertTrue(children.contains(Node.withNodeId("2").withNodeVersion(1)));

        for(int i = 0; i < 5; i++)
        {
            children = time(new Task<List<Node>>()
            {
                @Override
                public String message()
                {
                    return "Get 1000 (with 1% read permission) children again";
                }
    
                @Override
                public List<Node> execute()
                {
                    List<Node> children = permissionsDAO.getChildren("1", 1, "sglover", 0, 100);
                    return children;
                }
            });
        }
    }

    @Test
    public void test5() throws Exception
    {
        time(new Task<Void>()
        {
            @Override
            public String message()
            {
                return "Add 100 events";
            }

            @Override
            public Void execute()
            {
                permissionsDAO.addNode("1", 1, Auths
                        .start("ReadChildren", "GROUP_100")
                        .add("ReadProperties", "GROUP_100")
                        .add("ReadEvents", "GROUP_100"),
                        Properties.empty());
                for(int i = 0; i < 100; i++)
                {
                    ActivityEvent event = new ActivityEvent("file-liked", "sglover", "", "",
                            "1", "", "cm:content", null, "", "", 0l, "");
                    permissionsDAO.addEvent("1", 1, event);
                }

                return null;
            }
        });

        List<Event> events = time(new Task<List<Event>>()
        {
            @Override
            public String message()
            {
                return "Get 100 events";
            }

            @Override
            public List<Event> execute()
            {
                List<Event> events = permissionsDAO.getEvents("1", 1, 0, 10, "sglover")
                        .collect(Collectors.toList());
                return events;
            }
        });
        assertEquals(10, events.size());
    }
}
