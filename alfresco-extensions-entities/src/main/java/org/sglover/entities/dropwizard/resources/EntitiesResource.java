/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dropwizard.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.EntitiesService;
import org.sglover.nlp.Entity;

/**
 * JAX-RS / DropWizard Resource exposing the Synchronization service API over REST. 
 */
@Path("/alfresco/api/-default-/private/alfresco/versions/1")
public class EntitiesResource 
{
    private static final Logger LOGGER = Logger.getLogger(EntitiesResource.class.getName());

    private EntitiesService entitiesService;

	public EntitiesResource(EntitiesService entitiesService)
    {
		this.entitiesService = entitiesService;
    }

    @Path("/entities/{nodeId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> names(
    		@PathParam("nodeId") String nodeId,
    		@PathParam("nodeVersion") String nodeVersion,
    		@Context final HttpServletResponse httpResponse)
    {
    	Map<String, Long> ret = new HashMap<>();

        try
        {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("entities " + nodeId + "." + nodeVersion);

            if((nodeId == null || nodeId.equals("")) && (nodeVersion== null || nodeVersion.equals("")))
            {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            else
            {
            	Node node = new Node(nodeId, nodeVersion);
            	Collection<Entity<String>> names = entitiesService.getNames(node);
            	for(Entity<String> name : names)
            	{
            		ret.put(name.getEntity(), name.getCount());
            	}
            }

            return ret;
        }
        catch (Exception e)
        {
            LOGGER.error("caught",e);
            throw e;
        }
    }
}