/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service;

import static org.junit.Assert.assertNotNull;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class Statistics implements ExceptionListener
{
    @Test
    public void test1() throws Exception
    {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();

        connection.setExceptionListener(this);
        
        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue replyTo = session.createTemporaryQueue();
        MessageConsumer consumer = session.createConsumer(replyTo);

//        String queueName = "ActiveMQ.Statistics.Broker";
//        String queueName = "ActiveMQ.Statistics.Destination." + "alfresco.repo.events.nodes";
        String queueName = "ActiveMQ.Statistics.Subscription";
        Queue testQueue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(testQueue);
        Message msg = session.createMessage();
        msg.setJMSReplyTo(replyTo);
        producer.send(msg);
         
        MapMessage reply = (MapMessage) consumer.receive();
        assertNotNull(reply);
         
        for (Enumeration e = reply.getMapNames();e.hasMoreElements();) {
          String name = e.nextElement().toString();
          System.out.println(name + "=" + reply.getObject(name));
        }
    }

    @Override
    public void onException(JMSException exception)
    {
        System.err.println(exception);
    }
}
