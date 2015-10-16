/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.service.synchronization.api.GaugeMonitoringEvent;
import org.alfresco.service.synchronization.api.HistogramMonitoringEvent;
import org.alfresco.service.synchronization.api.HistogramMonitoringEvent.Histogram;
import org.alfresco.service.synchronization.api.SyncEvent;
import org.alfresco.service.synchronization.api.TimerMonitoringEvent;
import org.alfresco.service.synchronization.api.TimerMonitoringEvent.Timer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;

import com.mongodb.BasicDBObjectBuilder;

/**
 * 
 * @author sglover
 *
 */
public class ElasticSearchMonitoringIndexer
{
    private static final Log logger = LogFactory.getLog(ElasticSearchMonitoringIndexer.class);

    private ElasticSearchClient elasticSearchClient;

    private String indexName;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    public ElasticSearchMonitoringIndexer(ElasticSearchClient elasticSearchClient, String indexName) throws Exception
	{
		this.elasticSearchClient = elasticSearchClient;
		this.indexName = indexName;
	}

    public void init(boolean checkIndexes)
    {
    	if(!initialized.get())
    	{
	    	try
	    	{
		        elasticSearchClient.init(checkIndexes);

		        initialized.set(true);
	    	}
	    	catch(Exception e)
	    	{
	    		throw new ElasticsearchException("", e);
	    	}
    	}
    }

    public void shutdown()
    {
    }

	public void indexSync(SyncEvent event)
	{
    	String syncId = event.getSyncId();
    	String username = event.getUsername();
        long timestampMS = event.getTimestamp();
        Integer numSyncChanges = event.getNumSyncChanges();
        Integer numConflicts = event.getNumConflicts();
        long duration = event.getDuration();
        boolean isSuccess = event.isSuccess();
        String siteId = event.getSiteId();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start("syncId", syncId)
        		.add("u", username)
        		.add("tim", timestampMS)
        		.add("ti", timestampMS)
        		.add("duration", duration)
        		.add("isSuccess", isSuccess)
        		.add("s", siteId);
        if(numSyncChanges != null)
        {
        	builder.add("numSyncChanges", numSyncChanges);
        }
        if(numConflicts != null)
        {
        	builder.add("numConflicts", numConflicts);
        }

        String id = event.getId();
		String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id, IndexType.sync, json, true);

        logger.debug("Indexed sync start " + id + ", " + builder.get()
        		+ "response " + response.getId() + ", " + response.getType() + ", "
                + response.getIndex() + ", " + response.getVersion());
	}

	public void indexMonitoring(HistogramMonitoringEvent event)
	{
		long timestampMS = event.getTimestamp();
		String type = event.getType();
    	Histogram data = event.getData();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start("mt", type)
        		.add("tim", timestampMS)
        		.add("ti", timestampMS)
        		.add("max", data.getMax())
        		.add("min", data.getMin())
        		.add("mean", data.getMean())
        		.add("p50", data.getP50())
        		.add("p75", data.getP75())
        		.add("p95", data.getP95())
        		.add("p98", data.getP98())
        		.add("p99", data.getP99())
        		.add("p999", data.getP999())
        		.add("stdev", data.getStddev());

        String id = event.getId();
		String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id, IndexType.monitoring, json, true);

        logger.debug("Indexed monitoring " + id + ", " + builder.get()
        		+ "response " + response.getId() + ", " + response.getType() + ", "
                + response.getIndex() + ", " + response.getVersion());
	}

	public void indexMonitoring(TimerMonitoringEvent event)
	{
		long timestampMS = event.getTimestamp();
		String type = event.getType();
    	Timer data = event.getData();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start("mt", type)
        		.add("tim", timestampMS)
        		.add("ti", timestampMS)
        		.add("max", data.getMax())
        		.add("min", data.getMin())
        		.add("mean", data.getMean())
        		.add("p50", data.getP50())
        		.add("p75", data.getP75())
        		.add("p95", data.getP95())
        		.add("p98", data.getP98())
        		.add("p99", data.getP99())
        		.add("p999", data.getP999())
        		.add("stdev", data.getStddev())
        		.add("m1_rate", data.getM1_rate())
        		.add("m5_rate", data.getM5_rate())
        		.add("m15_rate", data.getM15_rate())
        		.add("mean_rate", data.getMean_rate());

        String id = event.getId();
		String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id, IndexType.monitoring, json, true);

        logger.debug("Indexed monitoring " + id + ", " + builder.get()
        		+ "response " + response.getId() + ", " + response.getType() + ", "
                + response.getIndex() + ", " + response.getVersion());
	}

	public void indexMonitoring(GaugeMonitoringEvent event)
	{
		long timestampMS = event.getTimestamp();
		String type = event.getType();
    	long data = event.getData();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder
        		.start("mt", type)
        		.add("tim", timestampMS)
        		.add("ti", timestampMS)
        		.add("value", data);

        String id = event.getId();
		String json = builder.get().toString();

        IndexResponse response = elasticSearchClient.index(indexName, id, IndexType.monitoring, json, true);

        logger.debug("Indexed monitoring " + id + ", " + builder.get()
        		+ "response " + response.getId() + ", " + response.getType() + ", "
                + response.getIndex() + ", " + response.getVersion());
	}
}
