/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.ByteArrayInputStream;
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
public class StringContentReader extends AbstractContentReader
{
    private ContentStore store;
    private byte[] bytes;

    public StringContentReader(String content, ContentStore store, Node node)
    {
        super(node);
        this.bytes = content.getBytes();
        this.store = store;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException
    {
        InputStream in = new ByteArrayInputStream(bytes);
        ReadableByteChannel channel = Channels.newChannel(in);
        return channel;
    }

    @Override
    public Long getSize()
    {
        return Long.valueOf(bytes.length);
    }

    @Override
    public ContentStore getStore()
    {
        return store;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.TEXT;
    }

    @Override
    public InputStream getStream() throws IOException
    {
        InputStream in = new ByteArrayInputStream(bytes);
        return in;
    }

    @Override
    public Reader getReader() throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
