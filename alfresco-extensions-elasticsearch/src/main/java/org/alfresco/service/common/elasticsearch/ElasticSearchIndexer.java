/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
import org.alfresco.serializers.DefaultFilesImpl;
import org.alfresco.serializers.Files;
import org.alfresco.serializers.HierarchicalNodeMetadataSerializer;
import org.alfresco.serializers.NodeMetadataSerializer;
import org.alfresco.serializers.NodeVersionKey;
import org.alfresco.serializers.PropertySerializer;
import org.alfresco.serializers.types.SerializerRegistry;
import org.alfresco.serializers.types.Serializers;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.AlfrescoDictionary;
import org.alfresco.services.Content;
import org.alfresco.services.TextContentGetter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.sglover.nlp.EntityExtracter;
import org.sglover.nlp.EntityTagger;

import com.mongodb.BasicDBObjectBuilder;

/**
 * 
 * @author sglover
 *
 */
public class ElasticSearchIndexer
{
    private static final Log logger = LogFactory
            .getLog(ElasticSearchIndexer.class);

    private ElasticSearchClient elasticSearchClient;

    private EntityTagger entityTagger;
    private EntityExtracter entityExtracter;

    private String indexName;

    private SerializerRegistry serializerRegistry;
    private PropertySerializer propertySerializer;
    private NodeMetadataSerializer nodeMetadataSerializer;
    private Files files;
    private Client client;
    private TextContentGetter contentGetter;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    public ElasticSearchIndexer(AlfrescoApi alfrescoApi,
            TextContentGetter contentGetter, EntityTagger entityTagger,
            EntityExtracter entityExtracter, Client client,
            AlfrescoDictionary alfrescoDictionary,
            ElasticSearchClient elasticSearchClient, String indexName)
            throws Exception
    {
        this.contentGetter = contentGetter;
        this.entityTagger = entityTagger;
        this.entityExtracter = entityExtracter;
        this.files = new DefaultFilesImpl();
        this.serializerRegistry = new Serializers();
        this.client = client;
        this.elasticSearchClient = elasticSearchClient;
        this.indexName = indexName;

        NamespaceService namespaceService = alfrescoDictionary
                .getNamespaceService();
        DictionaryService dictionaryService = alfrescoDictionary
                .getDictionaryService();

        this.propertySerializer = new PropertySerializer(dictionaryService,
                namespaceService);
        this.nodeMetadataSerializer = new HierarchicalNodeMetadataSerializer(
                dictionaryService, namespaceService, serializerRegistry, files,
                propertySerializer);
    }

    public void init(boolean checkIndexes)
    {
        if (!initialized.get())
        {
            try
            {
                elasticSearchClient.init(checkIndexes);

                initialized.set(true);
            }
            catch (Exception e)
            {
                throw new ElasticsearchException("", e);
            }
        }
    }

    public void shutdown()
    {
    }

    private String buildIndexId(NodeEvent nodeEvent)
    {
        long nodeInternalId = nodeEvent.getNodeInternalId();
        String versionLabel = getVersionLabel(nodeEvent);
        return buildIndexId(nodeInternalId, versionLabel);
    }

    private String buildIndexId(org.alfresco.events.types.NodeEvent nodeEvent)
    {
        return nodeEvent.getNodeId();
    }

