/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.http;

/**
 * 
 * @author sglover
 *
 */
public class GetNodeHttpRequest extends HttpRequest
{
	private String nodeId;
	private String nodeVersion;

	public GetNodeHttpRequest(String method, String hostname, int port, String username, String password,
			String nodeId, String nodeVersion)
    {
	    super(method, hostname, port, username, password);
	    this.nodeId = nodeId;
	    this.nodeVersion = nodeVersion;
    }

	@Override
	public String getUri()
	{
		String uri = "http://"
				+ hostname
				+ ":"
				+ port
				+ "/alfresco/api/-default-/private/alfresco/versions/1/contentByNodeId/"
				+ nodeId
				+ "/"
				+ nodeVersion;
		return uri;
	}
}
