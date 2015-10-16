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
public class SyncEventsRouteBuilder extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(SyncEventsRouteBuilder.class);

    @Value("${messaging.startSync.sourceTopic.endpoint}")
    public String sourceTopic;

    @Value("${messaging.startSync.sourceTopic.clientId}")
    private String clientId;

    @Value("${messaging.startSync.sourceTopic.durableSubscriptionName}")
    private String durableSubscriptionName;

    @Value("${messaging.startSync.sourceTopic.txnManager}")
    private String txnManager;

    @Autowired
    @Qualifier("loggingEventListener")
    private Object eventListener;

    public SyncEventsRouteBuilder()
    {
    }

    public SyncEventsRouteBuilder(String sourceTopic, String clientId,
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
        .routeId(sourceTopic + " -> bean")
        .transacted(txnManager)
        .unmarshal(defaultDataFormat)
        .bean(eventListener, "onMessage")
        .end();
    }
}