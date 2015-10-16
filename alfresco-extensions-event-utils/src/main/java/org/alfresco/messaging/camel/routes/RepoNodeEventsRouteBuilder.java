/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.messaging.camel.routes;

import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.messaging.jackson.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Route builder for Repo node events
 * 
 * @author sglover
 */
@Component
public class RepoNodeEventsRouteBuilder extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(RepoNodeEventsRouteBuilder.class);

//    public String sourceQueue = "direct-vm:alfresco.events"; //defaults to an invalid notset value
//    
//    @Value("${messaging.events.repo.node.targetTopic.endpoint}")
//    public String targetTopic = "amqp:topic:alfresco.repo.events?jmsMessageType=Text"; //defaults to an invalid notset value

    @Value("${messaging.sourceTopic.endpoint}")
    public String sourceTopic;

    @Value("${messaging.sourceTopic.clientId}")
    private String clientId;

    @Value("${messaging.sourceTopic.durableSubscriptionName}")
    private String durableSubscriptionName;

    @Value("${messaging.sourceTopic.txnManager}")
    private String txnManager;

    @Autowired
    @Qualifier("countingEventListener")
    private Object eventListener;

    public RepoNodeEventsRouteBuilder()
    {
    }

    public RepoNodeEventsRouteBuilder(String sourceTopic, String clientId,
    		String durableSubscriptionName, String txnManager, Object eventListener)
    {
    	this.eventListener = eventListener;
    	this.sourceTopic = sourceTopic;
    	this.clientId = clientId;
    	this.durableSubscriptionName = durableSubscriptionName;
    	this.txnManager = txnManager;
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
    public void configure()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Subscription service node events routes config: ");
            logger.debug("sourceTopic is "+sourceTopic);
            logger.debug("targetbean is "+eventListener);
        }

        ObjectMapper messagingObjectMapper = ObjectMapperFactory.createInstance();
        DataFormat defaultDataFormat = new JacksonDataFormat(messagingObjectMapper, Object.class);

        from(getSourceTopic())
        .routeId("topic:alfresco.repo.events.nodes -> bean")
//        .transacted().ref(txnManager)
        .unmarshal(defaultDataFormat)
        .bean(eventListener, "onMessage")
        .end();
    }

//    @Override
//    public void configure() throws Exception
//    {
//        if (logger.isDebugEnabled())
//        {
//            logger.debug("Repo node events routes config: ");
//            logger.debug("SourceQueue is "+sourceQueue);
//            logger.debug("targetTopic is "+targetTopic);
//        }
//
//        from(sourceQueue).routeId("alfresco.events -> topic:alfresco.repo.events")
//        .marshal("defaultDataFormat").to(targetTopic)
//        .end();
//    }
}