/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.dao;

import java.util.List;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.TransactionCommittedEvent;

/**
 * 
 * @author sglover
 *
 */
public interface EventsDAO
{
	void addEvent(Event nodeEvent);
	void txnCommitted(TransactionCommittedEvent event);
	List<Event> getEventsForTxn(String txnId);
}
