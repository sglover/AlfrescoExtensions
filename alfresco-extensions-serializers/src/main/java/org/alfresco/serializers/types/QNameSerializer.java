/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers.types;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author sglover
 *
 */
public class QNameSerializer implements Serializer
{
	private NamespaceService namespaceService;

	public QNameSerializer(SerializerRegistry registry, NamespaceService namespaceService)
	{
		registry.registerSerializer(QName.class.getName(), this);
		this.namespaceService = namespaceService;
	}

	@Override
	public Object serialize(Object object)
	{
		QName qname = (QName)object;
		String prefixedQName = qname.toPrefixString(namespaceService);
		int idx = prefixedQName.indexOf(":");
		if(idx == -1)
		{
		    throw new IllegalArgumentException("Can't serialize QName without a prefix " + qname);
		}
		return prefixedQName;
	}

	@Override
	public Object deSerialize(Object object)
	{
		String qname = (String)object;
		int idx = qname.indexOf(":");
		String prefix = null;
		String localName = null;
		if(idx != -1)
		{
			prefix = qname.substring(0, idx);
			localName = qname.substring(idx + 1);
		}
		else
		{
			localName = qname;
		}
		return QName.createQName(prefix, localName, namespaceService);
	}

}
