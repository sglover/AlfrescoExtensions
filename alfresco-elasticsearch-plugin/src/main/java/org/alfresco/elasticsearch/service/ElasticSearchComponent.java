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

import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.service.common.elasticsearch.ElasticSearchClient;
import org.alfresco.service.common.elasticsearch.ElasticSearchIndexer;
import org.alfresco.service.common.elasticsearch.entities.ElasticSearchEntitiesGetter;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.AlfrescoDictionary;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.nlp.EntityExtracter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;

/**
 * 
 * @author sglover
 *
 */
@Singleton
public class ElasticSearchComponent
{
	private static final Log logger = LogFactory.getLog(ElasticSearchComponent.class);

	private ElasticSearchIndexer elasticSearch;
	private ThreadPoolExecutor threadPool;

	@Inject public ElasticSearchComponent(Settings settings, Client client, AlfrescoApi alfrescoApi, ContentGetter contentGetter,
			AlfrescoHttpClient alfrescoHttpClient) throws Exception
	{
        String extracterType = settings.get("entities.extracter.type", "CoreNLP");
        String indexName = "alfresco";

    	ThreadFactory threadFactory = EsExecutors.daemonThreadFactory(settings);
    	this.threadPool = EsExecutors.newFixed(4, -1, threadFactory);

		ElasticSearchClient elasticSearchClient = new ElasticSearchClient(client, indexName);
    	//ElasticSearchEntitiesGetter entitiesService = new ElasticSearchEntitiesGetter(elasticSearchClient);
    	EntityExtracter entityExtracter = buildEntityExtracter(threadPool, extracterType, contentGetter);

    	AlfrescoDictionary alfrescoDictionary = new AlfrescoDictionary(alfrescoHttpClient);

     	this.elasticSearch = new ElasticSearchIndexer(alfrescoApi, contentGetter, entityExtracter, client,
     			alfrescoDictionary, /*entitiesService, */elasticSearchClient, indexName);
	}

	private EntityExtracter buildEntityExtracter(ThreadPoolExecutor threadPool, String extracterType, ContentGetter contentGetter)
	{
		EntityExtracter entityExtracter = null;

        logger.debug("extracterType = " + extracterType);

        switch(extracterType)
        {
        case "CoreNLP":
        	entityExtracter = EntityExtracter.coreNLPEntityExtracter(contentGetter, threadPool);
        	break;
        case "StanfordNLP":
        	entityExtracter = EntityExtracter.stanfordNLPEntityExtracter(contentGetter, threadPool);
        	break;
        default:
        	throw new ElasticsearchException("Invalid entity.extracter.type");
        }

        return entityExtracter;
	}

	public void close()
	{
    	if(threadPool != null && !threadPool.isShutdown())
    	{
    		threadPool.shutdown();
    	}

    	if(elasticSearch != null)
    	{
    		elasticSearch.shutdown();
    	}
	}

	public ElasticSearchIndexer getElasticSearch()
	{
		return elasticSearch;
	}

//	public void start() throws Exception
//	{
//		elasticSearch.init(true);
//	}
}
