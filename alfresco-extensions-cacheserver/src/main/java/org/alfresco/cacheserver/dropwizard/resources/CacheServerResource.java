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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.alfresco.cacheserver.CacheServer;
import org.alfresco.cacheserver.UserContext;
import org.alfresco.cacheserver.checksum.ChecksumService;
import org.alfresco.cacheserver.checksum.DocumentChecksums;
import org.alfresco.cacheserver.dao.ContentDAO;
import org.alfresco.cacheserver.entity.NodeInfo;
import org.alfresco.services.Content;
import org.apache.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JAX-RS / DropWizard Resource exposing the Synchronization service API over REST. 
 */
@Path("/alfresco/api/-default-/private/alfresco/versions/1")
public class CacheServerResource 
{
	private static final Logger LOGGER = Logger.getLogger(CacheServerResource.class.getName());

	private CacheServer cacheServer;
	private ContentDAO contentDAO;
	private ChecksumService checksumService;

	private ObjectMapper mapper = new ObjectMapper();

	public CacheServerResource(CacheServer cacheServer, ChecksumService checksumService)
    {
		this.cacheServer = cacheServer;
		this.checksumService = checksumService;
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
                UserContext.setUser(user);
                try
                {
	            	Content content = cacheServer.getByNodePath(nodePath);
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
                finally
                {
                	UserContext.setUser(null);
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
                UserContext.setUser(user);
                try
                {
	            	Content content = cacheServer.getByNodeId(nodeId, nodeVersion);
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
                finally
                {
                	UserContext.setUser(null);
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("caught",e);
            return Response.serverError().build();
        }
    }

    @Path("/checksums/{nodeId}/{nodeVersion}")
    @GET
    public Response checksums(
    		@PathParam("nodeId") String nodeId,
    		@PathParam("versionLabel") String versionLabel,
            @Auth UserDetails user,
    		@Context final HttpServletResponse httpResponse)
    {
        try
        {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("nodeId = " + nodeId+ ", version label = " + versionLabel);

            if(nodeId == null || versionLabel == null)
            {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            else
            {
                UserContext.setUser(user);
                try
                {
	            	NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, versionLabel, true);
	            	DocumentChecksums checksums = checksumService.getChecksums(nodeInfo.getContentPath());
	            	if(checksums != null)
	            	{
	            		String json = mapper.writeValueAsString(checksums);
		                return Response.ok(json).type(MediaType.APPLICATION_JSON).build();
	            	}
	            	else
	            	{
		                return Response.noContent().build();
	            	}
                }
                finally
                {
                	UserContext.setUser(null);
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