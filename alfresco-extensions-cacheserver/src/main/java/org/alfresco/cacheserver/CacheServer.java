/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.alfresco.cacheserver.content.ContentUpdater;
import org.alfresco.cacheserver.content.ContentUpdater.OperationType;
import org.alfresco.cacheserver.dao.WebsocketDAO;
import org.alfresco.cacheserver.dao.data.Registration;
import org.alfresco.checksum.Patch;
import org.alfresco.contentstore.AbstractContentStore;
import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.contentstore.dao.NodeInfo;
import org.alfresco.extensions.common.Content;
import org.alfresco.extensions.common.Node;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.ContentGetter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

/**
 * 
 * @author sglover
 *
 */
public class CacheServer
{
	private static Log logger = LogFactory.getLog(CacheServer.class);

	private AbstractContentStore contentStore;
	private ContentDAO contentDAO;
	private ContentGetter localContentGetter;
	private AlfrescoApi alfrescoApi;
	private ContentUpdater contentUpdater;
	private WebsocketDAO webSocketDAO;

	public CacheServer(ContentDAO contentDAO, AbstractContentStore contentStore,
			AlfrescoApi alfrescoApi, ContentGetter localContentGetter, ContentUpdater contentUpdater,
			WebsocketDAO webSocketDAO) throws IOException
	{
		this.contentStore = contentStore;
		this.contentDAO = contentDAO;
		this.localContentGetter = localContentGetter;
		this.alfrescoApi = alfrescoApi;
		this.contentUpdater = contentUpdater;
		this.webSocketDAO = webSocketDAO;
	}

	public void register(String ipAddress, String username)
	{
	    Registration registration = new Registration(ipAddress, username);
	    webSocketDAO.register(registration);
	}

	public void repoContentUpdated(final Node node, final String expectedMimeType, final Long expectedSize,
	        final boolean asyncChecksums) throws IOException
	{
		if(node.getNodeId() == null)
		{
			logger.warn("Null nodeId");
		}
		else
		{
			if(node.getVersionLabel() == null)
			{
				logger.warn("Ignoring " + node.getNodeId() + " with null nodeVersion");
			}
			else
			{
				try
				{
				    contentUpdater.updateContent(node, OperationType.Async, OperationType.Async, expectedMimeType,
				            expectedSize);
				}
				catch(CmisObjectNotFoundException e)
				{
					logger.warn("Node " + node + " not found");
				}
			}
		}
	}

	public void nodeAdded(Node node) throws IOException
	{
		if(node.getNodeId() == null)
		{
			logger.warn("Null nodeId");
		}
		else
		{
			if(node.getVersionLabel() == null)
			{
				logger.warn("Ignoring " + node.getNodeId() + " with null nodeVersion");
			}
			else
			{
				contentDAO.updateNode(NodeInfo.start(node));
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

/*	public Content getByNodePath(String nodePath, boolean asyncChecksums) throws IOException
	{
		Content content = localContentGetter.getContentByNodePath(nodePath);
		if(content == null)
		{
			NodeId fullNodeId = alfrescoApi.getObjectIdForNodePath(nodePath);
			String nodeId = fullNodeId.getNodeId();
			String nodeVersion = fullNodeId.getNodeVersion();
			try
			{
//				Content repoContent = remoteContentGetter.getContentByNodeId(nodeId, nodeVersion);
//				if(repoContent != null)
//				{
//					Node node = Node.build().nodeId(nodeId).versionLabel(nodeVersion);
//					contentUpdater.updateContent(node, content, asyncChecksums);
//				}
//				else
//				{
//					logger.warn("No content for node with path " + nodePath);
//				}
			    Node node = Node.build().nodeId(nodeId).versionLabel(nodeVersion);
                contentUpdater.updateContent(node, asyncChecksums, null, null);
			}
			catch(CmisObjectNotFoundException e)
			{
				logger.warn("Node " + nodeId + ";" + nodeVersion + " not found");
			}
		}

		return content;
	}*/

	public Content getByNodeId(String nodeId, String nodeVersion, boolean asyncChecksums) throws IOException
	{
		Content content = localContentGetter.getContentByNodeId(nodeId, nodeVersion);
		if(content == null)
		{
			try
			{
				// lazily get the content from the repo
				String nodePath = alfrescoApi.getPrimaryNodePathForNodeId(nodeId, nodeVersion);
				Node node = Node.build().nodeId(nodeId).versionLabel(nodeVersion).nodePath(nodePath);
				contentUpdater.updateContent(node, OperationType.Async, OperationType.Async, null, null);
			}
			catch(CmisObjectNotFoundException e)
			{
				logger.warn("Node " + nodeId + ";" + nodeVersion + " not found");
			}
		}

		return content;
	}

//	private PatchDocument getChecksums(String contentUrl) throws IOException
//	{
//		NodeChecksums checksums = checksumService.getChecksums(contentUrl);
//
//		// TODO buf size
//		FileChannel inChannel = contentStore.getChannel(contentUrl);
//		ByteBuffer buffer = ByteBuffer.allocate(1024*100);
//		inChannel.read(buffer);
//		try
//		{
//			PatchDocument patchDoc = checksumService.createPatchDocument(checksums, buffer);
//			return patchDoc;
//		}
//		finally
//		{
//			inChannel.close();
//		}
//	}
//
//	public MultiPart getChecksums(String nodeId, String nodeVersion) throws IOException
//	{
//		NodeInfo nodeInfo = contentDAO.getByNodeId(nodeId, nodeVersion, true);
//		String contentPath = nodeInfo.getContentPath();
//		PatchDocument patchDocument = getChecksums(contentPath);
//		MultipartBuilder multipartBuilder = new MultipartBuilder();
//		for(Patch patch : patchDocument.getPatches())
//		{
//			int lastMatchIndex = patch.getLastMatchIndex();
//			int size = patch.getSize();
//			InputStream is = new ByteArrayInputStream(patch.getBuffer());
//			multipartBuilder.add(lastMatchIndex, size, is);
//		}
//
//		return multipartBuilder.getMultiPart();
//	}

	public List<Patch> getPatches(String host, int port, String nodeId, long nodeVersion) throws MessagingException, IOException
	{
		List<Patch> patches = new LinkedList<>();

		StringBuilder sb = new StringBuilder();
		sb.append("/patch/");
		sb.append(nodeId);
		sb.append("/");
		sb.append(nodeVersion);
		String url = sb.toString();

		final ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);
		final Client client = Client.create(config);

		final WebResource resource = client.resource(url);
		final MimeMultipart response = resource.get(MimeMultipart.class);

		// This will iterate the individual parts of the multipart response
		for (int i = 0; i < response.getCount(); i++)
		{
		    final BodyPart part = response.getBodyPart(i);
		    System.out.printf(
		            "Embedded Body Part [Mime Type: %s, Length: %s]\n",
		            part.getContentType(), part.getSize());
		    InputStream in = part.getInputStream();
//		    Patch patch = new Patch();
//		    patches.add(patch);
		}

		return patches;
	}
}
