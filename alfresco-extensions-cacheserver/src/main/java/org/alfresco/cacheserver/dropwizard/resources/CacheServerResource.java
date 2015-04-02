/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dropwizard.resources;

import io.dropwizard.auth.Auth;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.alfresco.cacheserver.CacheServer;
import org.alfresco.services.Content;
import org.apache.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JAX-RS / DropWizard Resource exposing the Synchronization service API over REST. 
 */
@Path("/alfresco/api/-default-/private/alfresco/versions/1")
public class CacheServerResource 
{
	private static final Logger LOGGER = Logger.getLogger(CacheServerResource.class.getName());

	private CacheServer cacheServer;

	public CacheServerResource(CacheServer cacheServer)
    {
		this.cacheServer = cacheServer;
    }

    @Path("/contentByPath/{path:.*}")
    @GET
    public Response contentByPath(
    		@PathParam("path") String nodePath,
            @Auth UserDetails user,
    		@Context final HttpServletResponse httpResponse)
    {
        try
        {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("nodePath = " + nodePath);

            if(nodePath == null)
            {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            else
            {
            	String username = user.getUsername();

            	Content content = cacheServer.getByNodePath(nodePath, username);
            	if(content != null)
            	{
	            	InputStream in = content.getIn();
	            	String mimeType = content.getMimeType();
	                return Response.ok(in).type(mimeType).build();
            	}
            	else
            	{
	                return Response.noContent().build();
            	}
            }
        }
        catch (Exception e)
        {
            LOGGER.error("caught",e);
            return Response.serverError().build();
        }
    }

    @Path("/contentByNodeId/{nodeId}/{nodeVersion}")
    @GET
    public Response contentByNodeId(
    		@PathParam("nodeId") String nodeId,
    		@PathParam("nodeVersion") String nodeVersion,
            @Auth UserDetails user,
    		@Context final HttpServletResponse httpResponse)
    {
        try
        {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("nodeId = " + nodeId);

            if(nodeId == null || nodeVersion == null)
            {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            else
            {
            	String username = user.getUsername();

            	Content content = cacheServer.getByNodeId(nodeId, nodeVersion, username);
            	if(content != null)
            	{
	            	InputStream in = content.getIn();
	            	String mimeType = content.getMimeType();
	                return Response.ok(in).type(mimeType).build();
            	}
            	else
            	{
	                return Response.noContent().build();
            	}
            }
        }
        catch (Exception e)
        {
            LOGGER.error("caught",e);
            return Response.serverError().build();
        }
    }
}