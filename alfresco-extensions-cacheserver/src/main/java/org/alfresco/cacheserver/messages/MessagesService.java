/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.messages;

import org.alfresco.checksum.NodeChecksums;
import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public interface MessagesService
{
	void sendContentAvailableMessage(Node node, String mimeType,
			long size, String contentPath, NodeChecksums checksums);
//	void sendChecksumsAvailableMessage(String contentUrl, NodeChecksums checksums);
}
