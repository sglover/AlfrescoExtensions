/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.messaging.camel.routes;

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
public class AlfrescoExtensionsNodeEventsRouteBuilder extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(AlfrescoExtensionsNodeEventsRouteBuilder.class);
    
    @Value("${messaging.events.repo.ext.nodes.sourceQueue.endpoint}")
    public String sourceQueue = "direct-vm:alfresco.events.repo.ext.nodes"; //defaults to an invalid notset value
    
    @Value("${messaging.events.repo.ext.nodes.targetTopic.endpoint}")
    public String targetTopic = "amqp:topic:alfresco.events.repo.ext.nodes?jmsMessageType=Text"; //defaults to an invalid notset value

    @Override
    public void configure() throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Repo node events routes config: ");
            logger.debug("SourceQueue is "+sourceQueue);
            logger.debug("targetTopic is "+targetTopic);
        }

        from(sourceQueue).routeId("alfresco.events.repo.ext.nodes -> topic:alfresco.events.repo.ext.nodes")
        .marshal("defaultDataFormat").to(targetTopic)
//        .transacted("")
        .end();
    }
}
