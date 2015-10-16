/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dao.data;

/**
 * 
 * @author sglover
 *
 */
public class Registration
{
    private String ipAddress;
    private String username;

    public Registration(String ipAddress, String username)
    {
        super();
        this.ipAddress = ipAddress;
        this.username = username;
    }
    public String getIpAddress()
    {
        return ipAddress;
    }
    public String getUsername()
    {
        return username;
    }
    @Override
    public String toString()
    {
        return "Registration [ipAddress=" + ipAddress + ", username="
                + username + "]";
    }
}
