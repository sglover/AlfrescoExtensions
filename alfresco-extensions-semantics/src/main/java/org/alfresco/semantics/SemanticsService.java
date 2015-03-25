/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.semantics;

import org.alfresco.service.common.dropwizard.AbstractSpringDropwizardService;
import org.alfresco.service.common.dropwizard.SpringDropwizardConfiguration;
import org.apache.log4j.Logger;

import com.yammer.dropwizard.config.Environment;

/**
 * 
 * @author sglover
 *
 */
public class SemanticsService extends AbstractSpringDropwizardService<SpringDropwizardConfiguration>
{
    private static final String URL_PREFIX = "/alfresco/api/-default-/private/alfresco/versions/1";
    private static final Logger LOGGER = Logger.getLogger(SemanticsService.class.getName());
    
    public static void main(String[] args) throws Exception
    {
        LOGGER.debug("main "+args);
        String yamlConfigFileLocation = null;
        if (args.length > 1)
        {
            yamlConfigFileLocation = args[1];
        }
        new SemanticsService(yamlConfigFileLocation).run(args);
    }
    
    public SemanticsService(String yamlConfigFileLocation)
    {
        super(yamlConfigFileLocation);
        LOGGER.debug("<init>");
        this.isRequestResponseLogged = true;
    }

    @Override
    public void setupEnvironment(SpringDropwizardConfiguration configuration, Environment environment)
    {
        // register beans with dropwizard
        addEnvironmentItemFromSpringBean(environment,
                EnvironmentType.RESOURCE, "semanticsResource");
    }
}
