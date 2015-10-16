/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.events.dropwizard.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.alfresco.extensions.events.CountingEventListener;
import org.apache.log4j.Logger;

/**
 * JAX-RS / DropWizard Resource exposing the Synchronization service API over REST. 
 */
@Path("/alfresco/api/-default-/private/alfresco/versions/1")
public class EventsResource 
{
    private static final Logger LOGGER = Logger.getLogger(EventsResource.class.getName());

    private CountingEventListener listener;

	public EventsResource(CountingEventListener listener)
    {
		this.listener = listener;
    }

    @Path("/counts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> counts(
    		@Context final HttpServletResponse httpResponse)
    {
//        try
//        {
        	Map<String, AtomicLong> counts = listener.getCounts();
        	Map<String, Long> ret = new HashMap<String, Long>();
        	for(Map.Entry<String, AtomicLong> entry : counts.entrySet())
        	{
        		ret.put(entry.getKey(), entry.getValue().get());
        	}
        	return ret;
//        }
//        catch (Exception e)
//        {
//            LOGGER.error("caught",e);
//            throw e;
//        }
    }

//    @Path("/subscribe")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response subscribe(
//    		SubscribeRequest request, 
//    		@Context final HttpServletResponse httpResponse)
//    {
//    	String subscriberId = request.getSubscriberId();
//    	events.subscribe(subscriberId);
//    	return Response.status(Response.Status.CREATED).build();
//    }
//
//    @Path("/nodesubscribe")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
////    @Produces(MediaType.APPLICATION_JSON)
//    public Response subscribeToNode(
//    		NodeSubscribeRequest request, 
//    		@Context final HttpServletResponse httpResponse)
//    {
//    	String subscriberId = request.getSubscriberId();
//    	String subscriptionId = request.getSubscriptionId();
//    	String path = request.getPath();
//    	events.subscribeToNode(subscriberId, subscriptionId, path);
//    	return Response.status(Response.Status.CREATED).build();
//    }
}