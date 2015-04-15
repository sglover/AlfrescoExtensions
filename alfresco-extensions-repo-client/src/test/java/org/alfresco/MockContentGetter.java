/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.httpclient.Response;
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
public class MockContentGetter implements ContentGetter
{
    private static final Log logger = LogFactory.getLog(MockContentGetter.class);

    private Map<Node, Content> testContentByNodeId = new HashMap<>();
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

	public MockContentGetter addTestContent(long nodeInternalId, String nodeId, String nodeVersion, String nodeContent,
			String mimeType)
	{
		long size = nodeContent.getBytes().length;
		ByteArrayInputStream in = new ByteArrayInputStream(nodeContent.getBytes());
		Content content = new Content(in, mimeType, size);
		Node node = new Node(nodeId, nodeVersion);
		testContentByNodeId.put(node, content);
		testContentByNodeInternalId.put(nodeInternalId, content);
		return this;
	}

//	public MockContentGetter addTestContent(long nodeInternalId, String nodeId, String nodeVersion, InputStream nodeContent,
//			String mimeType, long size)
//	{
//		String key = nodeId + "." + nodeVersion;
//		Content content = new Content(nodeContent, mimeType, size);
//		testContentByNodeInternalId.put(nodeInternalId, content);
//		return this;
//	}

	@Override
	public Content getContent(String nodeId, String nodeVersion)
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
		final InputStream is = content.getIn();
		Response response = new Response()
		{

			@Override
            public InputStream getContentAsStream() throws IOException
            {
	            return is;
            }

			@Override
            public String getHeader(String name)
            {
	            return "";
            }

			@Override
            public String getContentType()
            {
	            return "text/plain";
            }

			@Override
            public int getStatus()
            {
	            return 0;
            }

			@Override
            public void release()
            {
            }
		};
		GetTextContentResponse textResponse = new GetTextContentResponse(response);
    	return textResponse;
    }
}
