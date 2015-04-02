/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.alfresco.cacheserver.contentstore.ContentStore;
import org.alfresco.cacheserver.dao.ContentDAO;
import org.alfresco.cacheserver.entity.NodeInfo;
import org.alfresco.cacheserver.entity.NodeUsage;
import org.alfresco.cacheserver.events.ContentAvailableEvent;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.Content;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.NodeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.messaging.MessageProducer;

/**
 * 
 * @author sglover
 *
 */
public class CacheServer
{
	private static Log logger = LogFactory.getLog(CacheServer.class);

	private ContentStore contentStore;
	private ContentDAO contentDAO;
	private ContentGetter contentGetter;
	private AlfrescoApi alfrescoApi;
	private MessageProducer messageProducer;
	private CacheServerIdentity cacheServerIdentity;

	public CacheServer(ContentDAO contentDAO, ContentStore contentStore, ContentGetter contentGetter,
			AlfrescoApi alfrescoApi, CacheServerIdentity cacheServerIdentity, MessageProducer messageProducer) throws IOException
	{
		this.contentStore = contentStore;
		this.contentDAO = contentDAO;
		this.contentGetter = contentGetter;
		this.alfrescoApi = alfrescoApi;
		this.cacheServerIdentity = cacheServerIdentity;
		this.messageProducer = messageProducer;
	}

	public String updateContent(String nodeId, String nodeVersion, String nodePath, Content content) throws IOException
	{
		InputStream in = content.getIn();
		String mimeType = content.getMimeType();
		Long size = content.getSize();

		File outFile = contentStore.write(in);
		String contentPath = outFile.getAbsolutePath();
//		long size = outFile.length();

		NodeInfo nodeInfo = new NodeInfo(nodeId, nodeVersion, nodePath, contentPath, mimeType, size);
		contentDAO.updateNode(nodeInfo);

		return contentPath;
	}

	private void sendContentAvailableMessage(String nodeId, String nodeVersion, String nodePath, String mimeType,
			long size)
	{
		if(messageProducer != null)
		{
			String cacheServerId = cacheServerIdentity.getId();
			String hostname = cacheServerIdentity.getHostname();
			int port = cacheServerIdentity.getPort();
			ContentAvailableEvent event = new ContentAvailableEvent(cacheServerId, nodeId, nodeVersion,
					nodePath, mimeType, size, hostname, port);

			logger.debug("Sending event: " + event);

			messageProducer.send(event);
		}
	}

	public void contentUpdated(String nodeId, String nodeVersion, String nodePath, String expectedMimeType,
			long expectedSize) throws IOException
	{
		if(nodeId == null)
		{
			logger.warn("Null nodeId");
		}
		else
		{
			if(nodeVersion == null)
			{
				logger.warn("Ignoring " + nodeId + " with null nodeVersion");
			}
			else
			{
				try
				{
					Content content = contentGetter.getContent(nodeId, nodeVersion);
		
					if(content != null)
					{
						String mimeType = content.getMimeType();
						Long size = content.getSize();

						if(expectedSize != size)
						{
							logger.warn("For node " + nodeId + "." + nodeVersion + ", expected size " + expectedSize + ", got " + size);
						}
				
						if(expectedMimeType != null && expectedMimeType != mimeType)
						{
							logger.warn("For node " + nodeId + "." + nodeVersion + ", expected mimeType " + expectedMimeType + ", got " + mimeType);
						}

						updateContent(nodeId, nodeVersion, nodePath, content);

						sendContentAvailableMessage(nodeId, nodeVersion, nodePath, mimeType, size);
					}
					else
					{
						logger.warn("No content for node " + nodeId + "." + nodeVersion);
					}
				}
				catch(CmisObjectNotFoundException e)
				{
					logger.warn("Node " + nodeId + ";" + nodeVersion + " not found");
				}
			}
		}
	}

	public void nodeAdded(String nodeId, String nodeVersion, String nodePath) throws IOException
	{
		if(nodeId == null)
		{
			logger.warn("Null nodeId");
		}
		else
		{
			if(nodeVersion == null)
			{
				logger.warn("Ignoring " + nodeId + " with null nodeVersion");
			}
			else
			{
				NodeInfo nodeInfo = new NodeInfo(nodeId, nodeVersion, nodePath, null, null, null);
				contentDAO.updateNode(nodeInfo);
			}
		}
	}

