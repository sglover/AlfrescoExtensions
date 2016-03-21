/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.extensions.common.Node;
import org.alfresco.services.Content;
import org.alfresco.services.ContentGetter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class MockContentGetter implements ContentGetter
{
    private static final Log logger = LogFactory.getLog(MockContentGetter.class);

    private Map<Node, Content> testContentByNodeId = new HashMap<>();
    private Map<String, Content> testContentByNodePath = new HashMap<>();
    private Map<Long, Content> testContentByNodeInternalId = new HashMap<>();

    public static MockContentGetter start()
    {
        return new MockContentGetter();
    }

    public MockContentGetter()
    {
    }

    public MockContentGetter addTestContent(long nodeInternalId, String nodeId,
            Long nodeVersion, String nodePath, String nodeContent,
            String mimeType)
    {
        long size = nodeContent.getBytes().length;
        ByteArrayInputStream in = new ByteArrayInputStream(
                nodeContent.getBytes());
        ReadableByteChannel channel = Channels.newChannel(in);

        Node node = Node.build().nodeId(nodeId).nodeInternalId(nodeInternalId).nodeVersion(nodeVersion);
        Content content = new Content(channel, size);
        testContentByNodeId.put(node, content);
        testContentByNodeInternalId.put(nodeInternalId, content);
        testContentByNodePath.put(nodePath, content);
        return this;
    }

    public MockContentGetter addTestContent(long nodeInternalId, String nodeId,
            Long nodeVersion, String nodePath, File nodeContent,
            String mimeType) throws FileNotFoundException
    {
        long size = nodeContent.length();
        InputStream in = new BufferedInputStream(new FileInputStream(
                nodeContent));
        ReadableByteChannel channel = Channels.newChannel(in);
        Node node = Node.build().nodeId(nodeId).nodeInternalId(nodeInternalId).nodeVersion(nodeVersion);
        Content content = new Content(channel, size);
        testContentByNodeId.put(node, content);
        testContentByNodeInternalId.put(nodeInternalId, content);
        testContentByNodePath.put(nodePath, content);
        return this;
    }

    @Override
    public Content getContentByNodeId(String nodeId, Long nodeVersion)
    {
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
        Content content = testContentByNodeId.get(node);
        return content;
    }

//    @Override
//    public GetTextContentResponse getTextContent(String nodeId, long nodeVersion)
//            throws AuthenticationException, IOException
//    {
//        ContentReader content = testContentByNodeInternalId.get(nodeId + "." + nodeVersion);
//        final ReadableByteChannel channel = content.getChannel();
//        GetTextContentResponse textResponse = new GetTextContentResponse(
//                channel, SolrApiContentStatus.OK, null, null, 0l);
//        return textResponse;
//    }

    // @Override
    // public Content getContentByNodePath(String path) throws IOException
    // {
    // Content content = testContentByNodePath.get(path);
    // return content;
    // }
}
