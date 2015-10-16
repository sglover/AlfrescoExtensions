/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dropwizard;

import java.io.IOException;

import org.alfresco.service.common.dropwizard.yaml.YamlPropertiesPersister;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 * 
 * @author sglover
 *
 */
public class Application
{
	private static Log logger = LogFactory.getLog(Application.class);

	private String springContextFileLocation = "cache-spring.xml";
	private String yamlConfigFileLocation = "config.yml";

	private ClassPathXmlApplicationContext context;

	public Application()
	{
	}

	public Application(String springContextFileLocation, String yamlConfigFileLocation)
	{
		this.springContextFileLocation = springContextFileLocation;
		this.yamlConfigFileLocation = yamlConfigFileLocation;
	}

    /**
     * Creates a Spring property place holder configurer for use in a Spring context
     * from the given file path.
     * 
     * @param springPropsFileName
     * @return the Spring property place holder configurer
     * @throws IOException 
     */
    protected PropertyPlaceholderConfigurer loadSpringConfigurer(String yamlConfigFileLocation) 
            throws IOException
    {
        if (StringUtils.isEmpty(yamlConfigFileLocation))
        {
            throw new IllegalArgumentException("Config file location must not be empty");
        }
        logger.debug("Loading properties from '" + yamlConfigFileLocation + "'");
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setLocation(new FileSystemResource(yamlConfigFileLocation));
        configurer.setPropertiesPersister(new YamlPropertiesPersister());
        return configurer;
    }

    public void start()
	{
	    if (springContextFileLocation != null)
	    {
	        // parent spring context with the normal DropWizard configuration defined
	        GenericApplicationContext parent = new GenericApplicationContext();
	        parent.refresh();
	        parent.getBeanFactory().registerSingleton("configuration",this);
	        parent.registerShutdownHook();
	        parent.start();
	
	        try
	        {
	            PropertyPlaceholderConfigurer configurer =
	                    loadSpringConfigurer(yamlConfigFileLocation);

	            // child spring context from xml
	            context = new ClassPathXmlApplicationContext(parent);
	            if (configurer != null)
	            {
	                context.addBeanFactoryPostProcessor(configurer);
	            }
	            context.setConfigLocations(new String[]{ springContextFileLocation });
	            context.registerShutdownHook();
	            context.refresh();
	        }
	        catch (IOException e)
	        {
	            throw new IllegalStateException("Could not create Spring context", e);
	        }
	    }
	    else
	    {
	    	throw new IllegalArgumentException("Spring context file location not set");
	    }
    }
}
