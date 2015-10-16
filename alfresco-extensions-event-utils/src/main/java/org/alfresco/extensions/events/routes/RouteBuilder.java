/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.events.routes;

import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Route builder for Repo node events
 * 
 * @author sglover
 */
@Component
public class RouteBuilder extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(RouteBuilder.class);

//    @Value("${messaging.events.repo.node.sourceTopic.endpoint}")
    public String sourceTopic = "activemq:topic:alfresco.repo.events.nodes";

//    private EventListener eventListener;

    @Value("${messaging.clientId}")
    private String clientId = "test1";

//    @Value("${messaging.routing.numThreads}")
    private int numThreads = 1;

    @Value("${messaging.durableSubscriptionName}")
    private String durableSubscriptionName = "test1";

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
            logger.debug("Node events routes config: ");
            logger.debug("sourceTopic is "+sourceTopic);
//            logger.debug("targetbean is "+eventListener);
        }

        from(getSourceTopic())
        .routeId("topic:alfresco.repo.events.nodes -> bean")
        .transacted()
        .unmarshal("defaultDataFormat")
        .beanRef("eventListener", "onChange")
        .end();
    }
}
