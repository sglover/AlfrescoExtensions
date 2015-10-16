/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Map;

import org.alfresco.MockAlfrescoApi;
import org.alfresco.MockContentGetter;
import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.ChecksumServiceImpl;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.checksum.dao.ChecksumDAO;
import org.alfresco.extensions.common.Content;
import org.alfresco.extensions.common.GUID;
import org.alfresco.extensions.common.Node;
import org.alfresco.services.AlfrescoApi;
import org.gytheio.messaging.MessageProducer;
import org.gytheio.messaging.MessagingException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

/**
 * 
 * @author sglover
 *
 */
// @RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration(locations = { "spring.xml" })
public class TestEdgeServer
{
    private static MongodForTestsFactory mongoFactory;

    private CacheServer edgeServer;
    // private ContentDAO contentDAO;
    // private ContentStore contentStore;
    private MockContentGetter contentGetter;

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
    }

    @AfterClass
    public static void afterClass()
    {
        mongoFactory.shutdown();
    }

    @Before
    public void before() throws Exception
    {
        final Mongo mongo = mongoFactory.newMongo();
        DB db = mongoFactory.newDB(mongo);

        MessageProducer messageProducer = new MessageProducer()
        {
            @Override
            public void send(Object arg0, String arg1, Map<String, Object> arg2)
                    throws MessagingException
            {
            }

            @Override
            public void send(Object arg0, String arg1)
                    throws MessagingException
            {
            }

            @Override
            public void send(Object arg0, Map<String, Object> arg1)
                    throws MessagingException
            {
            }

            @Override
            public void send(Object arg0) throws MessagingException
            {
            }
        };
        CacheServerIdentity cacheServerIdentity = new CacheServerIdentity()
        {

            @Override
            public int getPort()
            {
                return 0;
            }

            @Override
            public String getId()
            {
                return GUID.generate();
            }

            @Override
            public String getHostname()
            {
                return "localhost";
            }
        };
        ChecksumDAO checksumDAO = new ChecksumDAO()
        {
            @Override
            public void saveChecksums(NodeChecksums checksums)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public NodeChecksums getChecksums(String nodeId, long nodeVersion)
            {
                // TODO Auto-generated method stub
                return null;
            }
        };
        ChecksumService checksumService = new ChecksumServiceImpl(checksumDAO);
        CacheServerIdentity edgeServerIdentity = new CacheServerIdentity()
        {

            @Override
            public int getPort()
            {
                return 8080;
            }

            @Override
            public String getId()
            {
                return "";
            }

            @Override
            public String getHostname()
            {
                return "localhost";
            }
        };

        long time = System.currentTimeMillis();
        String contentCollectionName = "content" + time;
        String contentUsageCollectionName = "contentUsage" + time;
        // this.contentDAO = new MongoContentDAO(db, contentCollectionName,
        // contentUsageCollectionName, edgeServerIdentity);
        // this.contentStore = new ContentStore(ch);

        this.contentGetter = new MockContentGetter();
        AlfrescoApi alfrescoApi = new MockAlfrescoApi();

        // this.edgeServer = new CacheServer(contentDAO, contentStore,
        // contentGetter, alfrescoApi,
        // edgeServerIdentity, null);
    }

    private void compare(ByteBuffer expected, ByteBuffer actual)
    {
        Arrays.equals(expected.array(), actual.array());
    }

    @Test
    public void test1() throws Exception
    {
        long nodeInternalId = 1l;
        String nodeId = GUID.generate();
        String nodeVersion = "1";
        String nodePath = "/1/2/3";
        byte[] bytes = "test".getBytes("UTF-8");
        String expectedMimeType = "text/plain";
        Long expectedSize = new Long(bytes.length);
        InputStream nodeContent = new ByteArrayInputStream(bytes);
        ReadableByteChannel channel = null;
        try
        {
            contentGetter.addTestContent(nodeInternalId, nodeId, nodeVersion,
                    nodePath, "test", expectedMimeType);

            edgeServer.repoContentUpdated(Node.build().nodeId(nodeId)
                    .versionLabel(nodeVersion).nodePath(nodePath),
                    expectedMimeType, expectedSize, true);

            UserDetails userDetails = new User("admin", null, null);
            UserContext.setUser(userDetails);

            Content content = edgeServer.getByNodeId(nodeId, nodeVersion, true);
            channel = content.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(2048);
            channel.read(bb);
            assertNotNull(channel);
            assertEquals(expectedMimeType, content.getMimeType());
            assertEquals(expectedSize, content.getSize());

            ByteBuffer expectedNodeContent = ByteBuffer.wrap("test"
                    .getBytes("UTF-8"));

            compare(expectedNodeContent, bb);
        }
        finally
        {
            if (nodeContent != null)
            {
                nodeContent.close();
            }
            if (channel != null)
            {
                channel.close();
            }
            UserContext.setUser(null);
        }
    }
}
