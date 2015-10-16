/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import org.alfresco.checksum.PatchDocument;
import org.alfresco.extensions.common.Content;
import org.alfresco.httpclient.AuthenticationException;

/**
 * 
 * @author sglover
 *
 */
public interface ContentStore
{
    Content getByNodeId(String nodeId, String nodeVersion, boolean isPrimary) throws IOException;
    Content getTextContent(long nodeId) throws AuthenticationException, IOException;
    SeekableByteChannel getContent(String path) throws IOException;
    String applyPatch(PatchDocument patchDocument, String existingContentPath) throws IOException;
}
