/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.httpclient.Response;
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

	public MockContentGetter()
	{
	}

	public InputStream getContent(String nodeId)
	{
		InputStream is = new ByteArrayInputStream("Testing Testing".getBytes(StandardCharsets.UTF_8));;
    	return is;
	}

	@Override
    public GetTextContentResponse getTextContent(long nodeId)
            throws AuthenticationException, IOException
    {
		final InputStream is = new ByteArrayInputStream("Testing Testing".getBytes(StandardCharsets.UTF_8));;
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
