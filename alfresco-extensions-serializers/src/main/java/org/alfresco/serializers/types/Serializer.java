/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers.types;

/**
 * 
 * @author sglover
 *
 */
public interface Serializer
{
	Object serialize(Object object);
	Object deSerialize(Object object);
}
