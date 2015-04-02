/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dao;

import org.alfresco.cacheserver.entity.NodeInfo;
import org.alfresco.cacheserver.entity.NodeUsage;

/**
 * 
 * @author sglover
 *
 */
public interface ContentDAO
{
	NodeInfo getByNodePath(String contentURL);
	NodeInfo getByNodeId(String nodeId, String nodeVersion);
	void updateNode(NodeInfo nodeInfo);

	void addUsage(NodeUsage nodeUsage);
}
