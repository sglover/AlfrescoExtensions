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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Route builder for Repo node events
 * 
 * @author sglover
 */
@Component
public class DLQEventsRouteBuilder extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(DLQEventsRouteBuilder.class);

    public String sourceQueue;

    @Autowired
    @Qualifier("countingEventListener")
    private Object eventListener;

    public DLQEventsRouteBuilder()
    {
    }

    public DLQEventsRouteBuilder(String sourceQueue, Object eventListener)
    {
    	this.eventListener = eventListener;
    	this.sourceQueue = sourceQueue;
    }

    private String getSourceQueue()
    {
        StringBuilder sb = new StringBuilder(sourceQueue);
        return sb.toString();
    }

    @Override
    public void configure()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Subscription service node events routes config: ");
            logger.debug("sourceQueue is "+sourceQueue);
            logger.debug("targetbean is "+eventListener);
        }

        ObjectMapper messagingObjectMapper = ObjectMapperFactory.createInstance();
        DataFormat defaultDataFormat = new JacksonDataFormat(messagingObjectMapper, Object.class);

        from(getSourceQueue())
        .routeId("queue:alfresco.repo.events.nodes -> bean")
//        .transacted().ref(txnManager)
        .unmarshal(defaultDataFormat)
        .bean(eventListener, "onMessage")
        .end();
    }
}