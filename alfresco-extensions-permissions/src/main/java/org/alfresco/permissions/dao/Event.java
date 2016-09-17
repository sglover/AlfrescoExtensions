/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions.dao;

import java.util.Date;

/**
 * 
 * @author sglover
 *
 */
public class Event
{
    private String eventType;
    private String username;
    private Date ts;

    public Event(String eventType, String username, Date ts)
    {
        super();
        this.eventType = eventType;
        this.username = username;
        this.ts = ts;
    }
    public String getEventType()
    {
        return eventType;
    }
    public String getUsername()
    {
        return username;
    }
    public Date getTs()
    {
        return ts;
    }
    
}
