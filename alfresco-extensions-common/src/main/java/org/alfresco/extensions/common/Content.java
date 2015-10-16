/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.common;

import java.nio.channels.ReadableByteChannel;

/**
 * 
 * @author sglover
 *
 */
public class Content
{
	private ReadableByteChannel channel;
	private String mimeType;
	private Long size;
	public Content(ReadableByteChannel channel, String mimeType, Long size)
    {
	    super();
	    this.channel = channel;
	    this.mimeType = mimeType;
	    this.size = size;
    }
	public ReadableByteChannel getChannel()
	{
		return channel;
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