    private String buildIndexId(long nodeInternalId, String versionLabel)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeInternalId);
        if (versionLabel != null)
        {
            sb.append(".");
            sb.append(versionLabel);
        }
        else
        {
            sb.append(".1.0");
        }

        return sb.toString();
    }

    private List<String> splitPath(String path)
    {
        String[] pathList = path.split("/");
        return Arrays.asList(pathList);
    }

    private AlfrescoNode makeAlfrescoNode(NodeEvent event)
    {
        AlfrescoNode node = new AlfrescoNode();

        long txnId = event.getTxnInternalId();
        node.setTxnId(txnId);

        String changeTxnId = event.getTxnId();
        node.setChangeTxnId(changeTxnId);

        node.setNodeId(event.getNodeId());
        node.setNodeInternalId(event.getNodeInternalId());
        node.setNodeVersion(event.getNodeVersion());

        Set<String> aspects = event.getAspects();
        node.setAspects(aspects);

        Map<String, Serializable> serializedProperties = event
                .getNodeProperties();
        Map<String, Serializable> properties = propertySerializer
                .deserialize(serializedProperties);

        node.setProperties(properties);

        long aclId = event.getAclId();
        node.setAclId(aclId);

        String nodeId = event.getNodeId();
        node.setNodeRef(nodeId);

        String primaryPath = event.getPaths().get(0);
        node.setPaths(splitPath(primaryPath));

        String type = event.getNodeType();
        node.setType(type);

        return node;
    }

    private AlfrescoNode makeAlfrescoNode(
            org.alfresco.events.types.NodeEvent event)
    {
        AlfrescoNode node = new AlfrescoNode();

        String changeTxnId = event.getTxnId();
        node.setChangeTxnId(changeTxnId);

        node.setNodeId(event.getNodeId());

        Set<String> aspects = event.getAspects();
        node.setAspects(aspects);

        Map<String, Serializable> serializedProperties = event
                .getNodeProperties();
        Map<String, Serializable> properties = propertySerializer
                .deserialize(serializedProperties);

        node.setProperties(properties);

        String nodeId = event.getNodeId();
        node.setNodeRef(nodeId);

        String primaryPath = event.getPaths().get(0);
        node.setPaths(splitPath(primaryPath));

        String type = event.getNodeType();
        node.setType(type);

        return node;
    }

    public void indexEvent(NodeEvent event) throws IOException
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        long nodeInternalId = event.getNodeInternalId();
        long versionId = 1l;

        String nodeId = event.getNodeId();

        String changeTxnId = event.getTxnId();
        Long txnId = event.getTxnInternalId();
        Long timestampMS = event.getTimestamp();
        String username = event.getUsername();
        List<String> paths = event.getPaths();
        String nodeType = event.getNodeType();

        String path = null;
        if (paths != null && paths.size() > 0)
        {
            path = paths.get(0);
        }
        String site = event.getSiteId();
        if (site == null)
        {
            site = "";
        }
        String networkId = event.getNetworkId();
        if (networkId == null)
        {
            networkId = "";
        }
        org.alfresco.repo.Client client = event.getClient();
        String clientId = (client != null ? client.getClientId() : null);
        if (clientId == null)
        {
            clientId = "";
        }

        builder.add("t", event.getType()).add("u", username)
                .add("ct", changeTxnId).add("tx", txnId)
                .add("tim", timestampMS).add("ti", timestampMS)
                .add("nid", nodeInternalId).add("v", versionId)
                .add("n", nodeId).add("s", site).add("ne", networkId)
                .add("c", clientId).add("nt", nodeType);
        if (path != null)
        {
            builder.add("p", path);
        }

        String id = event.getId();
        String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id,
                IndexType.event, json, true);

        logger.debug("Indexed event " + id + ", " + builder.get() + "response "
                + response.getId() + ", " + response.getType() + ", "
                + response.getIndex() + ", " + response.getVersion());
    }

    public void indexEvent(org.alfresco.events.types.NodeEvent event)
            throws IOException
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        String nodeId = event.getNodeId();

        String changeTxnId = event.getTxnId();
        Long timestampMS = event.getTimestamp();
        String username = event.getUsername();
        String site = event.getSiteId();
        List<String> paths = event.getPaths();
        String nodeType = event.getNodeType();

        if (site == null)
        {
            site = "";
        }
        String networkId = event.getNetworkId();
        if (networkId == null)
        {
            networkId = "";
        }
        org.alfresco.repo.Client client = event.getClient();
        String clientId = (client != null ? client.getClientId() : null);
        if (clientId == null)
        {
            clientId = "";
        }
        String path = null;
        if (paths != null && paths.size() > 0)
        {
            path = paths.get(0);
        }

        builder.add("t", event.getType()).add("u", username)
                .add("ct", changeTxnId).add("ti", timestampMS)
                .add("tim", timestampMS).add("n", nodeId).add("s", site)
                .add("ne", networkId).add("c", clientId).add("nt", nodeType);
        if (path != null)
        {
            builder.add("p", path);
        }

        String id = event.getId();
        String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id,
                IndexType.event, json, true);

        logger.debug("Indexed event " + id + ", " + builder.get() + "response "
                + response.getId() + ", " + response.getType() + ", "
                + response.getIndex() + ", " + response.getVersion());
    }

    public void indexNode(NodeEvent event) throws IOException
    {
        AlfrescoNode node = makeAlfrescoNode(event);

        long nodeInternalId = node.getNodeInternalId();
        String nodeId = node.getNodeId();
        long nodeVersion = node.getNodeVersion();
        String versionLabel = getVersionLabel(event);
        long timestamp = event.getTimestamp();
        String username = event.getUsername();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("n", nodeId)
                .add("nid", nodeInternalId).add("v", nodeVersion)
                .add("l", versionLabel).add("t", timestamp).add("u", username);

        NodeVersionKey nodeVersionKey = new NodeVersionKey(nodeInternalId,
                nodeVersion);
        String changeTxnId = node.getChangeTxnId();
        Long txnId = node.getTxnId();
        String nodeType = node.getType();
        Set<String> aspects = node.getAspects();
        Map<String, Serializable> properties = node.getProperties();
        // Map<String, Serializable> deserializedProperties =
        // propertySerializer.deserialize(serializedProperties);
        // nodeMetadataSerializer.buildNodeMetadata(builder, nodeVersionKey,
        // changeTxnId, txnId, nodeType,
        // deserializedProperties, aspects);
        nodeMetadataSerializer.buildNodeMetadata(builder, nodeVersionKey,
                changeTxnId, txnId, nodeType, properties, aspects);

        String id = buildIndexId(event);
        String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id,
                IndexType.node, json, true);

        logger.debug("Indexed node " + nodeId + ", " + builder.get()
                + "response " + response.getId() + ", " + response.getType()
                + ", " + response.getIndex() + ", " + response.getVersion());
    }

    public void indexNode(org.alfresco.events.types.NodeEvent event)
            throws IOException
    {
        AlfrescoNode node = makeAlfrescoNode(event);

        String nodeId = node.getNodeId();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("n", nodeId);

        String changeTxnId = node.getChangeTxnId();
        String nodeType = node.getType();
        Set<String> aspects = node.getAspects();
        Map<String, Serializable> properties = node.getProperties();
        nodeMetadataSerializer.buildNodeMetadata(builder, nodeId, changeTxnId,
                nodeType, properties, aspects);

        String id = buildIndexId(event);
        String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id,
                IndexType.node, json, true);

        logger.debug("Indexed node " + nodeId + ", " + builder.get()
                + "response " + response.getId() + ", " + response.getType()
                + ", " + response.getIndex() + ", " + response.getVersion());
    }

    public void unindexNode(NodeEvent event) throws IOException
    {
        AlfrescoNode node = makeAlfrescoNode(event);

        String nodeId = node.getNodeId();
        String id = buildIndexId(event);

        DeleteResponse response = elasticSearchClient.unindex(indexName, id,
                IndexType.node);

        logger.debug("Unindexed node " + nodeId + "response "
                + response.getId() + ", " + response.getType() + ", "
                + response.getIndex() + ", " + response.getVersion());
    }

    public void reindexNode(NodeUpdatedEvent event) throws IOException
    {
        AlfrescoNode node = makeAlfrescoNode(event);

        long nodeInternalId = node.getNodeInternalId();
        String nodeId = node.getNodeId();
        long nodeVersion = node.getNodeVersion();
        String versionLabel = getVersionLabel(event);

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("n", nodeId)
                .add("nid", nodeInternalId).add("v", nodeVersion)
                .add("l", versionLabel);

        NodeVersionKey nodeVersionKey = new NodeVersionKey(nodeInternalId,
                nodeVersion);
        String changeTxnId = node.getChangeTxnId();
        Long txnId = node.getTxnId();
        String nodeType = node.getType();
        Set<String> aspects = node.getAspects();
        Map<String, Serializable> serializedProperties = node.getProperties();
        Map<String, Serializable> deserializedProperties = propertySerializer
                .deserialize(serializedProperties);

        nodeMetadataSerializer.buildNodeMetadata(builder, nodeVersionKey,
                changeTxnId, txnId, nodeType, deserializedProperties, aspects);

        String json = builder.get().toString();

        String id = buildIndexId(event);

        UpdateResponse response = elasticSearchClient.reindex(indexName, id,
                IndexType.node, json, true);

        logger.debug("Re-indexed node " + id + ", " + nodeId + ", "
                + builder.get() + "response " + response.getId() + ", "
                + response.getType() + ", " + response.getIndex() + ", "
                + response.getVersion());
    }

    public void reindexNode(org.alfresco.events.types.NodeUpdatedEvent event)
            throws IOException
    {
        AlfrescoNode node = makeAlfrescoNode(event);
        String nodeId = node.getNodeId();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("n", nodeId);

        String changeTxnId = node.getChangeTxnId();
        String nodeType = node.getType();
        Set<String> aspects = node.getAspects();
        Map<String, Serializable> serializedProperties = node.getProperties();
        Map<String, Serializable> deserializedProperties = propertySerializer
                .deserialize(serializedProperties);

        nodeMetadataSerializer.buildNodeMetadata(builder, nodeId, changeTxnId,
                nodeType, deserializedProperties, aspects);

        String json = builder.get().toString();

        String id = buildIndexId(event);

        UpdateResponse response = elasticSearchClient.reindex(indexName, id,
                IndexType.node, json, true);

        logger.debug("Re-indexed node " + id + ", " + nodeId + ", "
                + builder.get() + "response " + response.getId() + ", "
                + response.getType() + ", " + response.getIndex() + ", "
                + response.getVersion());
    }

    private String getVersionLabel(NodeEvent nodeEvent)
    {
        String versionLabel = nodeEvent.getVersionLabel();
        return (versionLabel != null ? versionLabel : "1.0");
    }

    private String getContent(ReadableByteChannel channel) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        ByteBuffer bb = ByteBuffer.allocate(2048);
        int c = -1;
        do
        {
            c = channel.read(bb);
            bb.flip();
            bb.clear();
            sb.append(new String(bb.array(), "UTF-8"));
        }
        while (c != -1);

        String content = sb.toString();
        return content;
    }

    public void indexContent(NodeContentPutEvent event) throws IOException
    {
        String nodeId = event.getNodeId();
        long nodeVersion = event.getNodeVersion();
        long nodeInternalId = event.getNodeInternalId();
        String nodeType = event.getNodeType();
        String versionLabel = getVersionLabel(event);
        List<String> paths = event.getPaths();
        String path = null;
        if (paths != null && paths.size() > 0)
        {
            path = paths.get(0);
        }

        try
        {
//            GetTextContentResponse response = contentGetter.getContentByNodeId(nodeInternalId);
            Content content = contentGetter.getContentByNodeId(nodeInternalId);

            ReadableByteChannel channel = (content != null ? content.getChannel() : null);
            if (channel != null)
            {
                String str = getContent(channel);

                BasicDBObjectBuilder builder = BasicDBObjectBuilder
                        .start("n", nodeId).add("nid", nodeInternalId)
                        .add("t", nodeType).add("p", path)
                        // .add("_n", nodeInternalId)
                        .add("v", nodeVersion).add("l", versionLabel)
                        // .add("c_enc", base64);
                        .add("c", str);

                String id = buildIndexId(event);
                String json = builder.get().toString();

                UpdateResponse esResponse = elasticSearchClient.reindex(
                        indexName, id, IndexType.content, json, false);

                logger.debug("Re-indexed content " + nodeId + ", "
                        + builder.get() + "response " + esResponse.getId()
                        + ", " + esResponse.getType() + ", "
                        + esResponse.getIndex() + ", "
                        + esResponse.getVersion());
            }
            else
            {
                logger.debug("No content for " + nodeInternalId + "."
                        + nodeVersion);
            }
        }
        catch (Exception e)
        {
            throw new ElasticsearchException("", e);
        }
    }

    public void indexContent(org.alfresco.events.types.NodeContentPutEvent event)
            throws IOException
    {
        // Can't do this, we need the nodeInternalId and the event does not
        // provide it
    }

    public void unindexContent(NodeEvent event) throws IOException
    {
        AlfrescoNode node = makeAlfrescoNode(event);

        String nodeId = node.getNodeId();
        long nodeInternalId = node.getNodeInternalId();
        long nodeVersion = node.getNodeVersion();

        String id = buildIndexId(event);

        DeleteResponse response = elasticSearchClient.unindex(indexName, id,
                IndexType.content);

        logger.debug("Unindexed content id = " + id + ", nodeId = " + nodeId
                + ", nodeInternalId = " + nodeInternalId + ", nodeVersion = "
                + nodeVersion + "response " + response.getId() + ", "
                + response.getType() + ", " + response.getIndex() + ", "
                + response.getVersion());
    }

    // TODO this is done asynchronously and may fail, in which case the even
    // read will have completed
    // successfully. Need to address this.
