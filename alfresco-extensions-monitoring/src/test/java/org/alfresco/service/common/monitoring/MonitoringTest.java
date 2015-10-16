/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.monitoring;



/**
 * 
 * @author sglover
 *
 */
public class MonitoringTest
{
//    private static final Log logger = LogFactory.getLog(MonitoringTest.class);
//
//    private DB db;
//
//    private MongoMetricsService metricsService;
//
//    private ElasticSearchClient client;
//    private ElasticSearchMonitoringIndexer indexer;
//
//    @Before
//    public void before() throws Exception
//    {
//    	this.client = new ElasticSearchClient("ec2-54-170-138-219.eu-west-1.compute.amazonaws.com", 9300, "test");
//        final MongoDbFactory factory = new MongoDbFactory();
//        factory.setMongoURI("mongodb://ec2-54-74-157-74.eu-west-1.compute.amazonaws.com:27017");
//        factory.setDbName("bm20-data");
//        this.db = factory.createInstance();
//
//    	this.indexer = new ElasticSearchMonitoringIndexer(client, "test");
//    	this.indexer.init(true);
//
//	    metricsService = new MongoMetricsService(db, "DeviceSync.DeviceSync11.ec2-54-74-226-6.eu-west-1.compute.amazonaws.com.metrics");
//	    metricsService.afterPropertiesSet();
//    }
//
//    @After
//    public void after() throws Exception
//    {
//    	if(client != null)
//    	{
//    		client.shutdown();
//    	}
//    }
//
//	@Test
//	public void testMonitoring()
//	{
//		try(Stream<Metrics> metrics = metricsService.getMetrics(0, 1000))
//		{
//			metrics.forEach(m ->
//			{
//				long timestampMS = m.getTimestampMS();
//
//				{
//					Histogram clearChangesRequestLag = m.getClearChangesRequestLag();
//					HistogramMonitoringEvent event = new HistogramMonitoringEvent("clearChangesRequestLag", timestampMS,
//							clearChangesRequestLag);
//					indexer.indexMonitoring(event);
//				}
//
//				{
//					Histogram getChangesRequestLag = m.getGetChangesRequestLag();
//					HistogramMonitoringEvent event = new HistogramMonitoringEvent("getChangesRequestLag", timestampMS,
//							getChangesRequestLag);
//					indexer.indexMonitoring(event);
//				}
//
//				{
//					Histogram getSubscriptionChangesResponseLag = m.getGetSubscriptionChangesResponseLag();
//					HistogramMonitoringEvent event = new HistogramMonitoringEvent("getSubscriptionChangesResponseLag", timestampMS,
//							getSubscriptionChangesResponseLag);
//					indexer.indexMonitoring(event);
//				}
//
//				{
//					Histogram nodeEventLag = m.getNodeEventLag();
//					HistogramMonitoringEvent event = new HistogramMonitoringEvent("nodeEventLag", timestampMS,
//							nodeEventLag);
//					indexer.indexMonitoring(event);
//				}
//
//				{
//					Timer syncsTimer = m.getSyncsTimer();
//					TimerMonitoringEvent event = new TimerMonitoringEvent("syncsTimer", timestampMS,
//							syncsTimer);
//					indexer.indexMonitoring(event);
//				}
//			});
//		}
//	}
}
