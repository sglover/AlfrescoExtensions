/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.entities;

import java.io.IOException;

import org.alfresco.cacheserver.entity.Node;
import org.alfresco.httpclient.AuthenticationException;

/**
 * 
 * @author sglover
 *
 */
public interface EntitiesService
{
	void getEntities(Node node) throws AuthenticationException, IOException;
}
