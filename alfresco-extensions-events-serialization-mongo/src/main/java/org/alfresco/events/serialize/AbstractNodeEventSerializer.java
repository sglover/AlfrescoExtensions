/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import static org.alfresco.events.serialize.Fields.FIELD_ALFRESCO_CLIENT_ID;
import static org.alfresco.events.serialize.Fields.FIELD_ASPECTS;
import static org.alfresco.events.serialize.Fields.FIELD_CLIENT_ID;
import static org.alfresco.events.serialize.Fields.FIELD_EVENT_TIMESTAMP;
import static org.alfresco.events.serialize.Fields.FIELD_ID;
import static org.alfresco.events.serialize.Fields.FIELD_NAME;
import static org.alfresco.events.serialize.Fields.FIELD_NETWORK_ID;
import static org.alfresco.events.serialize.Fields.FIELD_NODE_ID;
import static org.alfresco.events.serialize.Fields.FIELD_NODE_INTERNAL_ID;
import static org.alfresco.events.serialize.Fields.FIELD_NODE_MODIFICATION_TIME;
import static org.alfresco.events.serialize.Fields.FIELD_NODE_PROPERTIES;
import static org.alfresco.events.serialize.Fields.FIELD_NODE_TYPE;
import static org.alfresco.events.serialize.Fields.FIELD_NODE_VERSION_LABEL;
import static org.alfresco.events.serialize.Fields.FIELD_PARENT_NODE_IDS;
import static org.alfresco.events.serialize.Fields.FIELD_PRIMARY_PATH;
import static org.alfresco.events.serialize.Fields.FIELD_SEQ_NO;
import static org.alfresco.events.serialize.Fields.FIELD_SITE_ID;
import static org.alfresco.events.serialize.Fields.FIELD_TXN_ID;
import static org.alfresco.events.serialize.Fields.FIELD_TYPE;
import static org.alfresco.events.serialize.Fields.FIELD_USER_ID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.events.node.types.Event;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.repo.Client;
import org.alfresco.repo.Client.ClientType;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author steveglover
 *
 */
public abstract class AbstractNodeEventSerializer extends AbstractEventSerializer
{
    protected abstract DBObject serializeNodeEvent(NodeEvent event);

