/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao;

import org.springframework.security.core.userdetails.UserDetails;

public class UserContext
{
	private static ThreadLocal<UserDetails> user = new ThreadLocal<>();
	
	public static UserDetails getUser()
	{
		return user.get();
	}
	
	public static void setUser(UserDetails newUser)
	{
		user.set(newUser);
	}
}
