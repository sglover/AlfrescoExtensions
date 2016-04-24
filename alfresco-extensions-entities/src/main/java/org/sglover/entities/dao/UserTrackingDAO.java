/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao;

import java.util.List;

import org.alfresco.events.node.types.NodeContentGetEvent;
import org.alfresco.events.node.types.TransactionCommittedEvent;
import org.sglover.entities.values.ViewedNode;

/**
 * 
 * @author sglover
 *
 */
public interface UserTrackingDAO
{
	void addUserNodeView(NodeContentGetEvent event);
	List<ViewedNode> viewedNodes(String username, long timeDelta);
	void txnCommitted(TransactionCommittedEvent event);
}
