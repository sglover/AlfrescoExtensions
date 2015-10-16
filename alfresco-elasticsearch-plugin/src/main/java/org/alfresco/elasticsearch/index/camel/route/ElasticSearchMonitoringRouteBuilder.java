/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.index.camel.route;

import org.alfresco.elasticsearch.index.ElasticSearchEventListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.messaging.jackson.ObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Route builder for registering a durable subscriber for repository node events
 * 
 * @author sglover
 */
public class ElasticSearchMonitoringRouteBuilder extends RouteBuilder
{
    private static Log logger = LogFactory.getLog(ElasticSearchMonitoringRouteBuilder.class);

    private ElasticSearchEventListener eventListener;

    public String sourceTopic = "activemq:topic:alfresco.sync.start?jmsMessageType=Text";
    private String clientId;
    private String durableSubscriptionName;
    private String txnManager;

    public ElasticSearchMonitoringRouteBuilder(ElasticSearchEventListener eventListener, String sourceTopic, String clientId,
    		String durableSubscriptionName, String txnManager)
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

        sb.append("?cacheLevelName=CACHE_CONSUMER&");
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
            logger.debug("ElasticSearch monitoring events routes config: ");
            logger.debug("sourceTopic is "+sourceTopic);
            logger.debug("targetbean is "+eventListener);
        }

        ObjectMapper messagingObjectMapper = ObjectMapperFactory.createInstance();
        DataFormat defaultDataFormat = new JacksonDataFormat(messagingObjectMapper, Object.class);

        from(getSourceTopic())
        .routeId("topic:sync.start -> bean")
//        .transacted().ref(txnManager)
        .unmarshal(defaultDataFormat)
        .bean(eventListener, "onChange")
        .end();
    }
}