    public DBObject serialize(Event event)
    {
        if(!(event instanceof NodeEvent))
        {
            throw new IllegalArgumentException();
        }
        NodeEvent nodeEvent = (NodeEvent)event;
        return serializeNodeEvent(nodeEvent);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void populateNodeEvent(DBObject dbObject, NodeEvent nodeEvent)
    {
        String name = (String)dbObject.get(FIELD_NAME);
        nodeEvent.setName(name);
        String nodeId = (String)dbObject.get(FIELD_NODE_ID);
        nodeEvent.setNodeId(nodeId);

        Long nodeInternalId = (Long)dbObject.get(FIELD_NODE_INTERNAL_ID);
        nodeEvent.setNodeInternalId(nodeInternalId);

        String versionLabel = (String)dbObject.get(FIELD_NODE_VERSION_LABEL);
        nodeEvent.setVersionLabel(versionLabel);

        String alfrescoClientId = (String)dbObject.get(FIELD_ALFRESCO_CLIENT_ID);
        Client alfrescoClient = new Client(Client.ClientType.cmis, alfrescoClientId);
        nodeEvent.setClient(alfrescoClient);

        List<String> primaryPathList = (List)dbObject.get(FIELD_PRIMARY_PATH);
        if(primaryPathList != null)
        {
	        Path primaryPath = new Path(primaryPathList);
	        List<String> paths = new ArrayList<>(1);
	        paths.add(primaryPath.getPath());
	        nodeEvent.setPaths(paths);
        }

        List<List<String>> pathNodeIds = (List)dbObject.get(FIELD_PARENT_NODE_IDS);
        nodeEvent.setParentNodeIds(pathNodeIds);
        String siteId = (String)dbObject.get(FIELD_SITE_ID);
        nodeEvent.setSiteId(siteId);
        String userId = (String)dbObject.get(FIELD_USER_ID);
        nodeEvent.setUsername(userId);
        String networkId = (String)dbObject.get(FIELD_NETWORK_ID);
        nodeEvent.setNetworkId(networkId);
        String txnId = (String)dbObject.get(FIELD_TXN_ID);
        nodeEvent.setTxnId(txnId);
        String nodeType = (String)dbObject.get(FIELD_NODE_TYPE);
        nodeEvent.setNodeType(nodeType);
        String type = (String)dbObject.get(FIELD_TYPE);
        nodeEvent.setType(type);
        Long eventTime = (Long)dbObject.get(FIELD_EVENT_TIMESTAMP);
        nodeEvent.setTimestamp(eventTime);
        Long nodeModificationTime = (Long)dbObject.get(FIELD_NODE_MODIFICATION_TIME);
        nodeEvent.setNodeModificationTime(nodeModificationTime);
        String clientTypeStr = (String)dbObject.get(FIELD_CLIENT_ID);
        ClientType clientType = (clientTypeStr != null ? ClientType.valueOf(clientTypeStr) : null);
        String clientId = (String)dbObject.get(FIELD_ALFRESCO_CLIENT_ID);
        Client client = new Client(clientType, clientId);
        nodeEvent.setClient(client);
        List<String> aspectsList = (List<String>)dbObject.get(FIELD_ASPECTS);
        Set<String> aspects = (aspectsList != null ? new HashSet<>(aspectsList) : Collections.<String>emptySet());
        nodeEvent.setAspects(aspects);
        Map<String, Serializable> nodeProperties = (Map<String, Serializable>)dbObject.get(FIELD_NODE_PROPERTIES);
        nodeEvent.setNodeProperties(nodeProperties);
        Long seqNo = (Long)dbObject.get(FIELD_SEQ_NO);
        nodeEvent.setSeqNumber(seqNo);
    }

    protected void buildDBObjectFromNodeEvent(BasicDBObjectBuilder builder, NodeEvent nodeEvent)
    {
        Client client = nodeEvent.getClient();

        // TODO deal with secondary paths
        List<String> paths = nodeEvent.getPaths();
        Path path = (paths != null && paths.size() > 0 ? new Path(paths.get(0)) : null);

        String alfrescoClientId = (client != null ? client.getClientId() : null);
        ClientType clientType = (client != null ? client.getType() : null);

        builder
        .add(FIELD_EVENT_TIMESTAMP, nodeEvent.getTimestamp())
        .add(FIELD_ID, nodeEvent.getId())
        .add(FIELD_NAME, nodeEvent.getName())
        .add(FIELD_TXN_ID, nodeEvent.getTxnId())
        .add(FIELD_SEQ_NO, nodeEvent.getSeqNumber())
        .add(FIELD_TYPE, nodeEvent.getType().toString())
        .add(FIELD_NODE_ID, nodeEvent.getNodeId())
        .add(FIELD_NODE_INTERNAL_ID, nodeEvent.getNodeInternalId())
        .add(FIELD_NODE_VERSION_LABEL, nodeEvent.getVersionLabel())
        .add(FIELD_NETWORK_ID, nodeEvent.getNetworkId())
        .add(FIELD_SITE_ID, nodeEvent.getSiteId())
        .add(FIELD_USER_ID, nodeEvent.getUsername())
        .add(FIELD_CLIENT_ID, (clientType != null ? clientType.toString() : null))
        .add(FIELD_PRIMARY_PATH, (path != null ? path.getArrayPath() : new LinkedList<>()))
        .add(FIELD_PARENT_NODE_IDS, nodeEvent.getParentNodeIds())
        .add(FIELD_NODE_TYPE, nodeEvent.getNodeType())
        .add(FIELD_ASPECTS, nodeEvent.getAspects())
        .add(FIELD_NODE_PROPERTIES, nodeEvent.getNodeProperties())
        .add(FIELD_NODE_MODIFICATION_TIME, nodeEvent.getNodeModificationTime())
        .add(FIELD_ALFRESCO_CLIENT_ID, alfrescoClientId);
    }
}
