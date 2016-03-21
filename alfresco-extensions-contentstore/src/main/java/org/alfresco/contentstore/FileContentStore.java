/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public interface FileContentStore extends ContentStore
{
    FileContentReader getFileContentReader(Node node);
    FileContentWriter getFileContentWriter(ContentReference reference);
}
