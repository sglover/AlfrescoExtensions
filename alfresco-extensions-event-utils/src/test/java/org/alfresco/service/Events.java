/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class Events implements ExceptionListener
{
    @Test
    public void test1()
    {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            connection.setExceptionListener(this);
            
            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
//            Destination destination = session.createTopic("ActiveMQ.Advisory.Topic");
//            Destination destination = session.createTopic("ActiveMQ.Advisory.MessageDelivered.Topic.alfresco.repo.events.nodes");

            // Create a MessageConsumer from the Session to the Topic or Queue
//            MessageConsumer consumer = session.createConsumer(destination);

            ActiveMQTopic topic = new ActiveMQTopic("alfresco.repo.events.nodes");
//            ActiveMQTopic mdTopic = AdvisorySupport.getMessageConsumedAdvisoryTopic(topic);
            ActiveMQTopic mdTopic = AdvisorySupport.getSlowConsumerAdvisoryTopic(topic);
            //MessageConsumedAdvisoryTopic(topic);
            MessageConsumer consumer = session.createConsumer(mdTopic);
            // Wait for a message
            long start = System.currentTimeMillis();
            long end = System.currentTimeMillis();
            do
            {
                Message message = consumer.receive();
    //            Message message = consumer.receive(1000);
    //            message = consumer.receive(1000);
                if(message != null)
                {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        System.out.println("Received: " + text);
                    }
                    else if(message instanceof ActiveMQMessage)
                    {
                        ActiveMQMessage m = (ActiveMQMessage)message;
                        System.out.println("Received1: " + new String(m.getMarshalledProperties().getData()));
                        ActiveMQMessage m1 = (ActiveMQMessage)m.getDataStructure();
                        System.out.println("Received2: " + new String(m1.getMarshalledProperties().getData()));
//                        ConsumerInfo ci = (ConsumerInfo)ds;
//System.out.println(ci);
    //                    System.out.println("content = " + m.getMessage().getContent());
    //                    System.out.println("content = " + m.getContent());
    //                    ByteSequence bs = m.getMarshalledProperties();
                    }
                    else
                    {
                        System.out.println("Received: " + message);
                    }
                }
                end = System.currentTimeMillis();
            }
            while(end - start < 1000*600);

            consumer.close();
            session.close();
            connection.close();
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occured.  Shutting down client.");
    }
}
