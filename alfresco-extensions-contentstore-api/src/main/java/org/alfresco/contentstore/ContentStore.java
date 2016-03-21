/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.checksum.PatchDocument;
import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public interface ContentStore
{
    boolean exists(String nodeId, long nodeVersion);
    boolean exists(String nodeId, long nodeVersion, MimeType mimeType);
    ContentWriter getWriter(Node node, MimeType mimeType) throws IOException;
    ContentReader getReader(Node node) throws IOException;
    ContentReader getReader(Node node, MimeType mimeType) throws IOException;
    Node applyPatch(String nodeId, long nodeVersion, PatchDocument patchDocument) throws IOException;
    ReadableByteChannel getChannel(Node node) throws IOException;
    ReadableByteChannel getChannel(Node node, MimeType mimeType) throws IOException;
}
