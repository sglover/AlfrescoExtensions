/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author sglover
 *
 */
public class Auths
{
    private Map<String, String> auths = new HashMap<>();

    public Auths()
    {
    }

    public static Auths start(String permission, String authority)
    {
        Auths auths = new Auths();
        auths.add(permission, authority);
        return auths;
    }

    public Auths add(String permission, String authority)
    {
        auths.put(permission, authority);
        return this;
    }

    public Map<String, String> getAuths()
    {
        return auths;
    }
}
