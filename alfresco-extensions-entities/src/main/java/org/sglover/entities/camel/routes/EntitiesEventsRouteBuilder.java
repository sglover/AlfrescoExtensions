/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.camel.routes;

import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.entities.EventsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Route builder for Repo node events
 * 
 * @author sglover
 */
@Component
public class EntitiesEventsRouteBuilder extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(EntitiesEventsRouteBuilder.class);

    private String routeId = "topic:alfresco.repo.events.nodes -> bean";
    private String sourceTopic = "amqp:topic:alfresco.repo.events.nodes";
    private EventsListener eventsListener;
    private String clientId;
    private String dataFormat;
    private String txnManager;
    private String durableSubscriptionName;

    @Autowired
    public EntitiesEventsRouteBuilder(@Qualifier("eventsListener") EventsListener eventsListener,
    		@Value("${messaging.events.repo.node.sourceTopic.endpoint}") String sourceTopic,
    		@Value("${messaging.clientId}") String clientId,
    		@Value("${messaging.durableSubscriptionName}") String durableSubscriptionName,
    		@Value("${messaging.routing.numThreads}") int numThreads,
    		@Value("${messaging.dataFormat}") String dataFormat,
    		@Value("${messaging.txnManager}") String txnManager)
    {
    	this.eventsListener = eventsListener;
    	this.dataFormat = dataFormat;
    	this.txnManager = txnManager;
    	this.sourceTopic = sourceTopic;
    	this.clientId = clientId;
    	this.durableSubscriptionName = durableSubscriptionName;
    }

    private String getSourceTopic()
    {
        StringBuilder sb = new StringBuilder(sourceTopic);

        sb.append("?");
        sb.append("clientId=");
        sb.append(clientId);
        sb.append("&durableSubscriptionName=");
        sb.append(durableSubscriptionName);

        return sb.toString();
    }

    @Override
    public void configure() throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Entities service node events routes config: ");
            logger.debug("sourceTopic is "+sourceTopic);
            logger.debug("targetbean is "+eventsListener);
        }

        String sourceTopic = getSourceTopic();
        from(sourceTopic)
        .routeId(routeId)
        .transacted(txnManager)
        .unmarshal(dataFormat)
        .beanRef("eventsListener", "onEvent")
        .end();
    }
}
