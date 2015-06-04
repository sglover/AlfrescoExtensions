/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.index.mapper;

import static org.elasticsearch.index.mapper.MapperBuilders.dateField;
import static org.elasticsearch.index.mapper.MapperBuilders.stringField;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.common.elasticsearch.ElasticSearchIndexer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.FieldMapperListener;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeContext;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.ObjectMapperListener;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoMapper extends AbstractFieldMapper<Object>
{
    private static ESLogger logger = ESLoggerFactory.getLogger(AlfrescoMapper.class.getName());

    public static final String CONTENT_TYPE = "entities";

    public static class Defaults
    {
        public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
    }

    public static class FieldNames
    {
        public static final String NAME = "name";
        public static final String LOCATION = "location";
        public static final String DATE = "date";
        public static final String ORGS = "orgs";
    }

    public static class Builder extends AbstractFieldMapper.Builder<Builder, AlfrescoMapper>
    {
        private ContentPath.Type pathType = Defaults.PATH_TYPE;

        private Boolean ignoreErrors = null;

        private Integer defaultIndexedChars = null;

        private Boolean langDetect = null;

        private Mapper.Builder contentBuilder;
        private Mapper.Builder orgsBuilder = stringField("orgs");
        private Mapper.Builder nameBuilder = stringField("name");
        private Mapper.Builder dateBuilder = dateField("date");
        private Mapper.Builder locationBuilder = stringField("location");

        private ElasticSearchIndexer elasticSearch;

        public Builder(String name, ElasticSearchIndexer elasticSearch)
        {
            super(name, new FieldType(AbstractFieldMapper.Defaults.FIELD_TYPE));
            this.builder = this;
            this.contentBuilder = stringField(name);
            this.elasticSearch = elasticSearch;
        }

        public Builder pathType(ContentPath.Type pathType)
        {
            this.pathType = pathType;
            return this;
        }

        public Builder content(Mapper.Builder content)
        {
            this.contentBuilder = content;
            return this;
        }

        public Builder date(Mapper.Builder date)
        {
            this.dateBuilder = date;
            return this;
        }

        public Builder name(Mapper.Builder name)
        {
            this.nameBuilder = name;
            return this;
        }
        
        public Builder location(Mapper.Builder location)
        {
            this.locationBuilder = location;
            return this;
        }

        public Builder orgs(Mapper.Builder orgs)
        {
            this.orgsBuilder = orgs;
            return this;
        }

        @Override
        public AlfrescoMapper build(BuilderContext context)
        {
            ContentPath.Type origPathType = context.path().pathType();
            context.path().pathType(pathType);

            // create the content mapper under the actual name
            Mapper contentMapper = contentBuilder.build(context);

            // create the DC one under the name
            context.path().add(name);

            Mapper dateMapper = dateBuilder.build(context);
            Mapper nameMapper = nameBuilder.build(context);
            Mapper locationMapper = locationBuilder.build(context);
            Mapper orgsMapper = orgsBuilder.build(context);

            context.path().remove();

            context.path().pathType(origPathType);

            if (defaultIndexedChars == null && context.indexSettings() != null) {
                defaultIndexedChars = context.indexSettings().getAsInt("index.mapping.attachment.indexed_chars", 100000);
            }
            if (defaultIndexedChars == null) {
                defaultIndexedChars = 100000;
            }

            if (ignoreErrors == null && context.indexSettings() != null) {
                ignoreErrors = context.indexSettings().getAsBoolean("index.mapping.attachment.ignore_errors", Boolean.TRUE);
            }
            if (ignoreErrors == null) {
                ignoreErrors = Boolean.TRUE;
            }

            if (langDetect == null && context.indexSettings() != null) {
                langDetect = context.indexSettings().getAsBoolean("index.mapping.attachment.detect_language", Boolean.FALSE);
            }
            if (langDetect == null) {
                langDetect = Boolean.FALSE;
            }

            return new AlfrescoMapper(buildNames(context), multiFieldsBuilder.build(this, context), copyTo,
            		contentMapper, nameMapper, dateMapper, locationMapper, orgsMapper,
            		elasticSearch);
        }
    }

    /**
     * <pre>
     *  field1 : { type : "attachment" }
     * </pre>
     * Or:
     * <pre>
     *  field1 : {
     *      type : "attachment",
     *      fields : {
     *          field1 : {type : "binary"},
     *          title : {store : "yes"},
     *          date : {store : "yes"},
     *          name : {store : "yes"},
     *          author : {store : "yes"},
     *          keywords : {store : "yes"},
     *          content_type : {store : "yes"},
     *          content_length : {store : "yes"}
     *      }
     * }
     * </pre>
     */
    public static class TypeParser implements Mapper.TypeParser
    {
    	private ElasticSearchIndexer elasticSearch;

    	public TypeParser(ElasticSearchIndexer elasticSearch)
    	{
    		this.elasticSearch = elasticSearch;
    	}

        private Mapper.Builder<?, ?> findMapperBuilder(Map<String, Object> propNode, String propName, ParserContext parserContext) {
            String type;
            Object typeNode = propNode.get("type");
            if (typeNode != null) {
                type = typeNode.toString();
            } else {
                type = "string";
            }
            Mapper.TypeParser typeParser = parserContext.typeParser(type);
            Mapper.Builder<?, ?> mapperBuilder = typeParser.parse(propName, (Map<String, Object>) propNode, parserContext);

            return mapperBuilder;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
        	AlfrescoMapper.Builder builder = new AlfrescoMapper.Builder(name, elasticSearch);

            for (Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();

                if (fieldName.equals("fields")) {
                    Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                    for (Iterator<Map.Entry<String, Object>> fieldsIterator = fieldsNode.entrySet().iterator(); fieldsIterator.hasNext();) {
                        Map.Entry<String, Object> entry1 = fieldsIterator.next();
                        String propName = entry1.getKey();
                        Map<String, Object> propNode = (Map<String, Object>) entry1.getValue();

                        Mapper.Builder<?, ?> mapperBuilder = findMapperBuilder(propNode, propName, parserContext);
/*                        if (parseMultiField((AbstractFieldMapper.Builder) mapperBuilder, fieldName, parserContext, propName, propNode)) {
                            fieldsIterator.remove();
                        } else */

                        if (propName.equals(name)) {
                            builder.content(mapperBuilder);
                            fieldsIterator.remove();
                        } else {
                            switch (propName) {
                                case FieldNames.DATE:
                                    builder.date(mapperBuilder);
                                    fieldsIterator.remove();
                                    break;
                                case FieldNames.NAME:
                                    builder.name(mapperBuilder);
                                    fieldsIterator.remove();
                                    break;
                                case FieldNames.LOCATION:
                                    builder.name(mapperBuilder);
                                    fieldsIterator.remove();
                                    break;
                            }
                        }
                    }
//                    DocumentMapperParser.checkNoRemainingFields(fieldName, fieldsNode, parserContext.indexVersionCreated());
                    iterator.remove();
                }
            }

            return builder;
        }
    }

    private final Mapper contentMapper;
    private final Mapper dateMapper;
    private final Mapper nameMapper;
    private final Mapper locationMapper;
    private final Mapper orgsMapper;

    private ElasticSearchIndexer elasticSearch;

    public AlfrescoMapper(Names names, MultiFields multiFields, CopyTo copyTo,
    		Mapper contentMapper, Mapper nameMapper,
    		Mapper dateMapper, Mapper locationMapper, Mapper orgsMapper, ElasticSearchIndexer elasticSearch)
    {
        super(names, 1.0f, AbstractFieldMapper.Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null,
                ImmutableSettings.EMPTY, multiFields, copyTo);

        this.contentMapper = contentMapper;
        this.dateMapper = dateMapper;
        this.nameMapper = nameMapper;
        this.locationMapper = locationMapper;
        this.orgsMapper = orgsMapper;
        this.elasticSearch = elasticSearch;
    }

    @Override
    public Object value(Object value)
    {
        return null;
    }

    @Override
    public FieldType defaultFieldType()
    {
        return AbstractFieldMapper.Defaults.FIELD_TYPE;
    }

    @Override
    public FieldDataType defaultFieldDataType()
    {
        return null;
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        long nodeInternalId = -1;
        long nodeVersion = -1;

        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_NUMBER)
        {
            String text = parser.text();
            int idx = text.indexOf(".");
            if(idx == -1 )
            {
                throw new MapperParsingException("No nodeInternalId (_n) is provided.");
            }
            nodeInternalId = Long.valueOf(text.substring(0, idx)); 
            nodeVersion = Long.valueOf(text.substring(idx + 1));
        }

        IndexableField nodeTypeField = context.doc().getField("t");
        String nodeType = (nodeTypeField != null ? nodeTypeField.stringValue() : null);
        if (nodeType == null)
        {
            throw new MapperParsingException("No nodeType (t) is provided.");
        }

    	// TODO this is done asynchronously and may fail, in which case the even read will have completed
    	// successfully. Need to address this.
        String indexId = nodeInternalId + "." + nodeVersion;
        String versionLabel = "1.0";
		elasticSearch.indexEntitiesForContent(nodeInternalId, nodeVersion, nodeType, versionLabel, indexId);
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> fields) throws IOException
    {
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException
    {
        // ignore this for now
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener)
    {
        contentMapper.traverse(fieldMapperListener);
        dateMapper.traverse(fieldMapperListener);
        nameMapper.traverse(fieldMapperListener);
        locationMapper.traverse(fieldMapperListener);
        orgsMapper.traverse(fieldMapperListener);
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener)
    {
    }

    @Override
    public void close()
    {
        contentMapper.close();
        dateMapper.close();
        locationMapper.close();
        nameMapper.close();
        orgsMapper.close();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException
    {
        builder.startObject(name());
        builder.field("type", CONTENT_TYPE);

        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        nameMapper.toXContent(builder, params);
        dateMapper.toXContent(builder, params);
        locationMapper.toXContent(builder, params);
        orgsMapper.toXContent(builder, params);
        builder.endObject();

        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType()
    {
        return CONTENT_TYPE;
    }
}
