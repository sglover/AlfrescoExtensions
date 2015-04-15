/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.service;

import org.alfresco.camel.CamelComponent;
import org.alfresco.elasticsearch.index.ElasticSearchEventListener;
import org.alfresco.elasticsearch.index.camel.route.ElasticSearchEventsRouteBuilder;
import org.apache.camel.RoutesBuilder;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

/**
 * ElasticSearch component to initialise interaction with Camel/ActiveMQ.
 * 
 * @author sglover
 *
 */
public class CamelService extends AbstractLifecycleComponent<CamelService>
{
	private CamelComponent camel;
	private String brokerUrl;
	private String sourceTopic;
	private String clientId;
	private String durableSubscriptionName;

	private ElasticSearchEventListener eventListener;

	@Inject public CamelService(Settings settings, ElasticSearchEventListener eventListener)
    {
        super(settings);

        this.brokerUrl = settings.get("broker.url", "tcp://localhost:61616");
        this.camel = new CamelComponent(brokerUrl);

        this.sourceTopic = settings.get("broker.sourceTopic", "activemq:topic:alfresco.events.repo.nodes");
    	this.clientId = settings.get("broker.clientId", "alfresco.elasticsearch.plugin");
    	this.durableSubscriptionName = settings.get("brokerUrl.durableSubscriptionName", "alfresco.elasticsearch.plugin");

        this.eventListener = eventListener;
    }

	private void buildCamelContext() throws Exception
	{
		RoutesBuilder routesBuilder = new ElasticSearchEventsRouteBuilder(eventListener, sourceTopic,
				clientId, durableSubscriptionName, "PROPAGATION_REQUIRED");

		camel
			.buildCamelContext()
			.addRoutes(routesBuilder)
			.start();
	}

	@Override
    protected void doStart() throws ElasticsearchException
    {
		try
		{
			buildCamelContext();
		}
		catch(Exception e)
		{
			throw new ElasticsearchException("", e);
		}
    }

	@Override
    protected void doStop() throws ElasticsearchException
    {
		if(camel != null)
		{
			try
			{
				camel.stop();
			}
			catch(Exception e)
			{
				throw new ElasticsearchException("", e);
			}
		}
    }

	@Override
    protected void doClose() throws ElasticsearchException
    {
		if(camel != null)
		{
			try
			{
				camel.stop();
			}
			catch(Exception e)
			{
				throw new ElasticsearchException("", e);
			}
		}
    }
}
