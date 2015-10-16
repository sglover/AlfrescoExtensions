/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.monitoring.dao;

import java.util.stream.Stream;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public interface MetricsService
{
	void addMetrics(DBObject syncMetrics, DBObject subsMetrics, DBObject activeMQStats);
	Stream<Metrics> getMetrics(int skip, int limit);
}
