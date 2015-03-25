/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers.types;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class ContentDataSerializer implements Serializer
{
	public ContentDataSerializer(SerializerRegistry registry)
	{
		registry.registerSerializer(ContentData.class.getName(), this);
	}

	@Override
	public Object serialize(Object object)
	{
	    Object ret = DefaultTypeConverter.INSTANCE.convert(BasicDBObject.class, object);
	    return ret;
	}

	@Override
	public Object deSerialize(Object object)
	{
		DBObject dbObject = (DBObject)object;

		Object ret = DefaultTypeConverter.INSTANCE.convert(ContentData.class, dbObject);
		return ret;
	}
}
