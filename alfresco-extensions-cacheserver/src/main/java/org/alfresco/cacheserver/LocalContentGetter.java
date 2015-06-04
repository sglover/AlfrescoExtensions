/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.cacheserver.contentstore.ContentStore;
import org.alfresco.cacheserver.dao.ContentDAO;
import org.alfresco.cacheserver.entity.NodeInfo;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.Content;
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
	private final ContentStore textContentStore;

	public LocalContentGetter(ContentStore contentStore, ContentStore textContentStore, ContentDAO contentDAO)
    {
		this.contentStore = contentStore;
		this.textContentStore = textContentStore;
		this.contentDAO = contentDAO;
    }

	@Override
    public Content getContent(String nodeId, String nodeVersion) throws IOException
    {
        NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion, true);
        String contentPath = nodeInfo.getContentPath();
        InputStream in = contentStore.getContent(contentPath);
        Content content = new Content(in, nodeInfo.getMimeType(), nodeInfo.getSize());
	    return content;
    }

	@Override
    public GetTextContentResponse getTextContent(long nodeId) throws AuthenticationException, IOException
    {
        NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, "text/plain");
        if(nodeInfo != null)
        {
	        String contentPath = nodeInfo.getContentPath();
	        InputStream in = textContentStore.getContent(contentPath);
	        GetTextContentResponse getTextResponse = new GetTextContentResponse(in, null, 
	        		null, null, null);
	        return getTextResponse;
        }
        else
        {
        	logger.warn("Cannot find text content for node " + nodeId);
        	return null;
        }
    }

}
