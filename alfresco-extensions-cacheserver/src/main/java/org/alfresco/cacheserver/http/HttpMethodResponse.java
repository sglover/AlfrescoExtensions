/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

/**
 * 
 * @author sglover
 *
 */
public class HttpMethodResponse implements HttpResponse
{
	protected CloseableHttpResponse response;

    public HttpMethodResponse(CloseableHttpResponse response) throws IOException
    {
        this.response = response;
    }
    
    public void release() throws IOException
    {
    	response.close();
    }

    public InputStream getContentAsStream() throws IOException
    {
    	return response.getEntity().getContent();
    }

    public String getContentType()
    {
        return getHeader("Content-Type");
    }

    public String getHeader(String name)
    {
        Header[] header = response.getHeaders(name);
        return (header != null && header.length > 0) ? header[0].getValue() : null;
    }

    public int getStatus()
    {
        return response.getStatusLine().getStatusCode();
    }
}
