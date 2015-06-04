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
import java.util.List;

import org.alfresco.cacheserver.checksum.ChecksumService;
import org.alfresco.cacheserver.contentstore.ContentStore;
import org.alfresco.cacheserver.dao.ContentDAO;
import org.alfresco.cacheserver.entities.EntitiesService;
import org.alfresco.cacheserver.entity.Node;
import org.alfresco.cacheserver.entity.NodeInfo;
import org.alfresco.cacheserver.entity.NodeUsage;
import org.alfresco.cacheserver.events.ContentAvailableEvent;
import org.alfresco.cacheserver.transform.TransformService;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.Content;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.NodeId;
import org.alfresco.transformation.api.ContentReference;
import org.alfresco.transformation.api.MimeType;
import org.alfresco.transformation.api.TransformRequest;
import org.alfresco.transformation.api.TransformResponse;
import org.alfresco.transformation.client.TransformationCallback;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.messaging.MessageProducer;
import org.springframework.security.core.userdetails.UserDetails;

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
	private EntitiesService entitiesService;
	private TransformService transformService;
	private ChecksumService checksumService;

	public CacheServer(ContentDAO contentDAO, ContentStore contentStore,
			EntitiesService entitiesService, AlfrescoApi alfrescoApi, CacheServerIdentity cacheServerIdentity,
			MessageProducer messageProducer, ContentGetter contentGetter, ChecksumService checksumService,
			TransformService transformService) throws IOException
	{
		this.contentStore = contentStore;
		this.contentDAO = contentDAO;
		this.entitiesService = entitiesService;
		this.contentGetter = contentGetter;
		this.checksumService = checksumService;
//		new ContentGetter()
//		{
//			@Override
//            public Content getContent(String nodeId, String nodeVersion)
//            {
//				try
//				{
//		            NodeInfo nodeInfo = CacheServer.this.contentDAO.getByNodeId(nodeId, nodeVersion, true);
//		            String contentPath = nodeInfo.getContentPath();
//		            InputStream in = CacheServer.this.contentStore.getContent(contentPath);
//		            Content content = new Content(in, nodeInfo.getMimeType(), nodeInfo.getSize());
//		            return content;
//				}
//				catch(IOException e)
//				{
//					throw new RuntimeException(e);
//				}
//            }
//
//			@Override
//            public GetTextContentResponse getTextContent(long nodeId)
//                    throws AuthenticationException, IOException
//            {
//				try
//				{
//		            NodeInfo nodeInfo = CacheServer.this.contentDAO.getByNodeId(nodeId, "text/plain");
//		            String contentPath = nodeInfo.getContentPath();
//		            InputStream in = CacheServer.this.contentStore.getContent(contentPath);
//		            GetTextContentResponse getTextResponse = new GetTextContentResponse(in, null, 
//		            		null, null, null);
//		            return getTextResponse;
//				}
//				catch(IOException e)
//				{
//					throw new RuntimeException(e);
//				}
//            }
//			
//		};
		this.alfrescoApi = alfrescoApi;
		this.cacheServerIdentity = cacheServerIdentity;
		this.messageProducer = messageProducer;
		this.checksumService = checksumService;
		this.transformService = transformService;
	}

	public String updateContent(String nodeId, Long nodeInternalId, String versionLabel, String nodePath, Content content) throws IOException
	{
		InputStream in = content.getIn();
		String mimeType = content.getMimeType();
		Long size = content.getSize();

		logger.debug("CacheServer updating content for node " + nodeId + "." + versionLabel + ", " + nodePath
				+ ", " + content.getMimeType() + ", " + content.getSize());

		File outFile = contentStore.write(in);
		String contentPath = outFile.getAbsolutePath();

		sendContentAvailableMessage(nodeId, nodeInternalId, versionLabel, nodePath, mimeType, size);

		NodeInfo nodeInfo = NodeInfo.start()
				.setNodeId(nodeId)
				.setNodeInternalId(nodeInternalId)
				.setNodeVersion(versionLabel)
				.setNodePath(nodePath)
				.setContentPath(contentPath)
				.setMimeType(mimeType)
				.setSize(size);
		contentDAO.updateNode(nodeInfo);

		return contentPath;
	}

	private void sendContentAvailableMessage(String nodeId, Long nodeInternalId,
			String nodeVersion, String nodePath, String mimeType,
			long size)
	{
		if(messageProducer != null)
		{
			String cacheServerId = cacheServerIdentity.getId();
			String hostname = cacheServerIdentity.getHostname();
			int port = cacheServerIdentity.getPort();
			ContentAvailableEvent event = new ContentAvailableEvent(cacheServerId, nodeId, nodeInternalId, nodeVersion,
					nodePath, mimeType, size, hostname, port);

			logger.debug("Sending event: " + event);

			messageProducer.send(event);
		}
	}

	public void contentUpdated(final Node node, final String nodePath, final String expectedMimeType,
			final Long expectedSize) throws IOException
	{
		final String nodeId = node.getNodeId();
		final String nodeVersion = node.getNodeVersion();
		final long nodeInternalId = node.getNodeInternalId();

		if(node.getNodeId() == null)
		{
			logger.warn("Null nodeId");
		}
		else
		{
			if(node.getNodeVersion() == null)
			{
				logger.warn("Ignoring " + node.getNodeId() + " with null nodeVersion");
			}
			else
			{
				try
				{
					Content content = contentGetter.getContent(node.getNodeId(), node.getNodeVersion());
		
					if(content != null)
					{
						final String mimeType = content.getMimeType();
						Long size = content.getSize();

						if(expectedSize != null && expectedSize.longValue() != size)
						{
							logger.warn("For node " + nodeId + "." + nodeVersion + ", expected size " + expectedSize + ", got " + size);
						}
				
						if(expectedMimeType != null && !expectedMimeType.equals(mimeType))
						{
							logger.warn("For node " + nodeId + "." + nodeVersion + ", expected mimeType " + expectedMimeType + ", got " + mimeType);
						}

						final String contentPath = updateContent(nodeId, nodeInternalId, nodeVersion, nodePath, content);

						TransformationCallback callback = new TransformationCallback()
						{
							@Override
							public void transformCompleted(TransformResponse response)
							{
								try
								{
									List<ContentReference> targets = response.getTargets();
									if(targets.size() > 0)
									{
										long transformDuration = response.getTimeTaken();
										ContentReference target = targets.get(0);
										String targetPath = target.getPath();

										logger.debug("Transformed " + nodeId + "." + nodeVersion
												+ ", " + contentPath + " to text " + targetPath);

										File file = new File(targetPath);
										long size = file.length();
										NodeInfo nodeInfo = NodeInfo.start()
												.setNodeId(nodeId)
												.setNodeVersion(nodeVersion)
												.setNodeInternalId(nodeInternalId)
												.setNodePath(nodePath)
												.setContentPath(targetPath)
												.setMimeType("text/plain")
												.setSize(size);
										nodeInfo.setTransformDuration(transformDuration);
										nodeInfo.setPrimary(false);
										contentDAO.updateNode(nodeInfo);

										Node node = Node.build()
												.nodeId(nodeId)
												.nodeInternalId(nodeInternalId)
												.nodeVersion(nodeVersion);
										entitiesService.getEntities(node);
									}
								}
								catch(AuthenticationException | IOException e)
								{
									logger.error(e);
								}
							}
							
							@Override
							public void onError(TransformRequest request, Throwable e)
							{
								logger.error("Transform failed: " + nodeId + "." + nodeVersion
										+ ", " + nodePath + ", " + contentPath, e);
							}
						};
						MimeType mt = MimeType.INSTANCES.get(mimeType);
						transformService.transform(contentPath, mt, callback);
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

	public void nodeAdded(String nodeId, long nodeInternalId, String nodeVersion, String nodePath) throws IOException
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
				NodeInfo nodeInfo = NodeInfo.start()
						.setNodeId(nodeId)
						.setNodeInternalId(nodeInternalId)
						.setNodeVersion(nodeVersion)
						.setNodePath(nodePath);
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
				// TODO remove transformed content?
				NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion, true);
		
				String contentPath = nodeInfo.getContentPath();
				if(contentPath != null)
				{
					contentStore.remove(contentPath);
				}
			}
		}
	}

	private Content getContent(NodeInfo nodeInfo) throws IOException
	{
		String nodeId = nodeInfo.getNodeId();
		String nodeVersion = nodeInfo.getNodeVersion();
		String contentPath = nodeInfo.getContentPath();

		InputStream in = null;
		if(contentPath != null)
		{
			in = contentStore.getContent(contentPath);
			UserDetails userDetails = UserContext.getUser();
			String username = userDetails.getUsername();
			NodeUsage nodeUsage = new NodeUsage(nodeId, nodeVersion, System.currentTimeMillis(), username);
			contentDAO.addUsage(nodeUsage);
		}

		String mimeType = nodeInfo.getMimeType();
		Long size = nodeInfo.getSize();

		Content content = new Content(in, mimeType, size);
		return content;
	}

//	private Content updateContent(String nodeId, String nodeVersion, String nodePath, Content content, String username) throws IOException
//	{
//		Content ret = null;
//
//		InputStream in = content.getIn();
//		if(in != null)
//		{
//			try
//			{
//				String contentPath = updateContent(nodeId, nodeVersion, nodePath, content);
//
//				if(contentPath != null)
//				{
//					NodeUsage nodeUsage = new NodeUsage(nodeId, nodeVersion, System.currentTimeMillis(), username);
//					contentDAO.addUsage(nodeUsage);
//
//					InputStream in1 = contentStore.getContent(contentPath);
//					ret = new Content(in1, content.getMimeType(), content.getSize());
//				}
//				else
//				{
//					// TODO
//				}
//			}
//			finally
//			{
//				in.close();
//			}
//		}
//		else
//		{
//			logger.warn("No content for node with path " + nodePath);
//		}
//
//		return ret;
//	}

	public Content getByNodePath(String nodePath) throws IOException
	{
		Content content = null;

		NodeInfo nodeInfo = contentDAO.getByNodePath(nodePath);
		if(nodeInfo != null)
		{
			content = getContent(nodeInfo);
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
					String contentPath = updateContent(nodeId, null, nodeVersion, nodePath, repoContent);
					sendContentAvailableMessage(nodeId, null, nodeVersion, nodePath, mimeType, size);
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

	public Content getByNodeId(String nodeId, String nodeVersion) throws IOException
	{
		Content content = null;

		NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion, true);
		if(nodeInfo != null)
		{
			content = getContent(nodeInfo);
		}
		else
		{
			try
			{
				// lazily get the content from the repo
				String nodePath = alfrescoApi.getPrimaryNodePathForNodeId(nodeId, nodeVersion);
				content = contentGetter.getContent(nodeId, nodeVersion);
				if(content != null)
				{
					String contentPath = updateContent(nodeId, null, nodeVersion, nodePath, content);
					sendContentAvailableMessage(nodeId, null, nodeVersion, nodePath, content.getMimeType(), content.getSize());
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
