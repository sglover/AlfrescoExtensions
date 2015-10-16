/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dao;

import org.alfresco.cacheserver.dao.data.Registration;

/**
 * 
 * @author sglover
 *
 */
public interface WebsocketDAO
{
    void register(Registration registration);
    Registration getByUsername(String username);
}
