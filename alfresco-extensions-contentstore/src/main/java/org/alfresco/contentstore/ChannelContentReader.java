/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public class ChannelContentReader extends AbstractContentReader
{
    private ContentStore store;
    private ContentReference ref;
    private ReadableByteChannel channel;
    private Long size;

    public ChannelContentReader(ReadableByteChannel channel, ContentStore store, Node node, Long size)
    {
        super(node);
        this.channel = channel;
        this.size = size;
        this.store = store;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException
    {
        return channel;
    }

    @Override
    public MimeType getMimeType()
    {
        return ref.getMimetype();
    }

    @Override
    public Long getSize()
    {
        return size;
    }

    @Override
    public Node getNode()
    {
        return node;
    }

    @Override
    public ContentStore getStore()
    {
        return store;
    }

    @Override
    public InputStream getStream() throws IOException
    {
        return Channels.newInputStream(channel);
    }

    @Override
    public Reader getReader() throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
