/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.events.types.TransactionCommittedEvent;

/**
 * 
 * @author sglover
 *
 */
public class CountingEventListener implements EventListener
{
	private Map<String, AtomicLong> userCounters = new ConcurrentHashMap<String, AtomicLong>();

	@Override
	public void onMessage(Object event)
	{
		System.out.println(event);

		if(event instanceof TransactionCommittedEvent)
		{
			TransactionCommittedEvent txnEvent = (TransactionCommittedEvent)event;
			String username = txnEvent.getUsername();
			AtomicLong counter = userCounters.get(username);
			if(counter == null)
			{
				counter = new AtomicLong(0);
				userCounters.put(username, counter);
			}
			counter.incrementAndGet();
		}
	}

	public Map<String, AtomicLong> getCounts()
	{
		return userCounters;
	}
}
