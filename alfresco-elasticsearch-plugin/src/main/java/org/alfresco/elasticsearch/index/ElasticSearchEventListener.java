/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.index;

import org.alfresco.elasticsearch.service.ElasticSearchComponent;
import org.alfresco.service.common.elasticsearch.EventListener;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;

/**
 * Listens to node events from the repository and indexes nodes, content and entities.
 * 
 * @author sglover
 *
 */
@Singleton
public class ElasticSearchEventListener extends EventListener
{
	@Inject public ElasticSearchEventListener(ElasticSearchComponent elasticSearchComponent)
	{
		super(elasticSearchComponent.getElasticSearchIndexer(), elasticSearchComponent.getElasticSearchMonitoringIndexer());
	}

    public void onChange(Object message)
    {
    	elasticSearchIndexer.init(true);
    	super.onChange(message);
    }
}
