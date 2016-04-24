/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.alfresco.extensions.events.EventListener;
import org.alfresco.extensions.events.Events;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.MessageDispatch;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class EventPrinter
{
    // "tcp://ec2-54-170-12-65.eu-west-1.compute.amazonaws.com:61616"
    private String hostname = "localhost";
//    private String jmsName = "ActiveMQ.Advisory.Consumer.Topic.alfresco.repo.events.nodes";
    private String jmsName = "amqp:topic:alfresco.repo.events.activities";

    private Events events;

    @Before
    public void before() throws Exception {
        EventListener eventListener = new EventListener()
        {

            @Override
            public void onMessage(Object event) {
                System.out.println(event);
            }
        };
        this.events = new Events(jmsName, eventListener);
    }

    @Test
    public void test1() throws Exception {
        // ConnectionFactory out = new
        // ActiveMQConnectionFactory("tcp://http://172.29.102.6/:61616?jms.prefetchPolicy.all=10000");
        // ?jms.prefetchPolicy.all=10000
        ConnectionFactory out = new ActiveMQConnectionFactory("tcp://" + hostname + ":61616");
        ActiveMQConnection connection = (ActiveMQConnection) out.createConnection();

        connection.start();
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        Set<ActiveMQQueue> amqs = connection.getDestinationSource().getQueues();
        Iterator<ActiveMQQueue> queues = amqs.iterator();

        while (queues.hasNext()) {
            ActiveMQQueue queue_t = queues.next();
            String q_name = queue_t.getPhysicalName();
            List<MessageDispatch> msgList = ((ActiveMQSession) session)
                    .getUnconsumedMessages();

            System.out.println("\nQueue = " + q_name);
            if (q_name.equals(jmsName))
//                    "ActiveMQ.Advisory.Consumer.Topic.alfresco.repo.events.nodes"))
            // if(q_name.equals("ActiveMQ.DLQ"))
            {
                QueueBrowser queueBrowser = session.createBrowser(queue_t);
                Enumeration e = queueBrowser.getEnumeration();

                int numMsgs = 0;
                int i = 0;
                while (i++ < 1 && e.hasMoreElements()) {
                    Message message = (Message) e.nextElement();
                    System.out.println("message = " + message);
                    if (message instanceof ActiveMQTextMessage) {
                        ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
                        String text = activeMQTextMessage.getText();
                        System.out.println("text = " + text);
                    }
                    numMsgs++;
                }
                System.out.println("No of messages = " + numMsgs);
                queueBrowser.close();
            }
        }
        session.close();
        connection.close();
    }
    // private static ApplicationContext ctx =
    // ApplicationContextHelper.getApplicationContext();

    // private void createEndpoint()
    // {
    // EventHandler eventHandler = new EventHandler()
    // {
    //
    // @Override
    // public void shutdown()
    // {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void handle(Event event)
    // {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public Stats getStats()
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }
    // };
    // final QueueConsumer messageConsumer = new QueueConsumer(eventHandler);
    // final AmqpDirectEndpoint endpoint =
    // AmqpNodeBootstrapUtils.createEndpoint(messageConsumer, broker,
    // null, null, queue, queue);
    // testExecutorService.execute(endpoint.getListener());
    //
    // // Wait for listener initialization
    // while (!endpoint.isInitialized())
    // {
    // try
    // {
    // Thread.sleep(100);
    // }
    // catch (InterruptedException e)
    // {
    // }
    // }
    //
    // Runnable task = new Runnable()
    // {
    // @Override
    // public void run()
    // {
    // for(int i = 0; i < numTxns; i++)
    // {
    // String txnId = String.valueOf(i);
    //
    // String parentPath = subscriptionPath + txnId + "/";
    // String toParentPath = subscriptionPath + txnId + "/";
    // List<List<String>> parentNodeIds =
    // Arrays.asList(Arrays.asList(GUID.generate(), GUID.generate(),
    // GUID.generate()));
    // List<List<String>> toParentNodeIds =
    // Arrays.asList(Arrays.asList(GUID.generate(),
    // GUID.generate(), GUID.generate()));
    //
    // String nodeId = GUID.generate();
    // String nodeName = "name" + i;
    // String newName = nodeName;
    // List<String> paths = Arrays.asList(parentPath + nodeName);
    // List<String> toPaths = Arrays.asList(toParentPath + newName);
    //
    // for(int j = 1; j <= numEventsPerTxn; j++)
    // {
    // Event event = null;
    //
    // if(j == 1)
    // {
    // event = new NodeAddedEvent(seqNumber(), nodeName, txnId,
    // System.currentTimeMillis(),
    // null, null, nodeId, "cm:document", paths, parentNodeIds, "user1",
    // null, null, null, null);
    // results.expectedNumAdds.incrementAndGet();
    // }
    // else if(j == 5)
    // {
    // if(i % 2 == 0)
    // {
    // event = new NodeMovedEvent(seqNumber(), nodeName, newName, txnId,
    // System.currentTimeMillis(),
    // null, null, nodeId, "cm:document", paths, parentNodeIds, "user1",
    // null, toPaths, toParentNodeIds, null, null, null);
    // results.expectedNumMoves.incrementAndGet();
    // }
    // else
    // {
    // event = new NodeRenamedEvent(seqNumber(), nodeName, newName, txnId,
    // System.currentTimeMillis(),
    // null, null, nodeId, "cm:document", paths, parentNodeIds, "user1",
    // null, toPaths, null, null, null);
    // results.expectedNumRenames.incrementAndGet();
    // }
    // }
    // else
    // {
    // event = new NodeUpdatedEvent(seqNumber(), nodeName, txnId,
    // System.currentTimeMillis(),
    // null, null, nodeId, "cm:document", paths,
    // parentNodeIds, "user1", null, null, null, null, null,
    // null, null, null, null);
    // results.expectedNumUpdates.incrementAndGet();
    // }
    //
    // results.expectedNumEvents.incrementAndGet();
    // endpoint.send(event);
    // }
    //
    // results.expectedNumEvents.incrementAndGet();
    // results.expectedNumCommits.incrementAndGet();
    // TransactionCommittedEvent event = new
    // TransactionCommittedEvent(seqNumber(), txnId, null,
    // System.currentTimeMillis(), "user1", null);
    // endpoint.send(event);
    // }
    // }
    // };
    //
    // testExecutorService.execute(task);
    // }
}
