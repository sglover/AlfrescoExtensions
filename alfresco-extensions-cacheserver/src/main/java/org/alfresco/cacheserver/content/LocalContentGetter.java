/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.content;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.contentstore.ContentReader;
import org.alfresco.contentstore.ContentStore;
import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.Node;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.solr.GetTextContentResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class LocalContentGetter implements ContentGetter
{
    private static Log logger = LogFactory.getLog(LocalContentGetter.class);

    private final ContentDAO contentDAO;
    private final ContentStore contentStore;

    public LocalContentGetter(ContentStore contentStore, ContentDAO contentDAO)
    {
        this.contentStore = contentStore;
        this.contentDAO = contentDAO;
    }

    // private Content getContent(NodeInfo nodeInfo) throws IOException
    // {
    // NodeInfo nodeInfo = contentStore.getByNodeId(nodeId, nodeVersion, true);
    // if(nodeInfo != null)
    // {
    // content = getContent(nodeInfo);
    // }
    //
    // String nodeId = nodeInfo.getNode().getNodeId();
    // String nodeVersion = nodeInfo.getNode().getVersionLabel();
    // String contentPath = nodeInfo.getContentPath();
    //
    // SeekableByteChannel channel = contentStore.getContent(contentPath);
    // UserDetails userDetails = UserContext.getUser();
    // String username = userDetails.getUsername();
    // NodeUsage nodeUsage = new NodeUsage(nodeId, nodeVersion,
    // System.currentTimeMillis(), username);
    // contentDAO.addUsage(nodeUsage);
    //
    // String mimeType = nodeInfo.getMimeType();
    // Long size = nodeInfo.getSize();
    //
    // Content content = new Content(channel, mimeType, size);
    // return content;
    // }

    @Override
    public ContentReader getContentByNodeId(String nodeId, Long nodeVersion)
            throws IOException
    {
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
        ContentReader content = contentStore.getReader(node/* , true */);
        return content;
    }

    // @Override
    // public Content getContentByNodePath(String nodePath) throws IOException
    // {
    // Content content = null;
    //
    // NodeInfo nodeInfo = contentDAO.getByNodePath(nodePath);
    // if(nodeInfo != null)
    // {
    // content = getContent(nodeInfo);
    // }
    //
    // return content;
    // }

    @Override
    public GetTextContentResponse getTextContent(String nodeId, long nodeVersion)
            throws AuthenticationException, IOException
    {
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
        ContentReader content = contentStore.getReader(node, MimeType.TEXT);
        if (content != null)
        {
            ReadableByteChannel channel = content.getChannel();
            GetTextContentResponse getTextResponse = new GetTextContentResponse(
                    channel, null, null, null, null);
            return getTextResponse;
        }
        else
        {
            logger.warn("Cannot find text content for node " + nodeId);
            return null;
        }
    }

}
