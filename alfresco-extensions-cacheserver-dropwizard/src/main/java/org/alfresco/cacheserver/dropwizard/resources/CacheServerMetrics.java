/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dropwizard.resources;

import com.codahale.metrics.MetricRegistry;

/**
 * 
 * @author sglover
 *
 */
public class CacheServerMetrics
{
	public void init()
    {
		final MetricRegistry metrics = new MetricRegistry();

//	    Gauge<Long> numberOfEventsConsumed = new Gauge<Long>()
//	    {
//			@Override
//			public Long getValue()
//			{
//				return stats.getStats().getEventsConsumed();
//			}
//	    };
    }
}
