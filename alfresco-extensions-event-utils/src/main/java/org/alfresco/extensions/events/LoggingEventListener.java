/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.events;

import org.apache.log4j.Logger;

/**
 * 
 * @author sglover
 *
 */
public class LoggingEventListener implements EventListener
{
	private static final Logger LOGGER = Logger.getLogger(LoggingEventListener.class.getName());

	@Override
	public void onMessage(Object event)
	{
		LOGGER.debug(event);
	}
}
