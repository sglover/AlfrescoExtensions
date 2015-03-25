/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.serializers.PropertyValue.ValueType;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class PropertySerializer
{
    protected static final Log logger = LogFactory.getLog(PropertySerializer.class);

    private Set<QName> NUMBER_TYPES;

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private NodePropertyHelper nodePropertyHelper;

    public PropertySerializer(DictionaryService dictionaryService, NamespaceService namespaceService)
    {
        this.dictionaryService = dictionaryService;
        this.namespaceService = namespaceService;
        this.nodePropertyHelper = new NodePropertyHelper();
        init();
    }

    private void init()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);

        NUMBER_TYPES = new HashSet<QName>(4);
        NUMBER_TYPES.add(DataTypeDefinition.DOUBLE);
        NUMBER_TYPES.add(DataTypeDefinition.FLOAT);
        NUMBER_TYPES.add(DataTypeDefinition.INT);
        NUMBER_TYPES.add(DataTypeDefinition.LONG);
    }

    private void makeNodePropertyValue(PropertyDefinition propertyDef, Serializable value,
            XContentBuilder builder) throws IOException
    {
        String propName = propertyDef.getName().toPrefixString(namespaceService);
        PropertyValue propertyValue = nodePropertyHelper.makeNodePropertyValue(propertyDef, value);
        switch(propertyValue.getPersistedValueType())
        {
        case NULL:
            break;
        case BOOLEAN:
            boolean boolValue = propertyValue.getBooleanValue();
            builder.field(propName, boolValue);
            break;
        case LONG:
            long longValue = propertyValue.getLongValue();
            builder.field(propName, longValue);
            break;
        case FLOAT:
            float floatValue = propertyValue.getFloatValue();
            builder.field(propName, floatValue);
            break;
        case DOUBLE:
            double doubleValue = propertyValue.getDoubleValue();
            builder.field(propName, doubleValue);
            break;
        case STRING:
            String stringValue = propertyValue.getStringValue();
            builder.field(propName, stringValue);
            break;
        case JSONOBJECT:
            JSON json = propertyValue.getJSON();
            XContentBuilder builder1 = json.getXContent();
            builder.field(propName, builder1);
            break;
//        case FIXED_POINT:
//            ret = propertyValue.getJSON();
//            break;
        case SERIALIZABLE:
            // TODO
//            Serializable s = propertyValue.getSerializableValue();
//            byte[] b = SerializationUtils.serialize(s);
//            ret = files.createFile(nodeId, nodeVersion, propertyQName.toString(), b);
            break;
        default:
            throw new AlfrescoRuntimeException("Unrecognised value type: "
                    + propertyValue.getPersistedType());
        }
    }

    private void makeNodePropertyValue(PropertyDefinition propertyDef, Serializable value,
            BasicDBObjectBuilder builder)
    {
        String propName = propertyDef.getName().toPrefixString(namespaceService);
        PropertyValue propertyValue = nodePropertyHelper.makeNodePropertyValue(propertyDef, value);
        switch(propertyValue.getPersistedValueType())
        {
        case NULL:
            break;
        case BOOLEAN:
            boolean boolValue = propertyValue.getBooleanValue();
            builder.append(propName, boolValue);
            break;
        case LONG:
            long longValue = propertyValue.getLongValue();
            builder.append(propName, longValue);
            break;
        case FLOAT:
            float floatValue = propertyValue.getFloatValue();
            builder.append(propName, floatValue);
            break;
        case DOUBLE:
            double doubleValue = propertyValue.getDoubleValue();
            builder.append(propName, doubleValue);
            break;
        case STRING:
            String stringValue = propertyValue.getStringValue();
            builder.append(propName, stringValue);
            break;
        case JSONOBJECT:
            JSON json = propertyValue.getJSON();
            DBObject builder1 = json.getDBObject();
            builder.append(propName, builder1);
            break;
//        case FIXED_POINT:
//            ret = propertyValue.getJSON();
//            break;
        case SERIALIZABLE:
            // TODO
//            Serializable s = propertyValue.getSerializableValue();
//            byte[] b = SerializationUtils.serialize(s);
//            ret = files.createFile(nodeId, nodeVersion, propertyQName.toString(), b);
            break;
        default:
            throw new AlfrescoRuntimeException("Unrecognised value type: "
                    + propertyValue.getPersistedType());
        }
    }

