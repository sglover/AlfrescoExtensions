/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * 
 * @author sglover
 *
 */
public class SpringClasspathModelLoader implements ModelLoader, ApplicationContextAware
{
    private static final Log logger = LogFactory.getLog(SpringClasspathModelLoader.class);

    private ApplicationContext applicationContext;

    public InputStream load(String modelFilePath) throws IOException
    {
    	if(modelFilePath.startsWith("classpath:"))
    	{
    		modelFilePath = "classpath:" + modelFilePath;
    	}
    	Resource resource = applicationContext.getResource(modelFilePath);
    	InputStream in = resource != null ? resource.getInputStream() : null;
    	return in;
    }

//    class Loader implements Runnable
//    {
//    	private String modelFilePath;
//    	private String type;
//    	private CountDownLatch countDownLatch;
//    	private Map<String, T> tokenNameFinders;
//
//    	Loader(String modelFilePath, String type, CountDownLatch countDownLatch,
//    		Map<String, T> tokenNameFinders)
//    	{
//    		this.modelFilePath = modelFilePath;
//    		this.type = type;
//    		this.countDownLatch = countDownLatch;
//    		this.tokenNameFinders = tokenNameFinders;
//    	}
//
//		@Override
//        public void run()
//        {
//	        long start = System.currentTimeMillis();
//	        try {
//	        	Resource resource = applicationContext.getResource(modelFilePath);
//	        	InputStream in = resource != null ? resource.getInputStream() : null;
//
//	        	tokenNameFinders.put(type, new PooledTokenNameFinderModel(in));
//	        } catch (IOException e) {
//	            logger.error("Error loading model file " + modelFilePath, e);
//	        }
//	        long end = System.currentTimeMillis();
//	        long time = end - start;
//	        logger.info("Loaded file " + modelFilePath + " in " + time);
//	        countDownLatch.countDown();
//        }
//    }

	@Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
		this.applicationContext = applicationContext;
    }
}
