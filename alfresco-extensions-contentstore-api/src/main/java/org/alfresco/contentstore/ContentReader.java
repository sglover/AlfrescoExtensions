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
import java.nio.channels.ReadableByteChannel;

import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public interface ContentReader
{
    Node getNode();
    ContentStore getStore();
    ReadableByteChannel getChannel() throws IOException;
    InputStream getStream() throws IOException;
    Reader getReader() throws IOException;
    MimeType getMimeType();
    Long getSize();
}
