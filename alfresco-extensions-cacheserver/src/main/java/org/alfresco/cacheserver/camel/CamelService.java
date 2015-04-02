/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ElasticSearch component to initialise interaction with Camel/ActiveMQ.
 * 
 * @author sglover
 *
 */
public class CamelService
{
	private String brokerUrl = "tcp://localhost:61616";
	private String sourceTopic = "activemq:topic:alfresco.events.repo.nodes";
	private String clientId = "alfresco.cacheserver";
	private String durableSubscriptionName = "alfresco.cacheserver";

	private CacheServerNodeEventListener eventListener;

	private CamelContext camelContext;

	public CamelService(String brokerUrl, String sourceTopic, String clientId, String durableSubscriptionName,
			CacheServerNodeEventListener eventListener) throws Exception
    {
        super();

        this.brokerUrl = brokerUrl;
        this.sourceTopic = sourceTopic;
    	this.clientId = clientId;
    	this.durableSubscriptionName = durableSubscriptionName;
        this.eventListener = eventListener;

        buildCamelContext();
    }

	public void shutdown() throws Exception
	{
		if(camelContext != null)
		{
			camelContext.stop();
		}
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

//		RoutesBuilder routesBuilder = new CacheServerEventsRouteBuilder(eventListener, sourceTopic,
//				clientId, durableSubscriptionName, "PROPAGATION_REQUIRED");
//		camelContext.addRoutes(routesBuilder);
//
//		camelContext.start();
	}
}
