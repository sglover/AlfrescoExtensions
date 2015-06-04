/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.checksum;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author sglover
 *
 */
public interface ChecksumService
{
//	DocumentChecksums extractChecksums(String nodeId, String versionLabel, String contentPath);
//	void extractChecksumsAsync(String nodeId, String versionLabel, String contentPath);
	DocumentChecksums getChecksums(String contentUrl);
	PatchDocument createPatchDocument(DocumentChecksums checksums, ByteBuffer data);
	int getBlockSize();
	Adler32 adler32(int offset, int end, ByteBuffer data);
	String md5(byte[] bytes) throws NoSuchAlgorithmException;
	void saveChecksums(DocumentChecksums checksums);
}
