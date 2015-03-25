/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

import org.alfresco.events.node.types.RepositoryEvent;

/**
 * An event that occurs on an Alfresco Site.
 * 
 * @author Gethin James
 */
public interface SiteEvent extends RepositoryEvent
{
    public String getSiteId();
}