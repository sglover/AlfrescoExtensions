/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.serializers.types.SerializerRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * Builds an hierarchical representation of node metadata:
 * 
 * <className> : { properties }
 * | <aspectName> : { properties }
 * 
 * @author sglover
 *
 */
public class HierarchicalNodeMetadataSerializer extends AbstractNodeMetadataSerializer
{
    private SerializerRegistry serializerRegistry;

    public HierarchicalNodeMetadataSerializer(DictionaryService dictionaryService,
            NamespaceService namespaceService,
            SerializerRegistry serializerRegistry, Files files,
            PropertySerializer propertySerializer)
    {
        super(dictionaryService, namespaceService, files, propertySerializer);
        this.serializerRegistry = serializerRegistry;
    }

    protected DBObject buildDeleteProperties(NodeVersionKey nodeVersionKey, Set<String> qnames)
    {
        if (qnames == null || qnames.isEmpty())
        {
            throw new IllegalArgumentException();
        }
        else
        {
            BasicDBObjectBuilder updateBuilder = BasicDBObjectBuilder
                .start();
            BasicDBObjectBuilder propertiesBuilder = BasicDBObjectBuilder
                    .start();
            for (String propertyName : qnames)
            {
            	QName propertyQName = serializerRegistry.deserialize(QName.class, propertyName);

                PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
                ClassDefinition classDef = propDef.getContainerClass();
                QName aspectQName = classDef.getName();
                String className = serializerRegistry.serialize(aspectQName).toString();
                propertiesBuilder.add(className + "." + propertyName, "");
            }
            updateBuilder.add("$unset", propertiesBuilder.get());

            return updateBuilder.get();
        }
    }

    protected DBObject buildDeleteAspects(NodeVersionKey nodeVersionKey, Set<String> aspects)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        if (aspects == null || aspects.size() == 0)
        {
        	builder.add("$set", new BasicDBObject(CLASSES, new BasicDBObject()));

            return builder.get();
        }
        else
        {
            BasicDBObjectBuilder aspectsBuilder = BasicDBObjectBuilder
                    .start();
            for(String aspect : aspects)
            {
            	QName aspectQName = serializerRegistry.deserialize(QName.class, aspect);

                String className = serializerRegistry.serialize(aspectQName).toString();
                aspectsBuilder.add(className, "");
            }
        	builder.add("$unset", aspectsBuilder.get());
        }

