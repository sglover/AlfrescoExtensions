/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.content;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.cacheserver.CacheServer;
import org.alfresco.cacheserver.messages.MessagesService;
import org.alfresco.cacheserver.transform.TransformService;
import org.alfresco.contentstore.AbstractContentStore;
import org.alfresco.contentstore.ContentReader;
import org.alfresco.contentstore.ContentReference;
import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.contentstore.dao.NodeInfo;
import org.alfresco.extensions.transformations.api.TransformRequest;
import org.alfresco.extensions.transformations.api.TransformResponse;
import org.alfresco.extensions.transformations.client.TransformationCallback;
import org.alfresco.services.ContentGetter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.alfrescoextensions.common.MimeType;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.ChecksumService;
import org.sglover.checksum.NodeChecksums;
import org.sglover.entities.EntitiesService;

/**
 * 
 * @author sglover
 *
 */
public class ContentUpdaterImpl implements ContentUpdater
{
    private static Log logger = LogFactory.getLog(CacheServer.class);

    private ContentGetter remoteContentGetter;
    private EntitiesService entitiesService;
    private AbstractContentStore contentStore;
    private ContentDAO contentDAO;
    private ExecutorService executors = Executors.newFixedThreadPool(10);
    private MessagesService messagingService;
    private ChecksumService checksumService;
    private TransformService transformService;

    public ContentUpdaterImpl(AbstractContentStore contentStore, ContentDAO contentDAO,
            MessagesService messagingService, ChecksumService checksumService,
            EntitiesService entitiesService, ContentGetter remoteContentGetter,
            TransformService transformService)
    {
        super();
        this.contentStore = contentStore;
        this.contentDAO = contentDAO;
        this.messagingService = messagingService;
        this.checksumService = checksumService;
        this.entitiesService = entitiesService;
        this.remoteContentGetter = remoteContentGetter;
        this.transformService = transformService;
    }

    private NodeChecksums extractChecksums(final Node node, final ContentReader content, final String contentPath)
    {
        NodeChecksums checksums = checksumService.extractChecksums(node, contentPath);
        messagingService.sendContentAvailableMessage(node, content.getMimeType(), content.getSize(),
                contentPath, checksums);
        return checksums;
    }

    private void extractChecksumsAsync(final Node node, final ContentReader content, final String contentPath)
    {
        executors.submit(new Runnable()
        {
            @Override
            public void run()
            {
                extractChecksums(node, content, contentPath);
            }
        });
    }

    @Override
    public void updateContent(Node node, OperationType checksums, OperationType transforms,
            final String expectedMimeType,
            final Long expectedSize) throws IOException, CmisObjectNotFoundException
    {
        ContentReader content = remoteContentGetter.getContentByNodeId(node.getNodeId(), node.getVersionLabel());
        if(content != null)
        {
            final String mimeType = content.getMimeType();
            Long size = content.getSize();

            if(expectedSize != null && expectedSize.longValue() != size)
            {
                logger.warn("For node " + node + ", expected size " + expectedSize + ", got " + size);
            }
    
            if(expectedMimeType != null && !expectedMimeType.equals(mimeType))
            {
                logger.warn("For node " + node + ", expected mimeType " + expectedMimeType + ", got " + mimeType);
            }

            logger.debug("CacheServer updating content for node " + node
                    + ", " + content.getMimeType() + ", " + content.getSize());

            File outFile = contentStore.write(node, content);
            String contentPath = outFile.getAbsolutePath();

            switch(checksums)
            {
            case Async:
            {
                extractChecksumsAsync(node, content, contentPath);
                break;
            }
            case Sync:
            {
                extractChecksums(node, content, contentPath);
                break;
            }
            case None:
            default:
            }

//            NodeInfo nodeInfo = NodeInfo.start(node)
//                    .setContentPath(contentPath)
//                    .setMimeType(mimeType)
//                    .setSize(size);
//            contentDAO.updateNode(nodeInfo);

            TransformationCallback callback = new TransformationCallback()
            {
                @Override
                public void transformCompleted(TransformResponse response)
                {
                    try
                    {
                        List<ContentReference> targets = response.getTargets();
                        if(targets != null && targets.size() > 0)
                        {
                            long transformDuration = response.getTimeTaken();
                            ContentReference target = targets.get(0);
                            String targetPath = target.getPath();

                            logger.debug("Transformed " + node
                                    + ", " + contentPath + " to text " + targetPath);

                            File file = new File(targetPath);
                            long size = file.length();
                            NodeInfo nodeInfo = NodeInfo.start(node)
                                    .setContentPath(targetPath)
                                    .setMimeType("text/plain")
                                    .setSize(size);
                            nodeInfo.setTransformDuration(transformDuration);
                            nodeInfo.setPrimary(false);
                            contentDAO.updateNode(nodeInfo);

                            entitiesService.getEntities(node);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        logger.error(e);
                    }
                }
                
                @Override
                public void onError(TransformRequest request, Throwable e)
                {
                    logger.error("Transform failed: " + node + ", " + contentPath, e);
                }
            };
            MimeType mt = MimeType.INSTANCES.get(mimeType);

            switch(transforms)
            {
            case Async:
            {
                transformService.transformToTextAsync(contentPath, mt, callback);
                break;
            }
            case Sync:
            {
                transformService.transformToText(contentPath, mt, callback);
                break;
            }
            case None:
            default:
            }
        }
        else
        {
            logger.warn("No content for node " + node);
        }
    }
}
