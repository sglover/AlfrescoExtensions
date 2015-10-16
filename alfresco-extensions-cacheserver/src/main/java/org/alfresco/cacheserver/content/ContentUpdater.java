/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.content;

import java.io.IOException;

import org.alfresco.extensions.common.Node;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

/**
 * 
 * @author sglover
 *
 */
public interface ContentUpdater
{
    public static enum OperationType
    {
        None, Sync, Async;
    }

    void updateContent(Node node, OperationType checksums, OperationType transforms,
            final String expectedMimeType,
            final Long expectedSize) throws IOException, CmisObjectNotFoundException;
//    String updateContent(Node node, boolean asyncChecksums, String expectedMimeType, Long expectedSize)
//            throws IOException, CmisObjectNotFoundException;
//    String updateContent(Node node, Content content, boolean asyncChecksums) throws IOException;
}
