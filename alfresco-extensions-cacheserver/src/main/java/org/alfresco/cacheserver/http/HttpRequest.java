/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.http;

import java.util.Map;

/**
 * 
 * @author sglover
 *
 */
public abstract class HttpRequest
{
    protected String method;
//    private String uri;
    protected Map<String, String> args;
    protected Map<String, String> headers;
    protected byte[] body;
    protected String encoding = "UTF-8";
    protected String contentType;
    protected String hostname;
    protected int port;
    protected String username;
    protected String password;

    public HttpRequest(String method, String hostname, int port, String username, String password)
    {
        this.method = method;
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public String getMethod()
    {
        return method;
    }

    public String getContentType()
	{
		return contentType;
	}

	public String getHostname()
	{
		return hostname;
	}

	public int getPort()
	{
		return port;
	}

	public abstract String getUri();

    public String getFullUri()
    {
        // calculate full uri
    	String uri = getUri();
        String fullUri = uri == null ? "" : uri;
        if (args != null && args.size() > 0)
        {
            char prefix = (uri.indexOf('?') == -1) ? '?' : '&';
            for (Map.Entry<String, String> arg : args.entrySet())
            {
                fullUri += prefix + arg.getKey() + "=" + (arg.getValue() == null ? "" : arg.getValue());
                prefix = '&';
            }
        }
        
        return fullUri;
    }
    
    public HttpRequest setArgs(Map<String, String> args)
    {
        this.args = args;
        return this;
    }
    
    public Map<String, String> getArgs()
    {
        return args;
    }

    public HttpRequest setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }
    
    public Map<String, String> getHeaders()
    {
        return headers;
    }
    
    public HttpRequest setBody(byte[] body)
    {
        this.body = body;
        return this;
    }
    
    public byte[] getBody()
    {
        return body;
    }
    
    public HttpRequest setEncoding(String encoding)
    {
        this.encoding = encoding;
        return this;
    }
    
    public String getEncoding()
    {
        return encoding;
    }

    public HttpRequest setType(String contentType)
    {
        this.contentType = contentType;
        return this;
    }
    
    public String getType()
    {
        return contentType;
    }
}
