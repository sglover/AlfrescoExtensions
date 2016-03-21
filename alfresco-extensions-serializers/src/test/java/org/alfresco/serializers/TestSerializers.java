/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
//import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.serializers.types.QNameSerializer;
import org.alfresco.serializers.types.SerializerRegistry;
import org.alfresco.serializers.types.Serializers;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;

public class TestSerializers
{
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
	private NodeMetadataSerializer serializer;
	private Files files;
	private DB db;
	private SerializerRegistry serializerRegistry;
	private NodePropertyHelper nodePropertyHelper;
	private PropertySerializer propertySerializer;

	@Before
	public void before()
	{
		this.files = new MongoFilesImpl(db);
		this.dictionaryService = mock(DictionaryService.class);
		this.namespaceService = mock(NamespaceService.class);
		when(namespaceService.getPrefixes("http://www.alfresco.org/model/content/1.0")).thenReturn(Arrays.asList("cm"));
		when(namespaceService.getNamespaceURI("cm")).thenReturn("http://www.alfresco.org/model/content/1.0");
		PropertyDefinition authorProp = mock(PropertyDefinition.class);
		PropertyDefinition contentProp = mock(PropertyDefinition.class);
		ClassDefinition authorClass = mock(ClassDefinition.class);
		ClassDefinition contentClass = mock(ClassDefinition.class);
		DataTypeDefinition stringType = mock(DataTypeDefinition.class);
		DataTypeDefinition contentType = mock(DataTypeDefinition.class);
		when(stringType.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(authorClass.getName()).thenReturn(ContentModel.ASPECT_AUTHOR);
		when(contentClass.getName()).thenReturn(ContentModel.TYPE_CONTENT);
		when(authorProp.getContainerClass()).thenReturn(authorClass);
		when(authorProp.getDataType()).thenReturn(stringType);
		when(authorProp.getName()).thenReturn(ContentModel.PROP_NAME);
		when(contentProp.getContainerClass()).thenReturn(contentClass);
		when(contentProp.getDataType()).thenReturn(contentType);
		when(contentProp.getName()).thenReturn(ContentModel.PROP_CONTENT);
		when(contentType.getName()).thenReturn(DataTypeDefinition.CONTENT);
		when(dictionaryService.getProperty(ContentModel.PROP_AUTHOR)).thenReturn(authorProp);
		when(dictionaryService.getProperty(ContentModel.PROP_CONTENT)).thenReturn(contentProp);

		this.serializerRegistry = new Serializers();
		QNameSerializer qnameSerializer = new QNameSerializer(serializerRegistry, namespaceService);
		this.serializerRegistry.registerSerializer(QName.class.getName(), qnameSerializer);

		this.propertySerializer = new PropertySerializer(dictionaryService, namespaceService);

		serializer = new HierarchicalNodeMetadataSerializer(dictionaryService, namespaceService,
				serializerRegistry, files, propertySerializer);
	}

//	@Test
//	public void test1() throws Exception
//	{
//		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//		NodeVersionKey nodeVersionKey = new NodeVersionKey(1l, 1l);
//		String changeTxnId = GUID.generate();
//		Long txnId = 1l;
//
//		QName nodeTypeQName = ContentModel.TYPE_CONTENT;
//		String nodeType = (String)serializerRegistry.serialize(nodeTypeQName);
//
//		Map<String, Serializable> props = new HashMap<>();
//		String authorProp = (String)serializerRegistry.serialize(ContentModel.PROP_AUTHOR);
//		String contentProp = (String)serializerRegistry.serialize(ContentModel.PROP_CONTENT);
//		props.put(authorProp, "steve");
//		ContentData contentData = new ContentData("file:/a/b/c", "text/plain", 10l, "UTF-8");
//		ContentDataWithId content = new ContentDataWithId(contentData, 1l);
//		props.put(contentProp, content);
//
//		Set<String> aspectQNames = new HashSet<>();
//		String authorAspect = (String)serializerRegistry.serialize(ContentModel.ASPECT_AUTHOR);
//		aspectQNames.add(authorAspect);
//
//		serializer.buildNodeMetadata(builder, nodeVersionKey, changeTxnId, txnId, nodeType, props, aspectQNames);
//
//		DBObject dbObject = builder.get();
//
//		System.out.println(dbObject.toString());
//	}
	
//	@Test
//	public void test2() throws Exception
//	{
//        ContentData contentData = new ContentData("file:/a/b/c", "text/plain", 10l, "UTF-8");
//        ContentDataWithId content = new ContentDataWithId(contentData, 1l);
//
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//        propertySerializer.serialize(ContentModel.PROP_CONTENT, content, builder);
//        DBObject dbObject = builder.get();
//
//        System.out.println(dbObject.toString());
//
//        String contentProp = ContentModel.PROP_CONTENT.toPrefixString(namespaceService);
//        DBObject contentDBObject = (DBObject)dbObject.get(contentProp);
//        JSON json = JSON.from(contentDBObject);
//        Serializable content1 = propertySerializer.deserialize(ContentModel.PROP_CONTENT, json);
//        System.out.println(content1);
//	}
}
