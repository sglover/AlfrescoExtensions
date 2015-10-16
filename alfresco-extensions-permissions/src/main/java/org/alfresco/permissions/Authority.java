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
public class Authority
{
	private String parentAuthority;
	private long rangeStart;
	private long rangeEnd;
	public Authority(String parentAuthority, long rangeStart, long rangeEnd)
    {
	    super();
	    this.parentAuthority = parentAuthority;
	    this.rangeStart = rangeStart;
	    this.rangeEnd = rangeEnd;
    }
	public String getParentAuthority()
	{
		return parentAuthority;
	}
	public long getRangeStart()
	{
		return rangeStart;
	}
	public long getRangeEnd()
	{
		return rangeEnd;
	}

	
}
