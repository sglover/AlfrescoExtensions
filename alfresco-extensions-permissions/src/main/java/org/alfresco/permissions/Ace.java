/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

/**
 * 
 * @author sglover
 *
 */
public class Ace
{
	private String group;
	private String permission;

	public String getGroup()
	{
		return group;
	}
	public static Ace setGroup(String group)
	{
		Ace ace = new Ace();
		ace.group = group;
		return ace;
	}
	public String getPermission()
	{
		return permission;
	}
	public Ace setPermission(String permission)
	{
		this.permission = permission;
		return this;
	}
	@Override
    public String toString()
    {
	    return "Ace [group=" + group + ", permission=" + permission + "]";
    }

	
}
