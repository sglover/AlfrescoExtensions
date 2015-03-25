/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author sglover
 *
 */
public class ElasticSearchTest
{
    private static final Log logger = LogFactory.getLog(ElasticSearchTest.class);

    @Autowired
    private ElasticSearchClient client;

    @Before
    public void before() throws Exception
    {
    	this.client = new ElasticSearchClient("localhost", 9300, "alfresco");
    }

    @After
    public void after() throws Exception
    {
    	if(client != null)
    	{
    		client.shutdown();
    	}
    }

    private String getString(SearchHit hit, String fieldName)
    {
        String value = null;

        SearchHitField field = hit.field(fieldName);
        if(field != null)
        {
            value = field.getValue();
        }

        return value;
    }

    private Integer getInteger(SearchHit hit, String fieldName)
    {
        Integer value = null;

        SearchHitField field = hit.field(fieldName);
        if(field != null)
        {
            value = field.getValue();
        }

        return value;
    }

    private Long getLong(SearchHit hit, String fieldName)
    {
        Long value = null;

        SearchHitField field = hit.field(fieldName);
        if(field != null)
        {
            value = field.getValue();
        }

        return value;
    }

	@Test
	public void testQueryForTest()
	{
		logger.debug("testQueryForTest =======================");

		SearchResponse response = client.match("alfresco", "crypto");
        for(SearchHit hit : response.getHits().hits())
        {
            String nodeId = getString(hit, "id");
            String versionLabel = getString(hit, "l");
            long nodeInternalId = getLong(hit, "n");
            long nodeVersion = getLong(hit, "v");
            logger.debug("Found node " + nodeId + "." + versionLabel + "." + nodeInternalId + "." + nodeVersion);
        }
	}

	@Test
	public void testEventsForUser()
	{
		logger.debug("testEventsForUser =======================");

		SearchResponse response = client.matchEvents("alfresco", "admin",
				0, 300);
        for(SearchHit hit : response.getHits().hits())
        {
        	String nodeId = null;
        	String timestamp = null;

        	{
	        	SearchHitField field = hit.field("n");
	        	if(field != null)
	        	{
		        	nodeId = field.getValue();
	        	}
	        }

        	{
	        	SearchHitField field = hit.field("ti");
	        	if(field != null)
	        	{
	        		timestamp = field.getValue();
	        	}
	        }

        	logger.debug("Found event " + nodeId + ", " + timestamp);
        }
	}
	
	@Test
	public void testMatchEventsForNode()
	{
		logger.debug("testMatchEventsForNode =======================");

		int i = 1;

		SearchResponse response = client.matchEventsForNode("alfresco", "7726cb91-ba78-4554-8c15-e2e3de2127d0",
				0, 300);
        for(SearchHit hit : response.getHits().hits())
        {
        	String nodeId = null;
        	String timestamp = null;
        	String username = null;

        	{
	        	SearchHitField field = hit.field("n");
	        	if(field != null)
	        	{
		        	nodeId = field.getValue();
	        	}
	        }

        	{
	        	SearchHitField field = hit.field("ti");
	        	if(field != null)
	        	{
	        		timestamp = field.getValue();
	        	}
	        }

        	{
	        	SearchHitField field = hit.field("u");
	        	if(field != null)
	        	{
	        		username = field.getValue();
	        	}
	        }

        	logger.debug("Found event " + i++ + ": " + nodeId + ", " + timestamp + "," + username);
        }
	}
	
