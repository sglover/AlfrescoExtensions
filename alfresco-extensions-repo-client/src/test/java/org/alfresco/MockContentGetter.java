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
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.extensions.common.Content;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.solr.GetTextContentResponse;
import org.alfresco.services.solr.SolrApiContentStatus;
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
	
	private static class Node
	{
		private String nodeId;
		private String nodeVersion;
		public Node(String nodeId, String nodeVersion)
        {
	        super();
	        this.nodeId = nodeId;
	        this.nodeVersion = nodeVersion;
        }
		@SuppressWarnings("unused")
        public String getNodeId()
		{
			return nodeId;
		}
		@SuppressWarnings("unused")
		public String getNodeVersion()
		{
			return nodeVersion;
		}
		@Override
        public int hashCode()
        {
	        final int prime = 31;
	        int result = 1;
	        result = prime * result
	                + ((nodeId == null) ? 0 : nodeId.hashCode());
	        result = prime * result
	                + ((nodeVersion == null) ? 0 : nodeVersion.hashCode());
	        return result;
        }
		@Override
        public boolean equals(Object obj)
        {
	        if (this == obj)
		        return true;
	        if (obj == null)
		        return false;
	        if (getClass() != obj.getClass())
		        return false;
	        Node other = (Node) obj;
	        if (nodeId == null)
	        {
		        if (other.nodeId != null)
			        return false;
	        } else if (!nodeId.equals(other.nodeId))
		        return false;
	        if (nodeVersion == null)
	        {
		        if (other.nodeVersion != null)
			        return false;
	        } else if (!nodeVersion.equals(other.nodeVersion))
		        return false;
	        return true;
        }
	}

	public MockContentGetter addTestContent(long nodeInternalId, String nodeId, String nodeVersion, 
	        String nodePath, String nodeContent, String mimeType)
	{
		long size = nodeContent.getBytes().length;
		ByteArrayInputStream in = new ByteArrayInputStream(nodeContent.getBytes());
		ReadableByteChannel channel = Channels.newChannel(in);
		Content content = new Content(channel, mimeType, size);
		Node node = new Node(nodeId, nodeVersion);
		testContentByNodeId.put(node, content);
		testContentByNodeInternalId.put(nodeInternalId, content);
		testContentByNodePath.put(nodePath, content);
		return this;
	}

	public MockContentGetter addTestContent(long nodeInternalId, String nodeId, String nodeVersion,
	        String nodePath, File nodeContent, String mimeType) throws FileNotFoundException
	{
		long size = nodeContent.length();
		InputStream in = new BufferedInputStream(new FileInputStream(nodeContent));
		ReadableByteChannel channel = Channels.newChannel(in);
		Content content = new Content(channel, mimeType, size);
		Node node = new Node(nodeId, nodeVersion);
		testContentByNodeId.put(node, content);
		testContentByNodeInternalId.put(nodeInternalId, content);
		testContentByNodePath.put(nodePath, content);
		return this;
	}

	@Override
	public Content getContentByNodeId(String nodeId, String nodeVersion)
	{
		Node node = new Node(nodeId, nodeVersion);
		Content content = testContentByNodeId.get(node);
    	return content;
	}

	@Override
    public GetTextContentResponse getTextContent(long nodeInternalId)
            throws AuthenticationException, IOException
    {
		Content content = testContentByNodeInternalId.get(nodeInternalId);
		final ReadableByteChannel channel = content.getChannel();
		GetTextContentResponse textResponse = new GetTextContentResponse(channel, SolrApiContentStatus.OK,
		        null, null, 0l);
    	return textResponse;
    }

//    @Override
//    public Content getContentByNodePath(String path) throws IOException
//    {
//        Content content = testContentByNodePath.get(path);
//        return content;
//    }
}
