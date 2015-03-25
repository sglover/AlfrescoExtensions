/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.serializers.types.SerializerRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class DefaultNodeMetadataSerializer {
//    extends AbstractNodeMetadataSerializer
//{
//    private SerializerRegistry serializerRegistry;
//
//    public DefaultNodeMetadataSerializer(DictionaryService dictionaryService,
//            NamespaceService namespaceService, Files files,
//            PropertySerializer propertySerializer)
//    {
//        super(dictionaryService, namespaceService, files, propertySerializer);
//    }
//
//    protected DBObject buildDeleteProperties(NodeVersionKey nodeVersionKey, Set<String> propertyNames)
//    {
//        BasicDBObjectBuilder updateBuilder = BasicDBObjectBuilder
//                .start();
//        if (propertyNames == null || propertyNames.isEmpty())
//        {
//            throw new IllegalArgumentException();
//        }
//        else
//        {
//            BasicDBObjectBuilder propertiesBuilder = BasicDBObjectBuilder
//                    .start();
//            for (String propertyName : propertyNames)
//            {
//                propertiesBuilder.add(propertyName, "");
//            }
//            updateBuilder.add("$unset", propertiesBuilder.get());
//        }
//
//        return updateBuilder.get();
//    }
//
//    private DBObject buildPropertiesInsertWithQNames(NodeVersionKey nodeVersionKey, Map<QName, Serializable> persistableProps)
//    {
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//
//        for (Map.Entry<QName, Serializable> entry : persistableProps.entrySet())
//        {
//        	QName propertyQName = entry.getKey();
//        	String propertyName = (String)serializerRegistry.serialize(propertyQName);
//            Serializable value = entry.getValue();
//
//            Object propVal = propertySerializer.serialize(propertyQName, value);
////            Object propVal = buildPropertyValue(nodeVersionKey.getNodeId(), nodeVersionKey.getVersion(), propertyQName, value);
//            builder.add(PROPERTIES + "." + propertyName, propVal);
//        }
//
//        return builder.get();
//    }
//
//    private DBObject buildPropertiesInsert(NodeVersionKey nodeVersionKey, Map<String, Serializable> persistableProps)
//    {
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//
//        for (Map.Entry<String, Serializable> entry : persistableProps.entrySet())
//        {
//        	String propertyName = entry.getKey();
//            Serializable value = entry.getValue();
//
//            QName propertyQName = serializerRegistry.deserialize(QName.class, propertyName);
//            Object propVal = propertySerializer.serialize(propertyQName, value);
////            Object propVal = buildPropertyValue(nodeVersionKey.getNodeId(), nodeVersionKey.getVersion(), propertyQName, value);
//            builder.add(PROPERTIES + "." + propertyName, propVal);
//        }
//
//        return builder.get();
//    }
//
////    protected DBObject buildPropertiesUpdate(NodeVersionKey nodeVersionKey, Map<String, Serializable> persistableProps)
////    {
////        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
////
////        for (Map.Entry<String, Serializable> entry : persistableProps.entrySet())
////        {
////            String propertyName = entry.getKey();
////            Serializable value = entry.getValue();
////
////            QName propertyQName = serializerRegistry.deserialize(QName.class, propertyName);
////            Object propVal = buildPropertyValue(nodeVersionKey.getNodeId(), nodeVersionKey.getVersion(), propertyQName, value);
////            builder.add(PROPERTIES + "." + propertyName, propVal);
////        }
////
////        return builder.get();
////    }
//
////    protected void buildNodeMetadata(BasicDBObjectBuilder builder,
////            NodeVersionKey nodeVersionKey, String changeTxnId, Long txnId, QName nodeTypeQName,
////            Map<QName, Serializable> props, Set<QName> aspects)
////    {
////        DBObject propsSetter = buildPropertiesInsertWithQNames(nodeVersionKey, props);
////        String nodeType = (String)serializerRegistry.serialize(nodeTypeQName);
////        builder
////            .add(NODE_TYPE, nodeType)
////            .add(PROPERTIES, propsSetter)
////            .add(ASPECTS, aspects);
////    }
//
//    @Override
//    public void buildNodeMetadata(BasicDBObjectBuilder builder,
//            NodeVersionKey nodeVersionKey, String changeTxnId, Long txnId, String nodeType,
//            Map<String, Serializable> props, Set<String> aspects)
//    {
//        DBObject propsSetter = buildPropertiesInsert(nodeVersionKey, props);
//        builder
//            .add(NODE_TYPE, nodeType)
//            .add(PROPERTIES, propsSetter)
//            .add(ASPECTS, aspects);
//    }
//
//    protected DBObject buildAspectUpdate(String aspect)
//    {
//    	// TODO
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//
////        List<Object> aspects = buildAspectsInsert(aspectQNames);
////        builder.add("$push", new BasicDBObject(MongoNodeDAO.ASPECTS, aspects));
//
//        return builder.get();
//    }
//
//    protected DBObject buildAspectsUpdate(Set<String> aspects)
//    {
//    	// TODO
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//
////        List<Object> aspects = buildAspectsInsert(aspectQNames);
//        builder.add("$push", new BasicDBObject(ASPECTS, aspects));
//
//        return builder.get();
//    }
//
//    protected void deserializeNodeProperties(Map<NodeVersionKey, Map<String, Serializable>> map, DBObject dbObject,
//            Set<String> qnames)
//    {
//        Long nodeId = (Long)dbObject.get(NODE_ID);
//        Long nodeVersion = (Long)dbObject.get(NODE_VERSION);
//        if (nodeId == null || nodeVersion == null)
//        {
//            throw new RuntimeException("Expect results with a Node and Version: " + dbObject);
//        }
//        NodeVersionKey nodeKey = new NodeVersionKey(nodeId, nodeVersion);
//
//        Map<String, Serializable> props = new HashMap<>(17);
//
//        DBObject propertiesDBObject = (DBObject)dbObject.get(PROPERTIES);
//        if(propertiesDBObject != null)
//        {
//            for(String key : propertiesDBObject.keySet())
//            {
//                int idx = key.indexOf(":");
//                if(idx == -1)
//                {
//                    throw new AlfrescoRuntimeException("");
//                }
//                String prefix = key.substring(0, idx);
//                String localName = key.substring(idx + 1);
//                QName propertyQName = QName.createQName(prefix, localName, namespaceService);
//                Object propValue = propertiesDBObject.get(key);
//
//                //          Serializable propValue = (Serializable)dbObject.get("value");
//                //
//                //          GridFSDBFile out = myFS.findOne("");
//                //          Object val = SerializationUtils.deserialize(out.getInputStream());
//
//                if(qnames == null || qnames.size() == 0 || qnames.contains(propertyQName))
//                {
//                    PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
//                    Object value = getPropertyValue(propDef, propValue);
//                    String propertyName = (String)serializerRegistry.serialize(propertyQName);
//                    props.put(propertyName, (Serializable)value);
//                }
//            }
//        }
//
//        map.put(nodeKey, props);
//    }
//
//    @SuppressWarnings("unchecked")
//    protected Set<QName> deserializeNodeAspects(DBObject dbObject)
//    {
//        Set<QName> aspects = new HashSet<>();
//
//        List<String> aspectsDBObject = (List<String>) dbObject.get(ASPECTS);
//        if (aspectsDBObject != null)
//        {
//            for (String aspect : aspectsDBObject)
//            {
//                int idx = aspect.indexOf(":");
//                if (idx == -1)
//                {
//                    throw new AlfrescoRuntimeException("Invalid aspect "
//                            + aspect);
//                }
//                String prefix = aspect.substring(0, idx);
//                String localName = aspect.substring(idx + 1);
//                QName aspectQName = QName.createQName(prefix, localName,
//                        namespaceService);
//                aspects.add(aspectQName);
//            }
//        }
//
//        return aspects;
//    }
//
//	protected DBObject buildDeleteAspects(NodeVersionKey nodeVersionKey,
//            Set<String> aspects)
//    {
//	    // TODO Auto-generated method stub
//	    throw new UnsupportedOperationException();
//    }
}
