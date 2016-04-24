/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import org.alfresco.cacheserver.messages.MessagesService;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.NodeChecksums;

/**
 * 
 * @author sglover
 *
 */
public class MockMessagesService implements MessagesService
{

    @Override
    public void sendContentAvailableMessage(Node node, String mimeType,
            long size, String contentPath, NodeChecksums checksums)
    {
        // TODO Auto-generated method stub
        
    }

}
