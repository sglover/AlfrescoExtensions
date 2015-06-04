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

import org.alfresco.cacheserver.CacheServer;
import org.alfresco.cacheserver.CacheServerIdentity;
import org.alfresco.cacheserver.checksum.DocumentChecksums;
import org.alfresco.cacheserver.dao.ChecksumDAO;
import org.alfresco.cacheserver.events.ChecksumsAvailableEvent;
import org.alfresco.cacheserver.events.ContentAvailableEvent;
import org.alfresco.cacheserver.http.AuthenticationException;
import org.alfresco.cacheserver.http.CacheHttpClient;
import org.alfresco.cacheserver.http.HttpCallback;
import org.alfresco.services.Content;
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

	private CacheServer cacheServer;
	private CacheHttpClient cacheHttpClient;
	private CacheServerIdentity cacheServerIdentity;
	private ChecksumDAO checksumsDAO;

	public CacheServerMessagesEventListener(CacheServer cacheServer, CacheHttpClient cacheHttpClient,
			CacheServerIdentity cacheServerIdentity, ChecksumDAO checksumsDAO)
	{
		super();
		this.cacheServer = cacheServer;
		this.cacheHttpClient = cacheHttpClient;
		this.cacheServerIdentity = cacheServerIdentity;
		this.checksumsDAO = checksumsDAO;
	}

	public void onMessage(Object message) throws IOException, AuthenticationException
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
			final String nodeId = contentAvailableEvent.getNodeId();
			final String nodeVersion = contentAvailableEvent.getNodeVersion();
			final Long nodeInternalId = contentAvailableEvent.getNodeInternalId();
			final String nodePath = contentAvailableEvent.getNodePath();
			final String hostname = contentAvailableEvent.getHostname();
			final int port = contentAvailableEvent.getPort();

			logger.debug("Getting content for node " + nodeId + "." + nodeVersion
					+ ", "
					+ nodePath
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
						Content content = new Content(in, mimeType, size);
						cacheServer.updateContent(nodeId, nodeInternalId, nodeVersion, nodePath, content);
					}
					catch(IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			};
			cacheHttpClient.getNodeById(hostname, port, "admin", "admin", nodeId, nodeVersion, callback);
		}
		else if(message instanceof ChecksumsAvailableEvent)
		{
			final ChecksumsAvailableEvent checksumsAvailableEvent = (ChecksumsAvailableEvent)message;

			String cacheServerId = checksumsAvailableEvent.getCacheServerId();
			if(cacheServerIdentity.getId().equals(cacheServerId))
			{
				// ignore messages from this cache server
				return;
			}
			final DocumentChecksums checksums = checksumsAvailableEvent.getChecksums();

			checksumsDAO.saveChecksums(checksums);
		}
	}
}
