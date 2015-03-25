/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.util.List;

public class DiscoveryServiceImpl
{
	private UserTrackingDAO userTrackingDAO;
	private EntitiesDAO entitiesDAO;

	public void a(String username)
	{
		List<ViewedNode> viewedNodes = userTrackingDAO.viewedNodes(username, 1000*60*60*24*30);
		for(ViewedNode viewedNode : viewedNodes)
		{
			long nodeInternalId = viewedNode.getNodeInternalId();
			long nodeVersion = viewedNode.getNodeVersion();

			entitiesDAO.overlap(nodeInternalId, nodeVersion);

//			for(Entity<String> entity : entitiesDAO.getNames(nodeInternalId, nodeVersion))
//			{
//				String entityValue = entity.getEntity();
//			}
		}
	}
}