	@Test
	public void testUserAggregation() throws Exception
	{
		logger.debug("testUserAggregation =======================");

    	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

    	TermsBuilder aggregationBuilder = AggregationBuilders
    			.terms("users_agg")
    			.field("u")
//    			.subAggregation(AggregationBuilders.dateRange("event_date_range").)
    			.order(Terms.Order.term(true));

    	SearchResponse response = client.search("alfresco", queryBuilder, aggregationBuilder,
    			Arrays.asList(IndexType.event), new String[] {"t", "u", "ct", "tx", "txnId", "ti", "id", "v", "n"});

    	Terms terms = response.getAggregations().get("users_agg");
    	Collection<Terms.Bucket> buckets = terms.getBuckets();
    	for(Terms.Bucket bucket : buckets)
    	{
    		logger.debug("userAggregation bucket " + bucket.getKey() + " = " + bucket.getDocCount());

        	QueryBuilder queryBuilder1 = QueryBuilders.matchPhraseQuery("locations", bucket.getKey());

        	SearchResponse response1 = client.search("alfresco", queryBuilder1, null, Arrays.asList(IndexType.node), new String[] {"id","n","v"});
        	for(SearchHit hit : response1.getHits().hits())
        	{
        		SearchHitField field = hit.field("id");
        		String nodeId = (field != null ? (String)field.getValue() : "null");

        		field = hit.field("n");
        		Integer nodeInternalId = (field != null ? (Integer)field.getValue() : -1);

        		logger.debug("userAggregation hit " + nodeInternalId + ", " + nodeId);
        	}
    	}
	}

	@Test
	public void testUsernameAggregations() throws Exception
	{
		logger.debug("testUsernameAggregations =======================");

    	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

    	DateHistogramBuilder aggregationBuilder = AggregationBuilders
    			.dateHistogram("event_dates_agg")
    			.field("ti")
    			.interval(1000*60*60)
    			.subAggregation(AggregationBuilders
    					.terms("event_usernames")
    					.field("u"));

    	SearchResponse response = client.search("alfresco", queryBuilder, aggregationBuilder,
    			Arrays.asList(IndexType.event), new String[] {"t", "u", "ct", "tx", "txnId", "ti", "id", "v", "n"});

    	DateHistogram dateHistogram = response.getAggregations().get("event_dates_agg");
    	List<? extends DateHistogram.Bucket> buckets = dateHistogram.getBuckets();
    	for(DateHistogram.Bucket bucket : buckets)
    	{
    		DateTime date = bucket.getKeyAsDate();
    		long dateBucketCount = bucket.getDocCount();
    		logger.debug("dates histogram bucket " + date + " = " + dateBucketCount);

    		Aggregations aggregations = bucket.getAggregations();

        	Terms terms = aggregations.get("event_usernames");;
        	Collection<Terms.Bucket> usernameBuckets = terms.getBuckets();
        	for(Terms.Bucket usernameBucket : usernameBuckets)
        	{
        		logger.debug("username bucket " + usernameBucket.getKey() + " = " + usernameBucket.getDocCount());
        	}
    	}
	}

	@Test
	public void testSitesAggregation() throws Exception
	{
		logger.debug("testSitesAggregation =======================");

    	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

    	TermsBuilder aggregationBuilder = AggregationBuilders
    			.terms("event_sites_agg")
    			.field("s");

    	SearchResponse response = client.search("alfresco", queryBuilder, aggregationBuilder,
    			Arrays.asList(IndexType.event), new String[] {"t", "u", "ct", "tx", "txnId", "ti", "id", "v", "n"});

    	Terms terms = response.getAggregations().get("event_sites_agg");
    	List<Terms.Bucket> buckets = terms.getBuckets();
    	for(Terms.Bucket bucket : buckets)
    	{
    		String siteId = bucket.getKey();
    		long count = bucket.getDocCount();
    		logger.debug("sites bucket " + siteId + " = " + count);
    	}
	}

	@Test
	public void testEventTypesAggregation() throws Exception
	{
		logger.debug("testEventTypesAggregation =======================");

    	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

    	TermsBuilder aggregationBuilder = AggregationBuilders
    			.terms("event_types_agg")
    			.field("t");

    	SearchResponse response = client.search("alfresco", queryBuilder, aggregationBuilder,
    			Arrays.asList(IndexType.event), new String[] {"t", "u", "ct", "tx", "txnId", "ti", "id", "v", "n", "ne"});

    	Terms terms = response.getAggregations().get("event_types_agg");
    	List<Terms.Bucket> buckets = terms.getBuckets();
    	for(Terms.Bucket bucket : buckets)
    	{
    		String siteId = bucket.getKey();
    		long count = bucket.getDocCount();
    		logger.debug("types bucket " + siteId + " = " + count);
    	}
	}
}
