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

import org.alfresco.service.common.elasticsearch.ElasticSearchIndexer;
import org.alfresco.services.AlfrescoApi;
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

	@Inject public ElasticSearchComponent(Settings settings, Client client, AlfrescoApi alfrescoApi)
			throws Exception
	{
        String extracterType = settings.get("entities.extracter.type", "CoreNLP");

    	ThreadFactory threadFactory = EsExecutors.daemonThreadFactory(settings);
    	this.threadPool = EsExecutors.newFixed(4, -1, threadFactory);

    	ContentGetter contentGetter = alfrescoApi.getContentGetter();
    	EntityExtracter entityExtracter = buildEntityExtracter(threadPool, extracterType, contentGetter);

     	this.elasticSearch = new ElasticSearchIndexer(alfrescoApi, entityExtracter, client, "alfresco");
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
