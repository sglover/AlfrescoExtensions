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
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.PatchDocument;

/**
 * 
 * @author sglover
 *
 */
public interface ContentStore
{
    Node applyPatch(Node node, PatchDocument patchDocument) throws IOException;
    PatchDocument getPatch(Node node, InputStream in) throws IOException;
    PatchDocument getPatch(Node node) throws IOException;
    void writePatchAsProtocolBuffer(Node node, OutputStream out) throws IOException;

    InputStream getBlockAsInputStream(Node node, long rangeId, int size);

    boolean exists(Node node);

    ContentWriter getWriter(Node node) throws IOException;
    ContentReader getReader(Node node) throws IOException;
    ReadableByteChannel getChannel(Node node) throws IOException;
}
