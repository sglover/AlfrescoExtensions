/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class SynchronousEntityTaggerCallback implements EntityTaggerCallback
{
    private static final Log logger = LogFactory.getLog(SynchronousEntityTaggerCallback.class);

	private Entities entities;
	private Throwable ex;

	@Override
    public void onSuccess(Entities entities)
    {
		synchronized (this)
        {
			logger.debug("Got entities");

			this.entities = entities;
			this.notify();	        
        }
    }

	@Override
    public void onFailure(Throwable ex)
    {
		synchronized (this)
        {
			logger.debug("Got exception " + ex);

			this.ex = ex;
			this.notify();
        }
    }
	
	private synchronized Entities getEntities()
	{
		return entities;
	}
	
	private synchronized Throwable getEx()
	{
		return ex;
	}
	
	public Entities getEntities(int timeout)
	{
		Entities entities = null;

        try
        {
        	if(getEntities() == null && getEx() == null)
        	{
	            synchronized(this)
	            {
	            	logger.debug("Waiting for " + timeout + "ms");
	                this.wait(timeout);
	            }
        	}

    		if(getEx() != null)
    		{
    			throw new RuntimeException(getEx());
    		}
    		else if(getEntities() != null)
    		{
    			entities = getEntities();
    		}
        }
        catch(InterruptedException e)
        {
        	throw new RuntimeException(e);
        }

        return entities;
	}
}
