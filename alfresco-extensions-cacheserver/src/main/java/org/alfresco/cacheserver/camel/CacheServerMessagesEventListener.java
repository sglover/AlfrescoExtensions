/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.camel;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;

import org.alfresco.cacheserver.CacheServerIdentity;
import org.alfresco.cacheserver.content.ContentUpdater;
import org.alfresco.cacheserver.content.ContentUpdater.OperationType;
import org.alfresco.cacheserver.events.ContentAvailableEvent;
import org.alfresco.cacheserver.http.AuthenticationException;
import org.alfresco.cacheserver.http.CacheHttpClient;
import org.alfresco.cacheserver.http.HttpCallback;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.checksum.PatchDocument;
import org.alfresco.checksum.dao.ChecksumDAO;
import org.alfresco.contentstore.AbstractContentStore;
import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.contentstore.dao.NodeInfo;
import org.alfresco.extensions.common.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * @author sglover
 *
 */
public class CacheServerMessagesEventListener
{
	private static Log logger = LogFactory.getLog(CacheServerMessagesEventListener.class);

	private CacheHttpClient cacheHttpClient;
	private CacheServerIdentity cacheServerIdentity;
	private ChecksumDAO checksumsDAO;
	private ContentDAO contentDAO;
	private AbstractContentStore contentStore;
	private ContentUpdater contentUpdater;

	public CacheServerMessagesEventListener(CacheHttpClient cacheHttpClient,
			CacheServerIdentity cacheServerIdentity, ChecksumDAO checksumsDAO,
			ContentUpdater contentUpdater)
	{
		super();
		this.cacheHttpClient = cacheHttpClient;
		this.cacheServerIdentity = cacheServerIdentity;
		this.checksumsDAO = checksumsDAO;
		this.contentUpdater = contentUpdater;
	}

	public void onMessage(Object message) throws IOException, AuthenticationException, MessagingException
	{
		if(message instanceof ContentAvailableEvent)
		{
			final ContentAvailableEvent contentAvailableEvent = (ContentAvailableEvent)message;

			String cacheServerId = contentAvailableEvent.getCacheServerId();
			if(cacheServerIdentity.getId().equals(cacheServerId))
			{
				// ignore messages from this cache server
				return;
			}
			final Node node = contentAvailableEvent.getNode();
			final String hostname = contentAvailableEvent.getHostname();
			final int port = contentAvailableEvent.getPort();

			logger.debug("Getting content for node " + node
					+ " from cache server "
					+ hostname + ":" + port
					+ ", id = " + cacheServerId);

			HttpCallback callback = new HttpCallback()
			{
				@Override
				public void execute(InputStream in)
				{
					try
					{
						String mimeType = contentAvailableEvent.getMimeType();
						long size = contentAvailableEvent.getSize();
						contentUpdater.updateContent(node, OperationType.None, OperationType.None, mimeType, size);
					}
					catch(IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			};
			cacheHttpClient.getNodeById(hostname, port, "admin", "admin", node.getNodeId(),
					node.getVersionLabel(), callback);
			NodeInfo nodeInfo = contentDAO.getByNodeId(node.getNodeId(), node.getVersionLabel(), true);
			String contentPath = nodeInfo.getContentPath();

			final NodeChecksums checksums = contentAvailableEvent.getChecksums();

			checksumsDAO.saveChecksums(checksums);

		    String nodeId = contentAvailableEvent.getNode().getNodeId();
		    long nodeVersion = contentAvailableEvent.getNode().getNodeVersion();

			PatchDocument patchDocument = cacheHttpClient.getPatches(hostname, port, "admin", "admin", 
					nodeId, nodeVersion);
			contentStore.applyPatch(patchDocument, contentPath);
		}
		else
		{
		    // TODO
		}
//		else if(message instanceof ChecksumsAvailableEvent)
//		{
//			final ChecksumsAvailableEvent checksumsAvailableEvent = (ChecksumsAvailableEvent)message;
//
//			String cacheServerId = checksumsAvailableEvent.getCacheServerId();
//			if(cacheServerIdentity.getId().equals(cacheServerId))
//			{
//				// ignore messages from this cache server
//				return;
//			}
//			final NodeChecksums checksums = checksumsAvailableEvent.getChecksums();
//
//			checksumsDAO.saveChecksums(checksums);
//		}
	}
}
