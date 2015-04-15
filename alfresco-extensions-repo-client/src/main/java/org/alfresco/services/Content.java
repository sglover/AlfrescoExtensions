/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.InputStream;

/**
 * 
 * @author sglover
 *
 */
public class Content
{
	private InputStream in;
	private String mimeType;
	private Long size;
	public Content(InputStream in, String mimeType, Long size)
    {
	    super();
	    this.in = in;
	    this.mimeType = mimeType;
	    this.size = size;
    }
	public InputStream getIn()
	{
		return in;
	}
	public String getMimeType()
	{
		return mimeType;
	}
	public Long getSize()
	{
		return size;
	}
}
