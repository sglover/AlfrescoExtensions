/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.plugin;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.util.Collection;

import org.alfresco.elasticsearch.service.AlfrescoService;
import org.alfresco.elasticsearch.service.CamelService;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

/**
 * 
 * Alfresco ElasticSearch plugin.
 * 
 * @author sglover
 *
 */
public class AlfrescoPlugin extends AbstractPlugin
{
    public String name()
    {
        return "alfresco";
    }

    public String description()
    {
        return "Alfresco Plugin";
    }

    @SuppressWarnings("rawtypes")
    @Override public Collection<Class<? extends LifecycleComponent>> services()
    {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        services.add(AlfrescoService.class);
//        services.add(CamelService.class);
        return services;
    }

    public Collection<Class<? extends Module>> indexModules()
    {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(AlfrescoIndexModule.class);
        return modules;
    }

//    public void onModule(RiversModule module)
//    {
//    	
//    }
}
