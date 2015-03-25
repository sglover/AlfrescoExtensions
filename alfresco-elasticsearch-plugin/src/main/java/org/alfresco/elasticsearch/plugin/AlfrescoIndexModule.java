/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */package org.alfresco.elasticsearch.plugin;

import org.alfresco.elasticsearch.providers.AlfrescoProvider;
import org.alfresco.services.AlfrescoApi;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Singleton;

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
    	bind(AlfrescoApi.class).toProvider(AlfrescoProvider.class).in(Singleton.class);//asEagerSingleton();
    }
}
