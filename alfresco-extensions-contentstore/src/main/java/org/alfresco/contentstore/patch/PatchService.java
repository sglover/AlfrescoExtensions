/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.patch;

import java.io.IOException;

import org.alfresco.checksum.PatchDocument;

import com.sun.jersey.multipart.MultiPart;

/**
 * 
 * @author sglover
 *
 */
public interface PatchService
{
    PatchDocument getPatch(String nodeId, long nodeInternalVersion) throws IOException;
    MultiPart getPatchEntity(PatchDocument patchDocument) throws IOException;
    MultiPart getPatchEntity(String nodeId, long nodeInternalVersion) throws IOException;
    PatchDocument getPatch(MultiPart resource) throws IOException;
    MultiPart getMultiPart(PatchDocument patchDocument);
}
