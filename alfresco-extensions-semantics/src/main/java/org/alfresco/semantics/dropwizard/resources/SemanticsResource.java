/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.semantics.dropwizard.resources;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.alfresco.semantics.MongoSemantics;
import org.alfresco.semantics.Relation;
import org.alfresco.semantics.SemanticsProcessor;
import org.apache.log4j.Logger;

/**
 * JAX-RS / DropWizard Resource exposing the Synchronization service API over REST.
 * 
 *  @author sglover
 */
@Path("/alfresco/api/-default-/private/alfresco/versions/1")
public class SemanticsResource 
{
    private static final Logger LOGGER = Logger.getLogger(SemanticsResource.class.getName());

    private MongoSemantics semantics;
    private SemanticsProcessor semanticsProcessor;

	public SemanticsResource(MongoSemantics semantics, SemanticsProcessor semanticsProcessor)
    {
		this.semantics = semantics;
		this.semanticsProcessor = semanticsProcessor;
    }

    @Path("/process")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void process(
    		NamedEntity entity,
    		@Context final HttpServletResponse httpResponse)
    {
    	try
        {
	        semanticsProcessor.process(entity);
        }
    	catch (Exception e)
        {
            LOGGER.error("caught",e);
            throw new RuntimeException(e);
        }
    }

    @Path("/from/{fromId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Relation> relationsFrom(
    		@PathParam("fromId") String fromId,
    		@QueryParam("skip") String skipStr,
    		@QueryParam("maxItems") String maxItemsStr,
    		@QueryParam("categories") String categoriesStr,
    		@Context final HttpServletResponse httpResponse)
    {
        try
        {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("from " + fromId);

            if(fromId == null || fromId.equals(""))
            {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            else
            {
            	int skip = 0;
            	int maxItems = 0;
            	
            	if(skipStr != null)
                {
                    skip = Integer.parseInt(skipStr);
                }

            	if(maxItemsStr != null)
                {
            		maxItems = Integer.parseInt(maxItemsStr);
                }

            	Set<String> categories = new HashSet<>();
            	if(categoriesStr != null)
            	{
            		StringTokenizer st = new StringTokenizer(categoriesStr, ",");
            		while(st.hasMoreTokens())
            		{
            			String category = st.nextToken().trim();
            			categories.add(category);
            		}
            	}

            	return semantics.relationsFrom(fromId, categories, skip, maxItems);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("caught",e);
            throw e;
        }
    }
    
    @Path("/to/{toId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Relation> relationsTo(
    		@PathParam("toId") String toId,
    		@QueryParam("skip") String skipStr,
    		@QueryParam("maxItems") String maxItemsStr,
    		@QueryParam("categories") String categoriesStr,
    		@Context final HttpServletResponse httpResponse)
    {
        try
        {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("to " + toId);

            if(toId == null || toId.equals(""))
            {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            else
            {
            	int skip = 0;
            	int maxItems = 0;
            	
            	if(skipStr != null)
                {
                    skip = Integer.parseInt(skipStr);
                }

            	if(maxItemsStr != null)
                {
            		maxItems = Integer.parseInt(maxItemsStr);
                }

            	Set<String> categories = new HashSet<>();
            	if(categoriesStr != null)
            	{
            		StringTokenizer st = new StringTokenizer(categoriesStr, ",");
            		while(st.hasMoreTokens())
            		{
            			String category = st.nextToken().trim();
            			categories.add(category);
            		}
            	}

            	return semantics.relationsTo(toId, categories, skip, maxItems);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("caught",e);
            throw e;
        }
    }
}