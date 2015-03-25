/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import java.io.Serializable;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public abstract class AbstractNodeMetadataSerializer implements NodeMetadataSerializer
{
    protected Log logger = LogFactory.getLog(getClass());

    public static final String ASPECTS = "a";
    public static final String PROPERTIES = "p";
    public static final String NODE_ID = "n";
    public static final String TXN_ID = "t";
    public static final String CHANGE_TXN_ID = "tx";
    public static final String NODE_VERSION = "v";
    public static final String NODE_REF = "nr";
    public static final String CLASSES = "c";
    public static final String NODE_TYPE = "nt";

    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    protected Files files;
    protected PropertySerializer propertySerializer;
//    protected NodePropertyHelper nodePropertyHelper;

    public AbstractNodeMetadataSerializer(DictionaryService dictionaryService,
            NamespaceService namespaceService, Files files,
            PropertySerializer propertySerializer)
//            NodePropertyHelper nodePropertyHelper)
    {
        super();
        this.dictionaryService = dictionaryService;
        this.namespaceService = namespaceService;
        this.files = files;
        this.propertySerializer = propertySerializer;
//        this.nodePropertyHelper = nodePropertyHelper;
    }

    protected String buildPropertyName(QName propertyQName)
    {
        String propertyName = propertyQName.toPrefixString(namespaceService);
        return propertyName;
    }

//    protected Object buildPropertyValue(Long nodeId, Long nodeVersion, QName propertyQName, Serializable value)
//    {
//        Object ret = null;
//
//        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
//
//        QName propertyTypeQName;
//        if (propertyDef == null) // property not recognised
//        {
//            // allow it for now - persisting excess properties can be useful sometimes
//            propertyTypeQName = DataTypeDefinition.ANY;
//        }
//        else
//        {
//            propertyTypeQName = propertyDef.getDataType().getName();
//        }
//
//        if (value == null)
//        {
//            // The property is null. Null is null and cannot be massaged any other way.
//            PropertyValue propertyValue = nodePropertyHelper.makeNodePropertyValue(propertyDef, null);
//            switch(propertyValue.getPersistedValueType())
//            {
//            case NULL:
//                break;
//            case BOOLEAN:
//                ret = propertyValue.getBooleanValue();
//                break;
//            case LONG:
//                ret = propertyValue.getLongValue();
//                break;
//            case FLOAT:
//                ret = propertyValue.getFloatValue();
//                break;
//            case DOUBLE:
//                ret = propertyValue.getDoubleValue();
//                break;
//            case STRING:
//                ret = propertyValue.getStringValue();
//                break;
//            case JSONOBJECT:
//                ret = propertyValue.getJSON();
//                break;
//            case FIXED_POINT:
//                ret = propertyValue.getJSON();
//                break;
//            case SERIALIZABLE:
//                Serializable s = propertyValue.getSerializableValue();
//                byte[] b = SerializationUtils.serialize(s);
//                ret = files.createFile(nodeId, nodeVersion, propertyQName.toString(), b);
//            default:
//                throw new AlfrescoRuntimeException("Unrecognised value type: "
//                        + propertyValue.getPersistedType());
//            }
//        }
//        else
//        {
//            // A property may appear to be multi-valued if the model definition is loose and
//            // an unexploded collection is passed in. Otherwise, use the model-defined behaviour
//            // strictly.
//            boolean isMultiValued;
//            if (propertyTypeQName.equals(DataTypeDefinition.ANY))
//            {
//                // It is multi-valued if required (we are not in a collection and the property is a new collection)
//                isMultiValued = (value != null) && (value instanceof Collection<?>);
////                        && (collectionIndex == IDX_NO_COLLECTION);
//            }
//            else
//            {
//                isMultiValued = propertyDef.isMultiValued();
//            }
//
//            // Handle different scenarios.
//            // - Do we need to explode a collection?
//            // - Does the property allow collections?
//            if(value instanceof Collection<?>)
//            {
//                // We are not (yet) processing a collection and the property is a collection i.e. needs exploding
//                // Check that multi-valued properties are supported if the property is a collection
//                if (!isMultiValued)
//                {
//                    throw new DictionaryException("A single-valued property of this type may not be a collection: \n" +
//                            "   Property: " + propertyDef + "\n" +
//                            "   Type: " + propertyTypeQName + "\n" +
//                            "   Value: " + value);
//                }
//                // We have an allowable collection.
//                @SuppressWarnings("unchecked")
//                Collection<Object> collectionValues = (Collection<Object>) value;
//
//                List<Object> values = new LinkedList<>();
//                ret = values;
//                for (Object collectionValueObj : collectionValues)
//                {
//                    if (collectionValueObj != null && !(collectionValueObj instanceof Serializable))
//                    {
//                        throw new IllegalArgumentException("Node properties must be fully serializable, "
//                                + "including values contained in collections. \n" + "   Property: " + propertyDef + "\n"
//                                + /*"   Index:    " + collectionIndex + */"\n" + "   Value:    " + collectionValueObj);
//                    }
//                    Serializable collectionValue = (Serializable) collectionValueObj;
//                    values.add(collectionValue);
//                }
//            }
//            else
//            {
//                // We are either processing collection elements OR the property is not a collection
//                // Collections of collections are only supported by type d:any
//                if (value instanceof Collection<?> && !propertyTypeQName.equals(DataTypeDefinition.ANY))
//                {
//                    throw new DictionaryException(
//                            "Collections of collections (Serializable) are only supported by type 'd:any': \n"
//                                    + "   Property: " + propertyDef + "\n" + "   Type: " + propertyTypeQName + "\n"
//                                    + "   Value: " + value);
//                }
//
//                PropertyValue propertyValue = nodePropertyHelper.makeNodePropertyValue(propertyDef, value);
//                switch(propertyValue.getPersistedValueType())
//                {
//                case NULL:
//                    break;
//                case BOOLEAN:
//                    ret = propertyValue.getBooleanValue();
//                    break;
//                case LONG:
//                    ret = propertyValue.getLongValue();
//                    break;
//                case FLOAT:
//                    ret = propertyValue.getFloatValue();
//                    break;
//                case DOUBLE:
//                    ret = propertyValue.getDoubleValue();
//                    break;
//                case STRING:
//                    ret = propertyValue.getStringValue();
//                    break;
//                case JSONOBJECT:
//                    ret = propertyValue.getJSON();
//                    break;
//                case FIXED_POINT:
//                    ret = propertyValue.getJSON();
//                    break;
//                case SERIALIZABLE:
//                    Serializable s = propertyValue.getSerializableValue();
//                    byte[] b = SerializationUtils.serialize(s);
//                    ret = files.createFile(nodeId, nodeVersion, propertyQName.toString(), b);
//                    break;
//                default:
//                    throw new AlfrescoRuntimeException("Unrecognised value type: "
//                            + propertyValue.getPersistedType());
//                }
//            }
//        }
//
//        // Done
//        return ret;
//    }

    protected Serializable getPropertyValue(PropertyDefinition propertyDef, Object propValue)
    {
//    	Serializable value = null;

    	Serializable value = propertySerializer.deserialize(propertyDef, propValue);
//        if(propValue instanceof Collection<?>)
//        {
//        	LinkedList<Object> list = new LinkedList<>();
//            value = list;
//            Collection<Object> c = (Collection<Object>)propValue;
//            for(Object o : c)
//            {
//                Object value1 = getPropertyValue(propertyDef, o);
//                list.add(value1);
//            }
//        }
//        else
//        {
//            Serializable s = (Serializable)propValue;
//            PropertyValue nodePropValue = new PropertyValue();
//            if(s instanceof String)
//            {
//                nodePropValue.setStringValue((String)s);
//                nodePropValue.setPersistedType(ValueType.STRING.getOrdinalNumber());
//            }
//            else if(s instanceof Boolean)
//            {
//                nodePropValue.setBooleanValue((Boolean)s);
//                nodePropValue.setPersistedType(ValueType.BOOLEAN.getOrdinalNumber());
//            }
//            else if(s instanceof Double)
//            {
//                nodePropValue.setDoubleValue((Double)s);
//                nodePropValue.setPersistedType(ValueType.DOUBLE.getOrdinalNumber());
//            }
//            else if(s instanceof Float)
//            {
//                nodePropValue.setFloatValue((Float)s);
//                nodePropValue.setPersistedType(ValueType.FLOAT.getOrdinalNumber());
//            }
//            else if(s instanceof Long)
//            {
//                nodePropValue.setLongValue((Long)s);
//                nodePropValue.setPersistedType(ValueType.LONG.getOrdinalNumber());
//            }
//            else if(s instanceof BasicDBObject)
//            {
//                nodePropValue.setJSON((DBObject)s);
//            }
//            else
//            {
//                nodePropValue.setSerializableValue(s);
//                nodePropValue.setPersistedType(ValueType.SERIALIZABLE.getOrdinalNumber());
//            }
//
//            // Do we definitely have MLText?
//            boolean isMLText = (propertyDef != null && propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT));
//
//            // Get the local entry value
//            Serializable entryValue = nodePropertyHelper.makeSerializableValue(propertyDef, nodePropValue);
//
//            if(isMLText)
//            {
//                value = new MLText();
//
//                // Get the locale of the current value
//                DBObject dbObject = (DBObject)entryValue;
//                for(String languageTag : dbObject.keySet())
//                {
//                    Locale locale = Locale.forLanguageTag(languageTag);
//                    String text = (String)dbObject.get(languageTag);
//
//                    // Put the current value into the MLText object
//                    // Can put in nulls and Strings
//                    ((MLText)value).put(locale, text);
//                }
//            }
//            else
//            {
//                value = entryValue;
//            }
//        }

        // Done
        return value;
    }
}
