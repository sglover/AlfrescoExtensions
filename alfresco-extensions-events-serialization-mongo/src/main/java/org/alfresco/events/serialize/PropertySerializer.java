/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.alfresco.events.node.types.DataType;
import org.alfresco.events.node.types.PathElementType;
import org.alfresco.events.node.types.Property;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.AttributeElement;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.Path.DescendentOrSelfElement;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.repository.Path.ParentElement;
import org.alfresco.service.cmr.repository.Path.SelfElement;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.VersionNumber;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class PropertySerializer
{
	private List<DBObject> serializePath(Path path)
	{
		Iterator<Path.Element> elements = path.iterator();
		List<DBObject> elementsList = new ArrayList<DBObject>();
		while(elements.hasNext())
		{
			Element element = elements.next();

			PathElementType elementType = getElementType(element);
			DBObject elementValue = null;

			if(elementType == PathElementType.ChildAssoc)
			{
				ChildAssocElement childAssocElement = (ChildAssocElement)element;
				ChildAssociationRef childAssocRef = childAssocElement.getRef();
				elementValue = serializeChildAssocRef(childAssocRef);
			}
			else
			{
				throw new IllegalArgumentException();
			}

			DBObject dbObject = BasicDBObjectBuilder
					.start("element", elementValue)
					.add("elementType", elementType.toString())
					.get();
			elementsList.add(dbObject);
		}
		
		return elementsList;
	}

	private Path deserializePath(List<DBObject> dbValue)
	{
		Path path = new Path();

		List<DBObject> dbList = (List<DBObject>)dbValue;
		for(DBObject dbObject : dbList)
		{
			String elementTypeStr = (String)dbObject.get("elementType");
			PathElementType elementType = PathElementType.valueOf(elementTypeStr);
			DBObject elementValue = (DBObject)dbObject.get("element");
			if(elementType == PathElementType.ChildAssoc)
			{
				ChildAssociationRef childAssocRef = deserializeChildAssocRef(elementValue);
				ChildAssocElement element = new ChildAssocElement(childAssocRef);
				path.append(element);
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}

		return path;
	}

	private DBObject serializeChildAssocRef(ChildAssociationRef assocRef)
	{
		NodeRef parentRef = assocRef.getParentRef();
		NodeRef childRef = assocRef.getChildRef();
		QName typeQName = assocRef.getTypeQName();
		QName childQName = assocRef.getQName();
		DBObject dbObject = BasicDBObjectBuilder
				.start("parentRef", serializeNodeRef(parentRef))
				.add("childRef", serializeNodeRef(childRef))
				.add("typeQName", typeQName.toPrefixString())
				.add("childQName", childQName.toPrefixString())
				.get();
		return dbObject;
	}

	private ChildAssociationRef deserializeChildAssocRef(DBObject dbObject)
	{
		String parentRefStr = (String)dbObject.get("parentRef");
		NodeRef parentRef = new NodeRef(parentRefStr);
		String childRefStr = (String)dbObject.get("childRef");
		NodeRef childRef = new NodeRef(childRefStr);
		QName typeQName = QName.createQName((String)dbObject.get("typeQName"));
		QName childQName = QName.createQName((String)dbObject.get("childQName"));
		ChildAssociationRef assocRef = new ChildAssociationRef(typeQName, parentRef, childQName, childRef);
		return assocRef;
	}

	private AssociationRef deserializeAssocRef(DBObject dbObject)
	{
		String sourceRefStr = (String)dbObject.get("sourceRef");
		NodeRef sourceRef = new NodeRef(sourceRefStr);
		String targetRefStr = (String)dbObject.get("targetRef");
		NodeRef targetRef = new NodeRef(targetRefStr);
		QName typeQName = QName.createQName((String)dbObject.get("typeQName"));
		AssociationRef assocRef = new AssociationRef(sourceRef, typeQName, targetRef);
		return assocRef;
	}

	private DBObject serializeAssocRef(AssociationRef assocRef)
	{
		NodeRef sourceRef = assocRef.getSourceRef();
		NodeRef targetRef = assocRef.getTargetRef();
		QName typeQName = assocRef.getTypeQName();
		DBObject dbObject = BasicDBObjectBuilder
				.start("sourceRef", serializeNodeRef(sourceRef))
				.add("targetRef", serializeNodeRef(targetRef))
				.add("typeQName", typeQName.toPrefixString())
				.get();
		return dbObject;
	}

	private DBObject serializeNodeRef(NodeRef nodeRef)
	{
		StoreRef storeRef = nodeRef.getStoreRef();
		DBObject dbObject = BasicDBObjectBuilder
				.start("storeRef", BasicDBObjectBuilder
						.start("protocol", storeRef.getProtocol())
						.add("id", storeRef.getIdentifier())
						.get())
				.add("id", nodeRef.getId())
				.get();
		return dbObject;
	}

	private NodeRef deserializeNodeRef(DBObject dbObject)
	{
		DBObject storeRefDBObject = (DBObject)dbObject.get("storeRef");
		String protocol = (String)storeRefDBObject.get("protocol");
		String id = (String)storeRefDBObject.get("id");
		StoreRef storeRef = new StoreRef(protocol, id);

		String nodeId = (String)dbObject.get("id");

		NodeRef nodeRef = new NodeRef(storeRef, nodeId);
		return nodeRef;
	}

	private PathElementType getElementType(Path.Element element)
	{
		PathElementType elementType = null;
		if(element instanceof AttributeElement)
		{
			elementType = PathElementType.Attribute;
		}
		else if(element instanceof ChildAssocElement)
		{
			elementType = PathElementType.ChildAssoc;
		}
		else if(element instanceof DescendentOrSelfElement)
		{
			elementType = PathElementType.Descendant;
		}
		else if(element instanceof ParentElement)
		{
			elementType = PathElementType.Parent;
		}
		else if(element instanceof SelfElement)
		{
			elementType = PathElementType.Self;
		}
		else
		{
			throw new IllegalArgumentException("Not supported");
		}

		return elementType;
	}

	public DBObject serialize(Property property)
	{
		String name = property.getName();
		DataType type = property.getDataType();
		Object value = property.getValue();

		// Use the value as is for the data types:
		// DataType.Text, DataType.Int, DataType.Long, DataType.Float, DataType.Double, DataType.Boolean

		if(value != null)
		{
			if(type == null || type == DataType.Any)
			{
				value = value.toString();
			}
			else if(type == DataType.Encrypted)
			{
				value = "*****";
			}
			else if(type == DataType.Content)
			{
				// don't do anything with content properties (they're handled by contentput events)
				value = null;
	//			ContentData contentData = (ContentData)value;
	//			String contentUrl = contentData.getContentUrl();
	//			String encoding = contentData.getEncoding();
	//			String mimeType = contentData.getMimetype();
	//			contentData.
			}
			else if(type == DataType.Mltext)
			{
				MLText mlText = (MLText)value;
				BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
				for(Locale locale : mlText.getLocales())
				{
					String languageTag = locale.toLanguageTag();
					String mlValue = mlText.getValue(locale);
					builder.add(languageTag, mlValue);
				}
				value = builder.get();
			}
			else if(type == DataType.Date || type == DataType.Datetime)
			{
				Date date = (Date)value;
				value = ISO8601DateFormat.format(date); 
			}
			else if(type == DataType.Qname)
			{
				QName qname = (QName)value;
				value = qname.toPrefixString();
			}
			else if(type == DataType.Noderef)
			{
				NodeRef nodeRef = (NodeRef)value;
				value = serializeNodeRef(nodeRef);
			}
			else if(type == DataType.Childassocref)
			{
				ChildAssociationRef assocRef = (ChildAssociationRef)value;
				value = serializeChildAssocRef(assocRef);
			}
			else if(type == DataType.Assocref)
			{
				AssociationRef assocRef = (AssociationRef)value;
				value = serializeAssocRef(assocRef);
			}
			else if(type == DataType.Path)
			{
				Path path = (Path)value;
				value = serializePath(path);
			}
			else if(type == DataType.Category)
			{
				NodeRef nodeRef = (NodeRef)value;
				value = serializeNodeRef(nodeRef);
			}
			else if(type == DataType.Locale)
			{
				Locale locale = (Locale)value;
				value = locale.toLanguageTag();
			}
			else if(type == DataType.Version)
			{
				VersionNumber versionNumber = (VersionNumber)value;
				value = versionNumber.getParts();
			}
			else if(type == DataType.Period)
			{
				Period period = (Period)value;
				String periodType = period.getPeriodType();
				String expression = period.getExpression();
				value = BasicDBObjectBuilder
						.start("periodType", periodType)
						.add("expression", expression)
						.get();
			}
			else
			{
				// try converting to a string
				value = value.toString();
			}
		}

		DBObject ret = BasicDBObjectBuilder
				.start("name", name)
				.add("type", (type != null ? type.toString() : null))
				.add("value", value)
				.get();

		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Property deserialize(DBObject propDBObject)
	{
		String name = (String)propDBObject.get("name");
		DataType dataType = DataType.valueOf((String)propDBObject.get("type"));
		Object dbValue = propDBObject.get("value");
		Serializable value = null;

		if(dataType == DataType.Any)
		{
			// TODO
			value = null;
		}
		else if(dataType == DataType.Encrypted)
		{
			value = null;
		}
		else if(dataType == DataType.Content)
		{
			// don't do anything with content properties (they're handled by contentput events)
			value = null;
		}
		else if(dataType == DataType.Mltext)
		{
			DBObject dbObject = (DBObject)dbValue;
			MLText mlText = new MLText();
			for(String languageTag : dbObject.keySet())
			{
				Locale locale = Locale.forLanguageTag(languageTag);
				String mlValue = (String)dbObject.get(languageTag);
				mlText.addValue(locale, mlValue);
			}
			value = mlText;
		}
		else if(dataType == DataType.Date || dataType == DataType.Datetime)
		{
			String dateStr = (String)dbValue;
			value = ISO8601DateFormat.parse(dateStr); 
		}
		else if(dataType == DataType.Qname)
		{
			String qnameStr = (String)dbValue;
			QName qname = QName.createQName(qnameStr);
			value = qname;
		}
		else if(dataType == DataType.Noderef)
		{
			DBObject dbObject = (DBObject)dbValue;
			NodeRef nodeRef = deserializeNodeRef(dbObject);
			value = nodeRef;
		}
		else if(dataType == DataType.Childassocref)
		{
			DBObject dbObject = (DBObject)dbValue;
			value = deserializeChildAssocRef(dbObject);
		}
		else if(dataType == DataType.Assocref)
		{
			DBObject dbObject = (DBObject)dbValue;
			value = deserializeAssocRef(dbObject);
		}
		else if(dataType == DataType.Path)
		{
			value = deserializePath((List<DBObject>)dbValue);
		}
		else if(dataType == DataType.Category)
		{
			DBObject dbObject = (DBObject)dbValue;
			value = deserializeNodeRef(dbObject);
		}
		else if(dataType == DataType.Locale)
		{
			String languageTag = (String)dbValue;
			value = Locale.forLanguageTag(languageTag);
		}
		else if(dataType == DataType.Version)
		{
			VersionNumber versionNumber = new VersionNumber((String)dbValue);
			value = versionNumber;
		}
		else if(dataType == DataType.Period)
		{
			DBObject dbObject = (DBObject)dbValue;
			String periodType = (String)dbObject.get("periodType");
			String expression = (String)dbObject.get("expression");

			String periodStr = periodType + "|" + expression;
			Period period = new Period(periodStr);
			value = period;
		}
		else
		{
			// TODO
		}

		Property property = new Property(name, value, dataType);
		return property;
	}
}
