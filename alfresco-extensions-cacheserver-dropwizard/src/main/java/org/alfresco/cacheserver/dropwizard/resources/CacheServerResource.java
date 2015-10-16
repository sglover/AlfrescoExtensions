/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dropwizard.resources;

import io.dropwizard.auth.Auth;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.alfresco.cacheserver.UserContext;
import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.contentstore.patch.PatchService;
import org.alfresco.extensions.common.Content;
import org.alfresco.services.ContentGetter;
import org.apache.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.MultiPartMediaTypes;

/**
 * JAX-RS / DropWizard Resource exposing the Synchronization service API over REST. 
 */
@Path("/alfresco/api/-default-/private/alfresco/versions/1")
public class CacheServerResource 
{
	private static final Logger LOGGER = Logger.getLogger(CacheServerResource.class.getName());

	private ContentGetter localContentGetter;
	private ChecksumService checksumService;
	private PatchService patchService;

	private ObjectMapper mapper = new ObjectMapper();

	public CacheServerResource(ContentGetter localContentGetter, ChecksumService checksumService, PatchService patchService)
    {
		this.localContentGetter = localContentGetter;
		this.checksumService = checksumService;
		this.patchService = patchService;
    }

	//MULTIPART_MIXED_TYPE
//    @Path("/checksums/{path:.*}")
//    @GET
//    @Produces("multipart/mixed")
//    public Response a()
//    {
//
//    	cacheServer.getChecksums(nodeId, nodeVersion);
//
//
//    }

//    @Path("/contentByPath/{path:.*}")
//    @GET
//    public Response contentByPath(
//    		@PathParam("path") String nodePath,
//            @Auth UserDetails user,
//    		@Context final HttpServletResponse httpResponse)
//    {
//        try
//        {
//            if (LOGGER.isDebugEnabled()) LOGGER.debug("nodePath = " + nodePath);
//
//            if(nodePath == null)
//            {
//                throw new WebApplicationException(Response.Status.BAD_REQUEST);
//            }
//            else
//            {
//                UserContext.setUser(user);
//                try
//                {
//	            	Content content = localContentGetter.getContentByNodePath(nodePath);
//	            	if(content != null)
//	            	{
//		            	InputStream in = content.getIn();
//		            	String mimeType = content.getMimeType();
//		                return Response.ok(in).type(mimeType).build();
//	            	}
//	            	else
//	            	{
//		                return Response.noContent().build();
//	            	}
//                }
//                finally
//                {
//                	UserContext.setUser(null);
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            LOGGER.error("caught",e);
//            return Response.serverError().build();
//        }
//    }

	private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException
	{
	    final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
	    while (src.read(buffer) != -1) {
	        // prepare the buffer to be drained
	        buffer.flip();
	        // write to the channel, may block
	        dest.write(buffer);
	        // If partial transfer, shift remainder down
	        // If buffer is empty, same as doing clear()
	        buffer.compact();
	    }
	    // EOF will leave buffer in fill state
	    buffer.flip();
	    // make sure the buffer is fully drained.
	    while (buffer.hasRemaining()) {
	        dest.write(buffer);
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
	            	Content content = localContentGetter.getContentByNodeId(nodeId, nodeVersion);
	            	if(content != null)
	            	{
	            	    ReadableByteChannel inputChannel = content.getChannel();
		            	String mimeType = content.getMimeType();

		            	StreamingOutput streamer = new StreamingOutput()
		            	{
		                    @Override
		                    public void write(final OutputStream output) throws IOException, WebApplicationException
		                    {
		                        final WritableByteChannel outputChannel = Channels.newChannel(output);
	                            try
	                            {
	                                fastChannelCopy(inputChannel, outputChannel);
//		                            inputChannel.read(outputChannel);
		                        }
	                            finally
		                        {
		                            // closing the channels
		                            inputChannel.close();
		                            outputChannel.close();
		                        }
		                    }
		                };
		                return Response.ok(streamer).type(mimeType).build();
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

    @Path("/patch/{nodeId}/{nodeInternalVersion}")
    @GET
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response getPatch(@PathParam("nodeId") String nodeId,
		@PathParam("nodeInternalVersion") String nodeInternalVersionStr,
        @Auth UserDetails user,
		@Context final HttpServletResponse httpResponse)
	{
	    try
	    {
	        if (LOGGER.isDebugEnabled()) LOGGER.debug("nodeId = " + nodeId
	        		+ ", nodeInternalId = " + nodeInternalVersionStr);
	
	        if(nodeId == null || nodeInternalVersionStr == null)
	        {
	            throw new WebApplicationException(Response.Status.BAD_REQUEST);
	        }
	        else
	        {
	        	long nodeInternalVersion = Long.valueOf(nodeInternalVersionStr);
	            UserContext.setUser(user);
	            try
	            {
	            	MultiPart entity = patchService.getPatchEntity(nodeId, nodeInternalVersion);
	            	if(entity != null)
	            	{
		                return Response.ok(entity, MultiPartMediaTypes.MULTIPART_MIXED_TYPE).build();
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
    		@PathParam("nodeVersion") Long nodeVersion,
            @Auth UserDetails user,
    		@Context final HttpServletResponse httpResponse)
    {
        try
        {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("nodeId = " + nodeId+ ", nodeVersion = " + nodeVersion);

            if(nodeId == null || nodeVersion == null)
            {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            else
            {
                UserContext.setUser(user);
                try
                {
	            	NodeChecksums checksums = checksumService.getChecksums(nodeId, nodeVersion);
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