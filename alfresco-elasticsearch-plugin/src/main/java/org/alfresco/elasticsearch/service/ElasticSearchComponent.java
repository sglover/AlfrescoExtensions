/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.service;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.contentstore.ContentStore;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.service.common.elasticsearch.ElasticSearchClient;
import org.alfresco.service.common.elasticsearch.ElasticSearchIndexer;
import org.alfresco.service.common.elasticsearch.ElasticSearchMonitoringIndexer;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.AlfrescoDictionary;
import org.alfresco.services.TextContentGetter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.sglover.nlp.CoreNLPEntityTagger;
import org.sglover.nlp.EntityExtracter;
import org.sglover.nlp.EntityTagger;
import org.sglover.nlp.StanfordEntityTagger;

/**
 * 
 * @author sglover
 *
 */
@Singleton
public class ElasticSearchComponent
{
    private static final Log logger = LogFactory
            .getLog(ElasticSearchComponent.class);

    private ElasticSearchIndexer elasticSearchIndexer;
    private ElasticSearchMonitoringIndexer elasticSearchMonitoringIndexer;
    private ThreadPoolExecutor threadPool;

    @Inject
    public ElasticSearchComponent(Settings settings, Client client,
            AlfrescoApi alfrescoApi, ContentStore contentStore,
            AlfrescoHttpClient alfrescoHttpClient, TextContentGetter contentGetter) throws Exception
    {
        String extracterType = settings.get("entities.extracter.type",
                "CoreNLP");
        String indexName = "alfresco";

        ThreadFactory threadFactory = EsExecutors.daemonThreadFactory(settings);
        this.threadPool = EsExecutors.newFixed("Alfresco", 4, -1, threadFactory);

        ElasticSearchClient elasticSearchClient = new ElasticSearchClient(
                client, indexName);

        EntityTagger entityTagger = buildEntityTagger(extracterType);
        EntityExtracter entityExtracter = buildEntityExtracter(entityTagger);

        AlfrescoDictionary alfrescoDictionary = new AlfrescoDictionary(
                alfrescoHttpClient);

        this.elasticSearchIndexer = new ElasticSearchIndexer(alfrescoApi,
                contentGetter, entityTagger, entityExtracter, client,
                alfrescoDictionary, elasticSearchClient, indexName);
        this.elasticSearchMonitoringIndexer = new ElasticSearchMonitoringIndexer(
                elasticSearchClient, indexName);
    }

    private EntityTagger buildEntityTagger(String extracterType)
    {
        EntityTagger entityTagger = null;

        logger.debug("extracterType = " + extracterType);

        switch (extracterType)
        {
        case "CoreNLP":
            entityTagger = CoreNLPEntityTagger.defaultTagger();
            break;
        case "StanfordNLP":
            entityTagger = StanfordEntityTagger.build();
            break;
        default:
            throw new ElasticsearchException("Invalid entity.extracter.type");
        }

        return entityTagger;
    }

    private EntityExtracter buildEntityExtracter(EntityTagger entityTagger)
    {
        EntityExtracter entityExtracter = new EntityExtracter(entityTagger);
        return entityExtracter;
    }

    public void close()
    {
        if (threadPool != null && !threadPool.isShutdown())
        {
            threadPool.shutdown();
        }

        if (elasticSearchIndexer != null)
        {
            elasticSearchIndexer.shutdown();
        }
    }

    public ElasticSearchIndexer getElasticSearchIndexer()
    {
        return elasticSearchIndexer;
    }

    public ElasticSearchMonitoringIndexer getElasticSearchMonitoringIndexer()
    {
        return elasticSearchMonitoringIndexer;
    }

    // public void start() throws Exception
    // {
    // elasticSearch.init(true);
    // }
}
