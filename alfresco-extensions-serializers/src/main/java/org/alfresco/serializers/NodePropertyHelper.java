/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import java.io.Serializable;

import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides services for translating exploded properties
 * (as persisted in <b>alf_node_properties</b>) in the public form, which is a
 * <tt>Map</tt> of values keyed by their <tt>QName</tt>.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodePropertyHelper
{
    private static final Log logger = LogFactory.getLog(NodePropertyHelper.class);

    /**
     * Construct the helper with the appropriate DAOs and services
     */
    public NodePropertyHelper()
    {
    }

    /**
     * Helper method to convert the <code>Serializable</code> value into a full, persistable {@link NodePropertyValue}.
     * <p>
     * Where the property definition is null, the value will take on the {@link DataTypeDefinition#ANY generic ANY}
     * value.
     * <p>
     * Collections are NOT supported. These must be split up by the calling code before calling this method. Map
     * instances are supported as plain serializable instances.
     * 
     * @param propertyDef the property dictionary definition, may be null
     * @param value the value, which will be converted according to the definition - may be null
     * @return Returns the persistable property value
     */
    public PropertyValue makeNodePropertyValue(PropertyDefinition propertyDef, Serializable value)
    {
        // get property attributes
        final QName propertyTypeQName;
        if (propertyDef == null) // property not recognised
        {
            // allow it for now - persisting excess properties can be useful sometimes
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }
        try
        {
            PropertyValue propertyValue = null;
            propertyValue = new PropertyValue(propertyTypeQName, value);

            // done
            return propertyValue;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" +
                    "   property: " + (propertyDef == null ? "unknown" : propertyDef) + "\n" +
                    "   value: " + value + "\n" +
                    "   value type: " + value.getClass(),
                    e);
        }
    }

    /**
     * Extracts the externally-visible property from the persistable value.
     * 
     * @param propertyDef       the model property definition - may be <tt>null</tt>
     * @param propertyValue     the persisted property
     * @return                  Returns the value of the property in the format dictated by the property definition,
     *                          or null if the property value is null
     */
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
            // Handle conversions to and from ContentData
//            if (value instanceof ContentDataId)
//            {
//                // ContentData used to be persisted as a String and then as a Long.
//                // Now it has a special type to denote the ID
//                Long contentDataId = ((ContentDataId) value).getId();
//                ContentData contentData = contentDataDAO.getContentData(contentDataId).getSecond();
//                value = new ContentDataWithId(contentData, contentDataId);
//            }
//            else if ((value instanceof Long) && propertyTypeQName.equals(DataTypeDefinition.CONTENT))
//            {
//                Long contentDataId = (Long) value;
//                ContentData contentData = contentDataDAO.getContentData(contentDataId).getSecond();
//                value = new ContentDataWithId(contentData, contentDataId);
//            }
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
}
