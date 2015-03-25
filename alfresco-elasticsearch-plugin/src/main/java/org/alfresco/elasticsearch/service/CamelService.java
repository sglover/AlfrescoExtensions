/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.service;

import org.alfresco.elasticsearch.index.ElasticSearchEventListener;
import org.alfresco.elasticsearch.index.camel.route.ElasticSearchEventsRouteBuilder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ElasticSearch component to initialise interaction with Camel/ActiveMQ.
 * 
 * @author sglover
 *
 */
public class CamelService extends AbstractLifecycleComponent<CamelService>
{
	private String brokerUrl;
	private String sourceTopic;
	private String clientId;
	private String durableSubscriptionName;

	private ElasticSearchEventListener eventListener;

	private CamelContext camelContext;

	@Inject public CamelService(Settings settings, ElasticSearchEventListener eventListener)
    {
        super(settings);

        this.brokerUrl = settings.get("broker.url", "tcp://localhost:61616");
        this.sourceTopic = settings.get("broker.sourceTopic", "activemq:topic:alfresco.events.repo.nodes");
    	this.clientId = settings.get("broker.clientId", "alfresco.elasticsearch.plugin");
    	this.durableSubscriptionName = settings.get("brokerUrl.durableSubscriptionName", "alfresco.elasticsearch.plugin");

        this.eventListener = eventListener;
    }

	private void buildCamelContext() throws Exception
	{
		ActiveMQConnectionFactory mqFactory = new ActiveMQConnectionFactory(brokerUrl);

		PooledConnectionFactory mqConnPool = new PooledConnectionFactory();
		mqConnPool.setConnectionFactory(mqFactory);
		mqConnPool.setMaxConnections(5);

		PlatformTransactionManager txnManager = new JmsTransactionManager(mqConnPool);

		SpringTransactionPolicy camelRequiredTxn = new SpringTransactionPolicy(txnManager);
		camelRequiredTxn.setPropagationBehaviorName("PROPAGATION_REQUIRED");

		SimpleRegistry registry = new SimpleRegistry(); 
		registry.put("PROPAGATION_REQUIRED", camelRequiredTxn); 

		CamelContext camelContext = new DefaultCamelContext(registry);

		ActiveMQComponent component = new ActiveMQComponent();
//		AMQPComponent component = new AMQPComponent();
		component.setConnectionFactory(mqConnPool);
//		component.setTransactionManager(txnManager);
//		component.setTransactionTimeout(10000);

//		camelContext.addComponent("amqp", component);
		camelContext.addComponent("activemq", component);

		RoutesBuilder routesBuilder = new ElasticSearchEventsRouteBuilder(eventListener, sourceTopic,
				clientId, durableSubscriptionName, "PROPAGATION_REQUIRED");
		camelContext.addRoutes(routesBuilder);
		camelContext.start();
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
		if(camelContext != null)
		{
			try
			{
				camelContext.stop();
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
		if(camelContext != null)
		{
			try
			{
				camelContext.stop();
			}
			catch(Exception e)
			{
				throw new ElasticsearchException("", e);
			}
		}
    }
}
