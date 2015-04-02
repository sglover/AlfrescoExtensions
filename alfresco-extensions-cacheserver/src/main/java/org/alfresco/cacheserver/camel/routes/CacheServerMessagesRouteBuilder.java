/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.camel.routes;

import org.alfresco.cacheserver.camel.CacheServerMessagesEventListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * Route builder for registering a durable subscriber for repository node events
 * 
 * @author sglover
 */
public class CacheServerMessagesRouteBuilder extends RouteBuilder
{
    private static Log logger = LogFactory.getLog(CacheServerMessagesRouteBuilder.class);

    private CacheServerMessagesEventListener eventListener;

    private String dataFormat;
    private String sourceTopic = "activemq:topic:alfresco.events.cacheServer";
    private String clientId;
    private String durableSubscriptionName;
    private String txnManager;

    @Autowired
    public CacheServerMessagesRouteBuilder(@Qualifier("messagesEventListener") CacheServerMessagesEventListener eventListener,
    		@Value("${camel.cacheServer.events.dataFormat}") String dataFormat,
    		@Value("${camel.cacheServer.events.sourceTopic}") String sourceTopic,
    		@Value("${camel.cacheServer.events.clientId}") String clientId,
    		@Value("${camel.cacheServer.events.durableSubscriptionName}") String durableSubscriptionName,
    		@Value("${camel.cacheServer.events.txnManager}") String txnManager)
    {
    	this.dataFormat = dataFormat;
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
            logger.debug("Cache service routes config: ");
            logger.debug("sourceTopic is "+sourceTopic);
            logger.debug("targetbean is "+eventListener);
        }

        from(getSourceTopic())
        .routeId("topic:alfresco.events.cacheServer -> bean")
        .transacted().ref(txnManager)
        .unmarshal(dataFormat)
        .bean(eventListener, "onMessage")
        .end();
    }
}
