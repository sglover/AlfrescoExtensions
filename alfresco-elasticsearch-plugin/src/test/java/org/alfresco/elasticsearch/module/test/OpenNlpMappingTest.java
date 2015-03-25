/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.module.test;

import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.httpclient.AuthenticationException;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.logging.log4j.LogConfigurator;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.AnalyzerProviderFactory;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.codec.docvaluesformat.DocValuesFormatService;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext.Document;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.node.settings.NodeSettingsService;
import org.elasticsearch.script.ScriptEngineService;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

public class OpenNlpMappingTest {

    private DocumentMapperParser mapperParser;

    @Before
    public void setupMapperParser() throws AuthenticationException, IOException, JSONException {
        Index index = new Index("test");

        Map<String, AnalyzerProviderFactory> analyzerFactoryFactories = Maps.newHashMap();
        analyzerFactoryFactories.put("keyword", new PreBuiltAnalyzerProviderFactory("keyword", AnalyzerScope.INDEX, new KeywordAnalyzer()));
        AnalysisService analysisService = new AnalysisService(index, ImmutableSettings.Builder.EMPTY_SETTINGS, null, analyzerFactoryFactories, null, null, null);
        ThreadPool threadPool = new ThreadPool("");
        NodeSettingsService nodeSettingsService = new NodeSettingsService(ImmutableSettings.Builder.EMPTY_SETTINGS);
        ResourceWatcherService resourceWatcherService = new ResourceWatcherService(ImmutableSettings.Builder.EMPTY_SETTINGS,
        		threadPool);
        Environment env = new Environment();
        Set<ScriptEngineService> scriptEngines = new HashSet<ScriptEngineService>();

        mapperParser = new DocumentMapperParser(index, ImmutableSettings.Builder.EMPTY_SETTINGS,
        		analysisService, new PostingsFormatService(index),
        		new DocValuesFormatService(index),
        		new SimilarityLookupService(index, ImmutableSettings.Builder.EMPTY_SETTINGS),
        		new ScriptService(ImmutableSettings.Builder.EMPTY_SETTINGS, env,
        				scriptEngines, resourceWatcherService, nodeSettingsService));
        Settings settings = settingsBuilder()
                .put("opennlp.models.name.file", "src/test/resources/models/en-ner-person.bin")
                .put("opennlp.models.date.file", "src/test/resources/models/en-ner-date.bin")
                .put("opennlp.models.location.file", "src/test/resources/models/en-ner-location.bin")
                .build();

        LogConfigurator.configure(settings);

//        ElasticSearch elasticSearch = new ElasticSearch();
//        alfrescoService.start();

//        ContentGetter contentGetter = new MockContentGetter();

        // TODO
//        mapperParser.putTypeParser(AlfrescoMapper.CONTENT_TYPE,
//        		new AlfrescoMapper.TypeParser(elasticSearch));
    }

    @Test
    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("/test-mapping.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);

        String sampleText = copyToStringFromClasspath("/sample-text.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        Document doc = docMapper.parse(json).rootDoc();

        assertThat(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), is(sampleText));
        assertThat(doc.getFields("someField.name").length, is(2));
        assertThat(doc.getFields("someField.name")[0].stringValue(), is("Jack Nicholson"));
        assertThat(doc.getFields("someField.name")[1].stringValue(), is("Kobe Bryant"));
        assertThat(doc.get(docMapper.mappers().smartName("someField.date").mapper().names().indexName()), is("tomorrow"));
        assertThat(doc.get(docMapper.mappers().smartName("someField.location").mapper().names().indexName()), is("Munich"));

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = mapperParser.parse(builtMapping);

        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse(json).rootDoc();

        assertThat(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), is(sampleText));
        assertThat(doc.getFields("someField.name").length, is(2));
        assertThat(doc.getFields("someField.name")[0].stringValue(), is("Jack Nicholson"));
        assertThat(doc.getFields("someField.name")[1].stringValue(), is("Kobe Bryant"));
        assertThat(doc.get(docMapper.mappers().smartName("someField.date").mapper().names().indexName()), is("tomorrow"));
        assertThat(doc.get(docMapper.mappers().smartName("someField.location").mapper().names().indexName()), is("Munich"));
    }

    @Test
    public void testAnalyzedOpenNlpFieldMappings() throws IOException {
        String mapping = copyToStringFromClasspath("/test-mapping-keywordanalyzer.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);
        String message = String.format("\"name\":{\"type\":\"string\",\"analyzer\":\"keyword\"}");
        assertThat(docMapper.mappingSource().string(), containsString(message));
    }
}