//    public void indexEntities(NodeEvent nodeEvent)
//    {
//        if (nodeEvent instanceof NodeContentPutEvent)
//        {
//            long nodeInternalId = nodeEvent.getNodeInternalId();
//            long nodeVersion = nodeEvent.getNodeVersion();
//            String nodeType = nodeEvent.getNodeType();
//            String versionLabel = getVersionLabel(nodeEvent);
//            String indexId = buildIndexId(nodeEvent);
//            indexEntitiesForContent(nodeInternalId, nodeVersion, nodeType,
//                    versionLabel, indexId);
//        }
//        else if (nodeEvent instanceof NodeUpdatedEvent)
//        {
//            NodeUpdatedEvent nodeUpdatedEvent = (NodeUpdatedEvent) nodeEvent;
//            indexEntities(nodeUpdatedEvent);
//        }
//        else
//        {
//            // TODO
//        }
//    }
//
//    public void indexEntitiesForContent(long nodeInternalId, long nodeVersion,
//            String nodeType, String versionLabel, String indexId)
//    {
//        EntityTaggerCallback callback = new EntityTaggerCallbackImpl(client,
//                nodeInternalId, nodeVersion, versionLabel, indexId);
//        entityExtracter.getEntities(nodeInternalId, callback);
//    }

//    private void indexEntities(NodeUpdatedEvent nodeEvent)
//    {
//        long nodeInternalId = nodeEvent.getNodeInternalId();
//        long nodeVersion = nodeEvent.getNodeVersion();
//        String nodeType = nodeEvent.getNodeType();
//        String versionLabel = getVersionLabel(nodeEvent);
//        String indexId = buildIndexId(nodeEvent);
//        Map<String, Property> propertiesAdded = nodeEvent.getPropertiesAdded();
//        // EntityTaggerCallback callback = new EntityTaggerCallbackImpl(client,
//        // nodeInternalId, nodeVersion, versionLabel,
//        // indexId);
//        for (Map.Entry<String, Property> entry : propertiesAdded.entrySet())
//        {
//            Property property = entry.getValue();
//            Serializable value = property.getValue();
//            if (value instanceof String)
//            {
//                String content = (String) value;
//                EntityTaggerCallback callback = new EntityTaggerCallback()
//                {
//
//                    @Override
//                    public void onSuccess(Entities entities)
//                    {
//                        // TODO Auto-generated method stub
//
//                    }
//
//                    @Override
//                    public void onFailure(Throwable ex)
//                    {
//                        // TODO Auto-generated method stub
//
//                    }
//                };
//                entityTagger.getEntities(content, callback);
//            }
//        }
//    }

    // public double similarity(long nodeInternalId1, long nodeInternalId2)
    // throws Exception
    // {
    // Set<EntityType> entityTypes = new HashSet<>();
    // entityTypes.add(EntityType.locations);
    // Entities entities1 = entitiesService.entities(nodeInternalId1,
    // entityTypes);
    // Set<String> locations1 = entities1.getLocationsAsSet();
    // Entities entities2 = entitiesService.entities(nodeInternalId2,
    // entityTypes);
    // Set<String> locations2 = entities2.getLocationsAsSet();
    //
    // MinHash<String> minHash = new
    // MinHashImpl<String>(locations1.size()+locations2.size());
    // double similarity = minHash.similarity(locations1, locations2);
    // return similarity;
    // }
}
