/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.nio.channels.ReadableByteChannel;

/**
 * 
 * @author sglover
 *
 */
public class Content
{
    private ReadableByteChannel channel;
    private Long size;

    public Content(ReadableByteChannel channel, Long size)
    {
        super();
        this.channel = channel;
        this.size = size;
    }
    public ReadableByteChannel getChannel()
    {
        return channel;
    }
    public Long getSize()
    {
        return size;
    }
}
