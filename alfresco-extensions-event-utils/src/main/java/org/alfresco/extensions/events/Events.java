/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.events;

import org.alfresco.messaging.camel.routes.DLQEventsRouteBuilder;
import org.apache.camel.RoutesBuilder;
import org.sglover.camel.CamelComponent;

/**
 * 
 * @author sglover
 *
 */
public class Events
{
	private CamelComponent camel;

	public Events(EventListener eventListener) throws Exception
	{
		this("activemq:queue:ActiveMQ.DLQ", eventListener);
	}

	public Events(String sourceQueue, EventListener eventListener) throws Exception
	{
		this("tcp://172.29.102.6:61616", sourceQueue, eventListener);
	}

	public Events(String brokerUrl, String sourceQueue, EventListener eventListener) throws Exception
	{
//		String txnManager = "PROPAGATION_REQUIRED";
//		RoutesBuilder routesBuilder = new RepoNodeEventsRouteBuilder(sourceTopic, clientId, durableSubscriptionName, txnManager,
//				eventListener);
		RoutesBuilder routesBuilder = new DLQEventsRouteBuilder(sourceQueue, eventListener);

        this.camel = CamelComponent
        		.start(brokerUrl)
        		.buildCamelContext()
        		.addRoutes(routesBuilder);
        this.camel.start();
	}
}
