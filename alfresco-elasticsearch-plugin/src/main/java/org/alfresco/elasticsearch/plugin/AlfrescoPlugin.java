/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.plugin;

import java.util.ArrayList;
import java.util.Collection;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.Plugin;

/**
 * 
 * Alfresco ElasticSearch plugin.
 * 
 * @author sglover
 *
 */
public class AlfrescoPlugin extends Plugin
{
    public String name()
    {
        return "alfresco";
    }

    public String description()
    {
        return "Alfresco Plugin";
    }

//    @SuppressWarnings("rawtypes")
//    @Override public Collection<Class<? extends LifecycleComponent>> services()
//    {
//        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
//        services.add(CamelService.class);
//        return services;
//    }

    public Collection<Class<? extends Module>> modules()
    {
        Collection<Class<? extends Module>> modules = new ArrayList();
        modules.add(AlfrescoIndexModule.class);
        return modules;
    }
}