	public void removeContent(String nodeId, String nodeVersion)
	{
		if(nodeId == null)
		{
			logger.warn("Null nodeId");
		}
		else
		{
			if(nodeVersion == null)
			{
				logger.warn("Ignoring " + nodeId + " with null nodeVersion");
			}
			else
			{
				NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion);
		
				String contentPath = nodeInfo.getContentPath();
				contentStore.remove(contentPath);
			}
		}
	}

	private Content getContent(NodeInfo nodeInfo, String username) throws IOException
	{
		String nodeId = nodeInfo.getNodeId();
		String nodeVersion = nodeInfo.getNodeVersion();
		String contentPath = nodeInfo.getContentPath();

		InputStream in = null;
		if(contentPath != null)
		{
			in = contentStore.getContent(contentPath);
			NodeUsage nodeUsage = new NodeUsage(nodeId, nodeVersion, System.currentTimeMillis(), username);
			contentDAO.addUsage(nodeUsage);
		}

		String mimeType = nodeInfo.getMimeType();
		Long size = nodeInfo.getSize();

		Content content = new Content(in, mimeType, size);
		return content;
	}

	private Content updateContent(String nodeId, String nodeVersion, String nodePath, Content content, String username) throws IOException
	{
		Content ret = null;

		InputStream in = content.getIn();
		if(in != null)
		{
			try
			{
				String contentPath = updateContent(nodeId, nodeVersion, nodePath, content);

				if(contentPath != null)
				{
					NodeUsage nodeUsage = new NodeUsage(nodeId, nodeVersion, System.currentTimeMillis(), username);
					contentDAO.addUsage(nodeUsage);

					InputStream in1 = contentStore.getContent(contentPath);
					ret = new Content(in1, content.getMimeType(), content.getSize());
				}
				else
				{
					// TODO
				}
			}
			finally
			{
				in.close();
			}
		}
		else
		{
			logger.warn("No content for node with path " + nodePath);
		}

		return ret;
	}

	public Content getByNodePath(String nodePath, String username) throws IOException
	{
		Content content = null;

		NodeInfo nodeInfo = contentDAO.getByNodePath(nodePath);
		if(nodeInfo != null)
		{
			content = getContent(nodeInfo, username);
		}
		else
		{
			NodeId fullNodeId = alfrescoApi.getObjectIdForNodePath(nodePath);
			String nodeId = fullNodeId.getNodeId();
			String nodeVersion = fullNodeId.getNodeVersion();
			// TODO
			String mimeType = null;
			long size = -1l;
			try
			{
				Content repoContent = contentGetter.getContent(nodeId, nodeVersion);
				if(repoContent != null)
				{
					content = updateContent(nodeId, nodeVersion, nodePath, repoContent, username);
					sendContentAvailableMessage(nodeId, nodeVersion, nodePath, mimeType, size);
				}
				else
				{
					logger.warn("No content for node with path " + nodePath);
				}
			}
			catch(CmisObjectNotFoundException e)
			{
				logger.warn("Node " + nodeId + ";" + nodeVersion + " not found");
			}
		}

		return content;
	}

	public Content getByNodeId(String nodeId, String nodeVersion, String username) throws IOException
	{
		Content content = null;

		NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion);
		if(nodeInfo != null)
		{
			content = getContent(nodeInfo, username);
		}
		else
		{
			try
			{
				String nodePath = alfrescoApi.getPrimaryNodePathForNodeId(nodeId, nodeVersion);
				Content repoContent = contentGetter.getContent(nodeId, nodeVersion);
				if(repoContent != null)
				{
					content = updateContent(nodeId, nodeVersion, nodePath, repoContent, username);
					sendContentAvailableMessage(nodeId, nodeVersion, nodePath, content.getMimeType(), content.getSize());
				}
				else
				{
					logger.warn("No content for node " + nodeId + ";" + nodeVersion);
				}
			}
			catch(CmisObjectNotFoundException e)
			{
				logger.warn("Node " + nodeId + ";" + nodeVersion + " not found");
			}
		}

		return content;
	}

}
