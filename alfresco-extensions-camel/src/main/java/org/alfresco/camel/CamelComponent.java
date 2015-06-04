/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
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
public class CamelComponent
{
	private String brokerUrl;
//	private String sourceTopic;
//	private String clientId;
//	private String durableSubscriptionName;

//	private ElasticSearchEventListener eventListener;

	private CamelContext camelContext;

	public static CamelComponent start(String brokerUrl)
	{
		return new CamelComponent(brokerUrl);
	}

	public CamelComponent(String brokerUrl/*, String sourceTopic, String clientId, String durableSubscriptionName*/)
    {
		this.brokerUrl = brokerUrl;
//		this.sourceTopic = sourceTopic;
//		this.clientId = clientId;
//		this.durableSubscriptionName = durableSubscriptionName;
//        this.eventListener = eventListener;
    }

	public CamelComponent buildCamelContext() throws Exception
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

		this.camelContext = new DefaultCamelContext(registry);

		ActiveMQComponent component = new ActiveMQComponent();
//		AMQPComponent component = new AMQPComponent();
		component.setConnectionFactory(mqConnPool);
//		component.setTransactionManager(txnManager);
//		component.setTransactionTimeout(10000);

//		camelContext.addComponent("amqp", component);
		camelContext.addComponent("activemq", component);

//		RoutesBuilder routesBuilder = new ElasticSearchEventsRouteBuilder(eventListener, sourceTopic,
//				clientId, durableSubscriptionName, "PROPAGATION_REQUIRED");
		
		return this;
	}

	public CamelComponent addRoutes(RoutesBuilder routesBuilder) throws Exception
	{
		camelContext.addRoutes(routesBuilder);
		return this;
	}

	public void start() throws Exception
    {
    	camelContext.start();
    }

    public void stop() throws Exception
    {
		if(camelContext != null)
		{
			camelContext.stop();
		}
    }
}