        return builder.get();
    }

    protected DBObject buildPropertiesUpdate(NodeVersionKey nodeVersionKey, Map<String, Serializable> persistableProps)
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        for (Map.Entry<String, Serializable> entry : persistableProps.entrySet())
        {
        	String propertyName = entry.getKey();

            QName propertyQName = serializerRegistry.deserialize(QName.class, propertyName);
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);

            String clazz = "residual";
            if(propDef != null)
            {
                ClassDefinition classDef = propDef.getContainerClass();
                if(classDef != null)
                {
                    QName classQName = classDef.getName();
                    clazz = serializerRegistry.serialize(classQName).toString();
                }
            }

            Serializable value = entry.getValue();
            Object propVal = propertySerializer.serialize(propertyQName, value);

            builder.add(CLASSES + "." + clazz + "." + propertyName, propVal);
        }

        return builder.get();
    }

    protected void buildNodeMetadata(BasicDBObjectBuilder builder,
            NodeVersionKey nodeVersionKey, String changeTxnId, Long txnId, QName nodeTypeQName,
            Map<QName, Serializable> props, Set<QName> aspects)
    {
        String nodeType = serializerRegistry.serialize(nodeTypeQName).toString();

        Map<String, BasicDBObjectBuilder> classBuilders = new HashMap<>();

        Set<QName> classesToAdd = new HashSet<>();
        if(aspects != null)
        {
        	classesToAdd.addAll(aspects);
        }
        classesToAdd.add(nodeTypeQName);

        for(Map.Entry<QName, Serializable> entry : props.entrySet())
        {
        	QName propertyQName = entry.getKey();
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            Serializable propertyValue = entry.getValue();

            String className = "residual";
            if(propDef != null)
            {
                ClassDefinition classDef = propDef.getContainerClass();
                if(classDef != null)
                {
                    QName classQName = classDef.getName();
                    classesToAdd.remove(classQName);
                    className = serializerRegistry.serialize(classQName).toString();
                }
            }

            String propName = buildPropertyName(propertyQName);
            Object propValue = propertySerializer.serialize(propertyQName, propertyValue);

            BasicDBObjectBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = BasicDBObjectBuilder.start();
                classBuilders.put(className, classBuilder);
            }

            classBuilder.add(propName, propValue);
        }

        for(QName classQName : classesToAdd)
        {
        	String className = (String)serializerRegistry.serialize(classQName);
            BasicDBObjectBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = BasicDBObjectBuilder.start();
                classBuilders.put(className, classBuilder);
            }
        }

        BasicDBObjectBuilder setterBuilder = BasicDBObjectBuilder
                .start();
        for(Map.Entry<String, BasicDBObjectBuilder> classBuilderEntry : classBuilders.entrySet())
        {
            setterBuilder.add(classBuilderEntry.getKey(), classBuilderEntry.getValue().get());
        }

        DBObject setter = setterBuilder.get();
        builder.add(NODE_TYPE, nodeType).add(CLASSES, setter);    	
    }

    @Override
    public void buildNodeMetadata(BasicDBObjectBuilder builder,
            NodeVersionKey nodeVersionKey, String changeTxnId, Long txnId, String nodeType,
            Map<String, Serializable> props, Set<String> aspects)
    {
        Map<String, BasicDBObjectBuilder> classBuilders = new HashMap<>();

        Set<String> classesToAdd = new HashSet<>();
        if(aspects != null)
        {
        	classesToAdd.addAll(aspects);
        }
        classesToAdd.add(nodeType);

        for(Map.Entry<String, Serializable> entry : props.entrySet())
        {
        	String propertyName = entry.getKey();
            QName propertyQName = serializerRegistry.deserialize(QName.class, propertyName);
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            Serializable propertyValue = entry.getValue();

            String className = "residual";
            if(propDef != null)
            {
                ClassDefinition classDef = propDef.getContainerClass();
                if(classDef != null)
                {
                    QName classQName = classDef.getName();
                    classesToAdd.remove(classQName);
                    className = serializerRegistry.serialize(classQName).toString();
                }
            }

            BasicDBObjectBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = BasicDBObjectBuilder.start();
                classBuilders.put(className, classBuilder);
            }

            propertySerializer.serialize(propertyQName, propertyValue, classBuilder);
        }

        for(String className : classesToAdd)
        {
            BasicDBObjectBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = BasicDBObjectBuilder.start();
                classBuilders.put(className, classBuilder);
            }
        }

        BasicDBObjectBuilder setterBuilder = BasicDBObjectBuilder
                .start();
        for(Map.Entry<String, BasicDBObjectBuilder> classBuilderEntry : classBuilders.entrySet())
        {
            setterBuilder.add(classBuilderEntry.getKey(), classBuilderEntry.getValue().get());
        }

        DBObject setter = setterBuilder.get();
        builder.add(NODE_TYPE, nodeType).add(CLASSES, setter);
    }

    @Override
    public void buildNodeMetadata(BasicDBObjectBuilder builder,
            String nodeId, String changeTxnId, String nodeType,
            Map<String, Serializable> props, Set<String> aspects)
    {
        Map<String, BasicDBObjectBuilder> classBuilders = new HashMap<>();

        Set<String> classesToAdd = new HashSet<>();
        if(aspects != null)
        {
        	classesToAdd.addAll(aspects);
        }
        classesToAdd.add(nodeType);

        for(Map.Entry<String, Serializable> entry : props.entrySet())
        {
        	String propertyName = entry.getKey();
            QName propertyQName = serializerRegistry.deserialize(QName.class, propertyName);
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            Serializable propertyValue = entry.getValue();

            String className = "residual";
            if(propDef != null)
            {
                ClassDefinition classDef = propDef.getContainerClass();
                if(classDef != null)
                {
                    QName classQName = classDef.getName();
                    classesToAdd.remove(classQName);
                    className = serializerRegistry.serialize(classQName).toString();
                }
            }

            BasicDBObjectBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = BasicDBObjectBuilder.start();
                classBuilders.put(className, classBuilder);
            }

            propertySerializer.serialize(propertyQName, propertyValue, classBuilder);
        }

        for(String className : classesToAdd)
        {
            BasicDBObjectBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = BasicDBObjectBuilder.start();
                classBuilders.put(className, classBuilder);
            }
        }

        BasicDBObjectBuilder setterBuilder = BasicDBObjectBuilder
                .start();
        for(Map.Entry<String, BasicDBObjectBuilder> classBuilderEntry : classBuilders.entrySet())
        {
            setterBuilder.add(classBuilderEntry.getKey(), classBuilderEntry.getValue().get());
        }

        DBObject setter = setterBuilder.get();
        builder.add(NODE_TYPE, nodeType).add(CLASSES, setter);
    }

    @Override
    public void buildNodeMetadata(XContentBuilder builder,
            NodeVersionKey nodeVersionKey, String changeTxnId, Long txnId, String nodeType,
            Map<String, Serializable> props, Set<String> aspects) throws IOException
    {
        Map<String, XContentBuilder> classBuilders = new HashMap<>();

        Set<String> classesToAdd = new HashSet<>();
        if(aspects != null)
        {
            classesToAdd.addAll(aspects);
        }
        classesToAdd.add(nodeType);

        for(Map.Entry<String, Serializable> entry : props.entrySet())
        {
            String propertyName = entry.getKey();
            QName propertyQName = serializerRegistry.deserialize(QName.class, propertyName);
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            Serializable propertyValue = entry.getValue();

            String className = "residual";
            if(propDef != null)
            {
                ClassDefinition classDef = propDef.getContainerClass();
                if(classDef != null)
                {
                    QName classQName = classDef.getName();
                    classesToAdd.remove(classQName);
                    className = serializerRegistry.serialize(classQName).toString();
                }
            }

            XContentBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = jsonBuilder();
                classBuilders.put(className, classBuilder);
            }

            propertySerializer.serialize(propertyQName, propertyValue, classBuilder);
        }

        for(String className : classesToAdd)
        {
            XContentBuilder classBuilder = classBuilders.get(className);
            if(classBuilder == null)
            {
                classBuilder = jsonBuilder();
                classBuilders.put(className, classBuilder);
            }
        }

        XContentBuilder setterBuilder = jsonBuilder();
        for(Map.Entry<String, XContentBuilder> classBuilderEntry : classBuilders.entrySet())
        {
            setterBuilder.field(classBuilderEntry.getKey(), classBuilderEntry.getValue());
        }

        builder.field(NODE_TYPE, nodeType).field(CLASSES, setterBuilder);
    }

    protected DBObject buildAspectUpdate(String aspect)
    {
        BasicDBObjectBuilder setterBuilder = BasicDBObjectBuilder.start();
        DBObject setter = setterBuilder.get();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("$set", setter);
        return builder.get();
    }

    protected DBObject buildAspectsUpdate(Set<String> aspects)
    {
        BasicDBObjectBuilder setterBuilder = BasicDBObjectBuilder.start();
        for(String aspect : aspects)
        {
            setterBuilder.add(CLASSES + "." + aspect, new BasicDBObject());
        }
        DBObject setter = setterBuilder.get();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("$set", setter);
        return builder.get();
    }

    protected void deserializeNodeProperties(Map<NodeVersionKey, Map<String, Serializable>> map, DBObject dbObject,
            Set<String> qnames)
    {
        Long nodeId = (Long)dbObject.get(NODE_ID);
        Long nodeVersion = (Long)dbObject.get(NODE_VERSION);
        if (nodeId == null || nodeVersion == null)
        {
            throw new RuntimeException("Expect results with a Node and Version: " + dbObject);
        }
        NodeVersionKey nodeKey = new NodeVersionKey(nodeId, nodeVersion);

        Map<String, Serializable> props = new HashMap<>(17);

        DBObject classesDBObject = (DBObject)dbObject.get(CLASSES);
        if(classesDBObject != null)
        {
            for(String className : classesDBObject.keySet())
            {
                DBObject propertiesDBObject = (DBObject)classesDBObject.get(className);
                if(propertiesDBObject != null)
                {
                    for(String key : propertiesDBObject.keySet())
                    {
                        int idx = key.indexOf(":");
                        if(idx == -1)
                        {
                            throw new AlfrescoRuntimeException("");
                        }
                        String prefix = key.substring(0, idx);
                        String localName = key.substring(idx + 1);
                        try
                        {
	                        QName propertyQName = QName.createQName(prefix, localName, namespaceService);
	                        Object propValue = propertiesDBObject.get(key);
	        
	                        //          Serializable propValue = (Serializable)dbObject.get("value");
	                        //
	                        //          GridFSDBFile out = myFS.findOne("");
	                        //          Object val = SerializationUtils.deserialize(out.getInputStream());
	
	                        if(qnames == null || qnames.size() == 0 || qnames.contains(propertyQName))
	                        {
	                            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
	                            Object value = getPropertyValue(propDef, propValue);
	                            String propertyName = (String)serializerRegistry.serialize(propertyQName);
	                            props.put(propertyName, (Serializable)value);
	                        }
                        }
                        catch(NamespaceException e)
                        {
                        	logger.warn("Unknown prefix " + prefix + ", skipping node properties for class " + className);
                        }
                    }
                }
            }
        }

        map.put(nodeKey, props);
    }

    protected Set<QName> deserializeNodeAspects(DBObject dbObject)
    {
        Set<QName> aspects = new HashSet<>();

        DBObject classes = (DBObject)dbObject.get(CLASSES);
        if(classes != null)
        {
            for(String className : classes.keySet())
            {
            	if(className.equals("residual"))
            	{
            		// no containing class
            		continue;
            	}

                int idx = className.indexOf(":");
                if(idx == -1)
                {
                    throw new AlfrescoRuntimeException("");
                }
                String prefix = className.substring(0, idx);
                String localName = className.substring(idx + 1);
                try
                {
                	QName classQName = QName.createQName(prefix, localName, namespaceService);
                    AspectDefinition aspectDef = dictionaryService.getAspect(classQName);
                    if(aspectDef != null)
                    {
                        aspects.add(classQName);
                    }
                }
                catch(NamespaceException e)
                {
                	logger.warn("Unknown prefix " + prefix + ", skipping node aspect " + className);
                }
            }
        }

        return aspects;
    }
}
