/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */package org.alfresco.elasticsearch.plugin;

import org.alfresco.elasticsearch.providers.AlfrescoApiProvider;
import org.alfresco.elasticsearch.providers.AlfrescoHttpClientProvider;
import org.alfresco.elasticsearch.providers.ContentGetterProvider;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.ContentGetter;
import org.elasticsearch.common.inject.AbstractModule;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoIndexModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(RegisterAlfrescoType.class).asEagerSingleton();
    	bind(AlfrescoApi.class).toProvider(AlfrescoApiProvider.class).asEagerSingleton();//in(Singleton.class)
    	bind(AlfrescoHttpClient.class).toProvider(AlfrescoHttpClientProvider.class).asEagerSingleton();//in(Singleton.class)
    	bind(ContentGetter.class).toProvider(ContentGetterProvider.class).asEagerSingleton();//in(Singleton.class)
    }
}
