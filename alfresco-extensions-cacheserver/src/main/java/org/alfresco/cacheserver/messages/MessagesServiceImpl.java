/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.messages;

import org.alfresco.cacheserver.CacheServerIdentity;
import org.alfresco.cacheserver.events.ContentAvailableEvent;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.extensions.common.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.messaging.MessageProducer;

/**
 * 
 * @author sglover
 *
 */
public class MessagesServiceImpl implements MessagesService
{
	private static Log logger = LogFactory.getLog(MessagesServiceImpl.class);

	private CacheServerIdentity cacheServerIdentity;
	private MessageProducer messageProducer;

	public MessagesServiceImpl(CacheServerIdentity cacheServerIdentity, MessageProducer messageProducer)
	{
		this.cacheServerIdentity = cacheServerIdentity;
		this.messageProducer = messageProducer;
	}

	@Override
	public void sendContentAvailableMessage(Node node, String mimeType, long size, String contentPath,
	        NodeChecksums checksums)
	{
		if(messageProducer != null)
		{
			String cacheServerId = cacheServerIdentity.getId();
			String hostname = cacheServerIdentity.getHostname();
			int port = cacheServerIdentity.getPort();
			ContentAvailableEvent event = new ContentAvailableEvent(cacheServerId, node,
					mimeType, size, hostname, port, checksums);

			logger.debug("Sending event: " + event);

			messageProducer.send(event);
		}
	}

//	@Override
//	public void sendChecksumsAvailableMessage(String contentUrl, NodeChecksums checksums)
//	{
//		if(messageProducer != null)
//		{
//			String cacheServerId = cacheServerIdentity.getId();
//			ChecksumsAvailableEvent event = new ChecksumsAvailableEvent(cacheServerId, contentUrl,
//					checksums);
//
//			logger.debug("Sending event: " + event);
//
//			messageProducer.send(event);
//		}
//	}
}