//    private Serializable makeNodePropertyValue(PropertyDefinition propertyDef, Serializable value)
//    {
//        Serializable ret = null;
//
//        PropertyValue propertyValue = nodePropertyHelper.makeNodePropertyValue(propertyDef, value);
//        switch(propertyValue.getPersistedValueType())
//        {
//        case NULL:
//            break;
//        case BOOLEAN:
//            ret = propertyValue.getBooleanValue();
//            break;
//        case LONG:
//            ret = propertyValue.getLongValue();
//            break;
//        case FLOAT:
//            ret = propertyValue.getFloatValue();
//            break;
//        case DOUBLE:
//            ret = propertyValue.getDoubleValue();
//            break;
//        case STRING:
//            ret = propertyValue.getStringValue();
//            break;
//        case JSONOBJECT:
//            JSON json = propertyValue.getJSON();
//            ret = json;
//            break;
////        case FIXED_POINT:
////            ret = propertyValue.getJSON();
////            break;
//        case SERIALIZABLE:
//            // TODO
////            Serializable s = propertyValue.getSerializableValue();
////            byte[] b = SerializationUtils.serialize(s);
////            ret = files.createFile(nodeId, nodeVersion, propertyQName.toString(), b);
//            break;
//        default:
//            throw new AlfrescoRuntimeException("Unrecognised value type: "
//                    + propertyValue.getPersistedType());
//        }
//
//        return ret;
//    }

    public Serializable serialize(QName propertyQName, Object value)
    {
    	Serializable ret = null;

        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);

        if(value != null)
        {
            QName propertyTypeQName;
            if (propertyDef == null) // property not recognised
            {
                // allow it for now - persisting excess properties can be useful sometimes
                propertyTypeQName = DataTypeDefinition.ANY;
            }
            else
            {
                propertyTypeQName = propertyDef.getDataType().getName();
            }

            // A property may appear to be multi-valued if the model definition is loose and
            // an unexploded collection is passed in. Otherwise, use the model-defined behaviour
            // strictly.
            boolean isMultiValued;
            if (propertyTypeQName.equals(DataTypeDefinition.ANY))
            {
                // It is multi-valued if required (we are not in a collection and the property is a new collection)
                isMultiValued = (value != null) && (value instanceof Collection<?>);
            }
            else
            {
                isMultiValued = propertyDef.isMultiValued();
            }

            // Handle different scenarios.
            // - Do we need to explode a collection?
            // - Does the property allow collections?
            if(value instanceof Collection<?>)
            {
                // We are not (yet) processing a collection and the property is a collection i.e. needs exploding
                // Check that multi-valued properties are supported if the property is a collection
                if (!isMultiValued)
                {
                    throw new DictionaryException("A single-valued property of this type may not be a collection: \n" +
                            "   Property: " + propertyDef + "\n" +
                            "   Type: " + propertyTypeQName + "\n" +
                            "   Value: " + value);
                }
                // We have an allowable collection.
                @SuppressWarnings("unchecked")
                Collection<Object> collectionValues = (Collection<Object>) value;

                LinkedList<Object> values = new LinkedList<>();
                for (Object collectionValueObj : collectionValues)
                {
                    if (collectionValueObj != null && !(collectionValueObj instanceof Serializable))
                    {
                        throw new IllegalArgumentException("Node properties must be fully serializable, "
                                + "including values contained in collections. \n" + "   Property: " + propertyDef + "\n"
                                + "\n" + "   Value:    " + collectionValueObj);
                    }
                    Serializable collectionValue = (Serializable) collectionValueObj;
                    values.add(collectionValue);
                }
                ret = values;
            }
            else
            {
                // We are either processing collection elements OR the property is not a collection
                // Collections of collections are only supported by type d:any
                if (value instanceof Collection<?> && !propertyTypeQName.equals(DataTypeDefinition.ANY))
                {
                    throw new DictionaryException(
                            "Collections of collections (Serializable) are only supported by type 'd:any': \n"
                                    + "   Property: " + propertyDef + "\n" + "   Type: " + propertyTypeQName + "\n"
                                    + "   Value: " + value);
                }
                ret = (Serializable)value;
            }
        }

        return ret;
    }

    public void serialize(QName propertyQName, Serializable value, XContentBuilder builder) throws IOException
    {
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
    	Serializable val = serialize(propertyQName, value);
    	makeNodePropertyValue(propertyDef, val, builder);
    }

    public void serialize(QName propertyQName, Serializable value, BasicDBObjectBuilder builder)
    {
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
    	Serializable val = serialize(propertyQName, value);
    	makeNodePropertyValue(propertyDef, val, builder);
    }

    public Map<String, Object> serialize(Map<QName, Serializable> properties)
	{
		Map<String, Object> ret = new HashMap<>();

    	for(Map.Entry<QName, Serializable> entry : properties.entrySet())
    	{
    	    QName propertyQName = entry.getKey();
    	    String propName = propertyQName.toPrefixString(namespaceService);
    	    Serializable propValue = entry.getValue();

    	    Object serializedPropValue = serialize(propertyQName, propValue);

    	    ret.put(propName, serializedPropValue);
    	}
    	
    	return ret;
	}

    public Serializable deserialize(QName propertyName, Object propValue)
    {
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyName);
        return deserialize(propertyDef, propValue);
    }

    @SuppressWarnings("unchecked")
    public Serializable deserialize(PropertyDefinition propertyDef, Object propValue)
    {
    	Serializable value = null;

        if(propValue instanceof Collection<?>)
        {
            LinkedList<Object> list = new LinkedList<>();
            value = list;
            Collection<Object> c = (Collection<Object>)propValue;
            for(Object o : c)
            {
                Object value1 = deserialize(propertyDef, o);
                list.add(value1);
            }
        }
        else
        {
            Serializable s = (Serializable)propValue;
            PropertyValue nodePropValue = new PropertyValue();
            if(s instanceof String)
            {
                nodePropValue.setStringValue((String)s);
                nodePropValue.setPersistedType(ValueType.STRING.getOrdinalNumber());
            }
            else if(s instanceof Boolean)
            {
                nodePropValue.setBooleanValue((Boolean)s);
                nodePropValue.setPersistedType(ValueType.BOOLEAN.getOrdinalNumber());
            }
            else if(s instanceof Double)
            {
                nodePropValue.setDoubleValue((Double)s);
                nodePropValue.setPersistedType(ValueType.DOUBLE.getOrdinalNumber());
            }
            else if(s instanceof Float)
            {
                nodePropValue.setFloatValue((Float)s);
                nodePropValue.setPersistedType(ValueType.FLOAT.getOrdinalNumber());
            }
            else if(s instanceof Long)
            {
                nodePropValue.setLongValue((Long)s);
                nodePropValue.setPersistedType(ValueType.LONG.getOrdinalNumber());
            }
            else if(s instanceof JSON)
            {
                nodePropValue.setJSON((JSON)s);
                nodePropValue.setPersistedType(ValueType.JSONOBJECT.getOrdinalNumber());
            }
            else
            {
                nodePropValue.setSerializableValue(s);
                nodePropValue.setPersistedType(ValueType.SERIALIZABLE.getOrdinalNumber());
            }

            // Do we definitely have MLText?
//            boolean isMLText = (propertyDef != null && propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT));

            // Get the local entry value
            Serializable entryValue = makeSerializableValue(propertyDef, nodePropValue);
            value = entryValue;
        }

        // Done
        return value;
    }

    public Serializable makeSerializableValue(PropertyDefinition propertyDef, PropertyValue propertyValue)
    {
        if (propertyValue == null)
        {
            return null;
        }
        // get property attributes
        final QName propertyTypeQName;
        if (propertyDef == null)
        {
            // allow this for now
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }
        try
        {
            Serializable value = propertyValue.getValue(propertyTypeQName);

            // done
            return value;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" +
                    "   property: " + (propertyDef == null ? "unknown" : propertyDef) + "\n" +
                    "   property value: " + propertyValue, e);
        }
    }

    public Map<String, Serializable> deserialize(Map<String, Serializable> serializedProperties)
    {
        Map<String, Serializable> properties = new HashMap<>();
        for(Map.Entry<String, Serializable> entry : serializedProperties.entrySet())
        {
        	String propName = entry.getKey();
        	Serializable propValue = entry.getValue();
        	QName propertyQName = QName.createQName(propName, namespaceService);

        	PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
        	Serializable deserializedPropValue = deserialize(propertyDef, propValue);

        	properties.put(propName, deserializedPropValue);
        }

        return properties;
    }
}