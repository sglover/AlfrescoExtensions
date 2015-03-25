/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.camel.routes;

import org.alfresco.entities.EventsListener;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${messaging.events.repo.node.sourceTopic.endpoint}")
    public String sourceTopic = "activemq:topic:alfresco.repo.events.nodes";

    @Autowired
    private EventsListener eventsListener;

    @Value("${messaging.clientId}")
    private String clientId;

    @Value("${messaging.routing.numThreads}")
    private int numThreads;

    @Value("${messaging.durableSubscriptionName}")
    private String durableSubscriptionName;

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
            logger.debug("Subscription service node events routes config: ");
            logger.debug("sourceTopic is "+sourceTopic);
            logger.debug("targetbean is "+eventsListener);
        }

//        onException(com.mongodb.MongoException.Network.class)
//        .setBody(simple(exceptionMessage().toString()))
//        .beanRef("messagingExceptionProcessor", "onReceive")
//        .end();

        from(getSourceTopic())
        .routeId("topic:alfresco.repo.events.nodes -> bean")
        .transacted()
        .unmarshal("defaultDataFormat")
        .beanRef("eventsListener", "onChange")
        .end();
    }
}
