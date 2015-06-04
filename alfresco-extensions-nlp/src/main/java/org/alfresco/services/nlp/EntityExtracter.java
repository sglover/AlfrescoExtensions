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
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.solr.GetTextContentResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class EntityExtracter
{
	private static Log logger = LogFactory.getLog(EntityExtracter.class.getName());

	private ContentGetter contentGetter;
	private EntityTagger entityTagger;
	private ExecutorService executorService;

	private Set<String> includeNodeTypes = new HashSet<>();

	public EntityExtracter(ContentGetter contentGetter, EntityTagger entityTagger, ExecutorService executorService)
	{
		this.contentGetter = contentGetter;
		this.entityTagger = entityTagger;
		this.includeNodeTypes.add("cm:content");
		this.executorService = executorService;
	}

	public EntityExtracter(ContentGetter contentGetter, EntityTagger entityTagger)
	{
		this(contentGetter, entityTagger, Executors.newFixedThreadPool(5));
	}

    public void getEntities(long nodeInternalId, EntityTaggerCallback callback)
    {
    	logger.debug("Node " + nodeInternalId + " extracting entities");

    	ExtractEntitiesRunnable extractEntities = new ExtractEntitiesRunnable(nodeInternalId, callback);
    	executorService.execute(extractEntities);
    }

    public Entities getEntities(long nodeInternalId) throws IOException, AuthenticationException
    {
    	Entities entities = null;

    	logger.debug("Node " + nodeInternalId + " extracting entities");

    	ExtractEntities extractEntities = new ExtractEntities(nodeInternalId);
    	entities = extractEntities.execute();

        return entities;
    }

    private class ExtractEntitiesRunnable extends ExtractEntities implements Runnable
    {
    	private EntityTaggerCallback callback;

    	public ExtractEntitiesRunnable(long nodeInternalId, EntityTaggerCallback callback)
        {
	        super(nodeInternalId);
	        this.callback = callback;
        }

		@Override
	    public void run()
	    {
	    	executeAsync(callback);
	    }
    }

    private class ExtractEntities
    {
    	private long nodeInternalId;

    	public ExtractEntities(long nodeInternalId)
        {
    		this.nodeInternalId = nodeInternalId;
        }

	    public Entities execute() throws IOException, AuthenticationException
	    {
	    	Entities entities = null;

	        GetTextContentResponse response = contentGetter.getTextContent(nodeInternalId);
        	try
        	{
		        InputStream in = (response != null ? response.getContent() : null);
		        if(in != null)
		        {
	        		StringWriter writer = new StringWriter();
					String encoding = "UTF-8";
					IOUtils.copy(in, writer, encoding);
					String content = writer.toString();
	
					logger.debug("OpenNlp node = " + nodeInternalId);

					entities = entityTagger.getEntities(content);
		        }
		        else
		        {
		        	logger.warn("Unable to get text content for node " + nodeInternalId);
		        }
        	}
        	finally
        	{
        		if(response != null)
        		{
        			response.release();
        		}
        	}

			return entities;
	    }

	    public void executeAsync(EntityTaggerCallback callback)
	    {
	        try
	        {
		        GetTextContentResponse response = contentGetter.getTextContent(nodeInternalId);
	        	try
	        	{
			        InputStream in = (response != null ? response.getContent() : null);
			        if(in != null)
			        {
		        		StringWriter writer = new StringWriter();
						String encoding = "UTF-8";
						IOUtils.copy(in, writer, encoding);
						String content = writer.toString();
		
						logger.debug("OpenNlp node = " + nodeInternalId);

						entityTagger.getEntities(content, callback);
			        }
			        else
			        {
			        	logger.warn("Unable to get text content for node " + nodeInternalId);
			        }
	        	}
	        	finally
	        	{
	        		if(response != null)
	        		{
	        			response.release();
	        		}
	        	}
	        }
	        catch (IOException | AuthenticationException e)
	        {
	        	logger.warn("", e);
	        }
	    }
    }
}
