/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author sglover
 *
 */
public class Acl
{
	private Set<Ace> aces = new HashSet<>();

	public static Acl acl()
	{
		return new Acl();
	}

	public Acl()
    {
	    super();
    }

	public Acl addAce(Ace ace)
	{
		aces.add(ace);
		return this;
	}

	public Set<Ace> getAces()
	{
		return aces;
	}

	@Override
    public String toString()
    {
	    return "Acl [aces=" + aces + "]";
    }

	
}
