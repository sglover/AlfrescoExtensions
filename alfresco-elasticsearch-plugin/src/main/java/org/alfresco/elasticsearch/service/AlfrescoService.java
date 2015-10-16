/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.service;

import org.alfresco.httpclient.AlfrescoHttpClient;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

/**
 * A service provided entirely to provide lifecycle shutdown for various components.
 * 
 * @author sglover
 *
 */
public class AlfrescoService //extends AbstractLifecycleComponent<AlfrescoService>
{
//	private AlfrescoHttpClient repoClient;
//	private ElasticSearchComponent elasticSearchComponent;
//
//	@Inject public AlfrescoService(Settings settings, AlfrescoHttpClient repoClient,
//			ElasticSearchComponent elasticSearchComponent)
//    {
//        super(settings);
//
//        this.repoClient = repoClient;
//        this.elasticSearchComponent = elasticSearchComponent;
//    }
//
//	@Override
//    protected void doStart() throws ElasticsearchException
//    {
////        addLifecycleListener(new LifecycleListener()
////		{
////        	public void afterStart()
////        	{
////        		try
////        		{
////        			AlfrescoService.this.elasticSearchComponent.start();
////        		}
////        		catch(Exception e)
////        		{
////        			throw new ElasticsearchException("", e);
////        		}
////        	}
////		});
//    }
//
//	@Override
//    protected void doStop() throws ElasticsearchException
//    {
//		if(elasticSearchComponent != null)
//		{
//			elasticSearchComponent.close();
//		}
//    	if(repoClient != null)
//    	{
//    		repoClient.close();
//    	}
//    }
//
//	@Override
//    protected void doClose() throws ElasticsearchException
//    {
//		if(elasticSearchComponent != null)
//		{
//			elasticSearchComponent.close();
//		}
//    	if(repoClient != null)
//    	{
//    		repoClient.close();
//    	}
//    }
}
