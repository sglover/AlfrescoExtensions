/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao;

import org.sglover.alfrescoextensions.common.MimeType;

/**
 * 
 * @author sglover
 *
 */
public interface ContentDAO
{
    boolean nodeExists(String nodeId, long nodeInternalVersion, boolean isPrimary);

    NodeInfo getByNodeId(String nodeId, long nodeInternalVersion, MimeType mimeType);

    NodeInfo getByNodeId(String nodeId, long nodeInternalVersion, boolean isPrimary);

    NodeInfo getByNodeId(String nodeId, String nodeVersion, boolean isPrimary);

    NodeInfo getByNodeId(long nodeInternalId, String mimeType);

    void updateNode(NodeInfo node);
}
