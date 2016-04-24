/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.patch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.NodeChecksums;
import org.sglover.checksum.PatchDocument;
import org.sglover.checksum.PatchDocumentImpl;

import com.sun.jersey.multipart.MultiPart;

/**
 * 
 * @author sglover
 *
 */
public interface PatchService
{
    void getPatch(PatchDocument patchDocument, NodeChecksums nodeChecksums, ReadableByteChannel inChannel)
            throws IOException;

//    MultiPart getPatchEntity(PatchDocument patchDocument) throws IOException;
//    MultiPart getPatchEntity(NodeChecksums nodeChecksums, ReadableByteChannel channel) throws IOException;
//    PatchDocumentImpl getPatch(MultiPart resource) throws IOException;
//    MultiPart getMultiPart(PatchDocument patchDocument);

    void writePatch(Node node, PatchDocument patchDocument, OutputStream out) throws IOException;
    PatchDocument getPatch(InputStream in) throws IOException;

    void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ReadableByteChannel channel) throws IOException;
    void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, InputStream in) throws IOException;
    void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ByteBuffer data);
}
