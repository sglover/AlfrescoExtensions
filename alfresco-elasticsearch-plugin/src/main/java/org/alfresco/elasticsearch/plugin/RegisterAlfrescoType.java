/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.plugin;

import org.alfresco.elasticsearch.index.mapper.AlfrescoMapper;
import org.alfresco.elasticsearch.service.ElasticSearchComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;

/**
 * 
 * @author sglover
 *
 */
public class RegisterAlfrescoType extends AbstractIndexComponent
{
    @Inject
    public RegisterAlfrescoType(Index index, @IndexSettings Settings indexSettings, MapperService mapperService,
                                  AnalysisService analysisService, ElasticSearchComponent elasticSearchComponent)
    {
        super(index, indexSettings);
        mapperService.documentMapperParser().putTypeParser("entities",
                new AlfrescoMapper.TypeParser(elasticSearchComponent.getElasticSearch()));
    }
}
