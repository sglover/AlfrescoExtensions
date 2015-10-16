/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.monitoring.dao;

import org.alfresco.service.synchronization.api.HistogramMonitoringEvent.Histogram;
import org.alfresco.service.synchronization.api.TimerMonitoringEvent.Timer;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class Metrics
{
	private long timestampMS;
	private Histogram clearChangesRequestLag;
	private Histogram getChangesRequestLag;
	private Histogram getSubscriptionChangesResponseLag;
	private Histogram nodeEventLag;
	private Timer syncsTimer;

	public Metrics(long timestampMS, Histogram clearChangesRequestLag, Histogram getChangesRequestLag,
			Histogram getSubscriptionChangesResponseLag, Histogram nodeEventLag, Timer syncsTimer)
	{
		this.timestampMS = timestampMS;
		this.clearChangesRequestLag = clearChangesRequestLag;
		this.getChangesRequestLag = getChangesRequestLag;
		this.getSubscriptionChangesResponseLag = getSubscriptionChangesResponseLag;
		this.nodeEventLag = nodeEventLag;
		this.syncsTimer = syncsTimer;
	}

	private static Histogram histogramFromDBObject(DBObject dbObject)
	{
		int max = (Integer)dbObject.get("max");
		double mean = (Double)dbObject.get("mean");
		int min = (Integer)dbObject.get("min");
		double p50 = (Double)dbObject.get("p50");
		double p75 = (Double)dbObject.get("p75");
		double p95 = (Double)dbObject.get("p95");
		double p98 = (Double)dbObject.get("p98");
		double p99 = (Double)dbObject.get("p99");
		double p999 = (Double)dbObject.get("p999");
		double stddev = (Double)dbObject.get("stddev");
		Histogram histogram = new Histogram(max, mean, min, p50, p75, p95, p98, p99, p999, stddev);
		return histogram;
	}

	private static Timer timerFromDBObject(DBObject dbObject)
	{
		double max = (Double)dbObject.get("max");
		double mean = (Double)dbObject.get("mean");
		double min = (Double)dbObject.get("min");
		double p50 = (Double)dbObject.get("p50");
		double p75 = (Double)dbObject.get("p75");
		double p95 = (Double)dbObject.get("p95");
		double p98 = (Double)dbObject.get("p98");
		double p99 = (Double)dbObject.get("p99");
		double p999 = (Double)dbObject.get("p999");
		double stddev = (Double)dbObject.get("stddev");
		double m15_rate = (Double)dbObject.get("m15_rate");
		double m1_rate = (Double)dbObject.get("m1_rate");
		double m5_rate = (Double)dbObject.get("m5_rate");
        double mean_rate = (Double)dbObject.get("mean_rate");
		Timer timer = new Timer(max, mean, min, p50, p75, p95, p98, p99, p999, stddev,
				m15_rate, m1_rate, m5_rate, mean_rate);
		return timer;
	}

	public static Metrics fromDBObject(DBObject dbObject)
	{
		long timestamp = (Long)dbObject.get("timestamp");

		DBObject syncMetricsDBOBject = (DBObject)dbObject.get("sync");
		DBObject syncHistogramsDBOBject = (DBObject)syncMetricsDBOBject.get("histograms");
		DBObject getSubscriptionChangesResponseLagDBOBject = (DBObject)syncHistogramsDBOBject.get("getSubscriptionChangesResponseLag");
		Histogram getSubscriptionChangesResponseLag = histogramFromDBObject(getSubscriptionChangesResponseLagDBOBject);

		DBObject syncTimersDBObject = (DBObject)syncMetricsDBOBject.get("timers");
		DBObject syncsTimerDBObject = (DBObject)syncTimersDBObject.get("syncsTimer");
		Timer syncsTimer = timerFromDBObject(syncsTimerDBObject);

		DBObject subsMetricsDBOBject = (DBObject)dbObject.get("subs");
		DBObject subsHistogramsDBOBject = (DBObject)subsMetricsDBOBject.get("histograms");
		DBObject clearChangesRequestLagDBOBject = (DBObject)subsHistogramsDBOBject.get("clearChangesRequestLag");
		Histogram clearChangesRequestLag = histogramFromDBObject(clearChangesRequestLagDBOBject);
		DBObject getChangesRequestLagDBOBject = (DBObject)subsHistogramsDBOBject.get("getChangesRequestLag");
		Histogram getChangesRequestLag = histogramFromDBObject(getChangesRequestLagDBOBject);
		DBObject nodeEventLagDBOBject = (DBObject)subsHistogramsDBOBject.get("nodeEventLag");
		Histogram nodeEventLag = histogramFromDBObject(nodeEventLagDBOBject);

		DBObject activeMQMetricsDBOBject = (DBObject)dbObject.get("activeMQ");

		Metrics metrics = new Metrics(timestamp, clearChangesRequestLag, getChangesRequestLag, getSubscriptionChangesResponseLag,
				nodeEventLag, syncsTimer);
		return metrics;
	}

	public long getTimestampMS()
	{
		return timestampMS;
	}

	public Histogram getClearChangesRequestLag()
	{
		return clearChangesRequestLag;
	}

	public Histogram getGetChangesRequestLag()
	{
		return getChangesRequestLag;
	}

	public Histogram getGetSubscriptionChangesResponseLag()
	{
		return getSubscriptionChangesResponseLag;
	}

	public Histogram getNodeEventLag()
	{
		return nodeEventLag;
	}

	public Timer getSyncsTimer()
	{
		return syncsTimer;
	}
}
