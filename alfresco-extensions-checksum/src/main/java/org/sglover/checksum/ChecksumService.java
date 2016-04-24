/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.sglover.alfrescoextensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public interface ChecksumService
{
    void extractChecksumsAsync(Node node, InputStream in);

    NodeChecksums getChecksums(final Node node, final InputStream in);

    NodeChecksums extractChecksums(Node node, InputStream in);

    NodeChecksums getChecksums(String nodeId, long nodeVersion);

//    void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ReadableByteChannel channel) throws IOException;
//    void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, InputStream in) throws IOException;
//    void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ByteBuffer data);

    int getBlockSize();

    void saveChecksums(NodeChecksums checksums);
}
