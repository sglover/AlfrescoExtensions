/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.checksum;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public interface ChecksumService
{
	void extractChecksumsAsync(Node node, String contentPath);
	NodeChecksums extractChecksums(Node node, String contentPath);
	NodeChecksums getChecksums(String nodeId, long nodeVersion);
	PatchDocument createPatchDocument(NodeChecksums checksums, ReadableByteChannel channel) throws IOException;
	PatchDocument createPatchDocument(NodeChecksums checksums, ByteBuffer data);
	int getBlockSize();
	void saveChecksums(NodeChecksums checksums);
}
