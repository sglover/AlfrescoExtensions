/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.events.node.types.NodeContentGetEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

/**
 * 
 * @author sglover
 *
 */
public class ElasticSearchClient
{
    private static final Log logger = LogFactory.getLog(ElasticSearchClient.class);

    private Client client;
    private String indexName;

    public ElasticSearchClient(String clusterName, String indexName)
	{
    	Node node = nodeBuilder().clusterName(clusterName).node();
    	this.client = node.client();
        this.indexName = indexName;
	}

    @SuppressWarnings("resource")
    public ElasticSearchClient(String hostname, int port, String indexName)
	{
        this.client = new TransportClient()
        	.addTransportAddress(new InetSocketTransportAddress(hostname, port));
        this.indexName = indexName;
	}

    public ElasticSearchClient(Client client, String indexName)
	{
        this.client = client;
        this.indexName = indexName;
	}

	private void checkIndexes() throws IOException
	{
//        XContentBuilder nodeMapping = jsonBuilder().startObject()
//                .startObject(IndexType.node.getName())
//	                .startObject("properties")
//	                  .startObject("id")
//	                  	.field("type", "string")
//	                  	.field("store",true)
//	                  .endObject()
//	                  .startObject("nt")
//	                  	.field("type", "string")
//	                  	.field("store",true)
//	                  .endObject()
//	                .endObject()
//	            .endObject()
//             .endObject();
        XContentBuilder contentMapping = jsonBuilder().startObject()
                .startObject(IndexType.content.getName())
                  .startObject("properties")
                    .startObject("_n")
                    	.field("type", "entities")
                    	.field("store",true)
                    .endObject()
                    .startObject("id")
                    	.field("type", "string")
                    	.field("store",true)
                    .endObject()
                    .startObject("t")
                    	.field("type", "string")
                    	.field("store", true)
                    .endObject()
                    .startObject("n")
                    	.field("type", "long")
                    	.field("store", true)
                    .endObject()
                    .startObject("v")
                    	.field("type", "long")
                    	.field("store",true)
                    .endObject()
                    .startObject("c")
                    	.field("type", "string")
                    	.field("index", "analyzed")
                    	.field("store", true)
                    .endObject()
                  .endObject()
             .endObject();

//		.add("ct", changeTxnId)
//		.add("tx",  txnId)

        XContentBuilder eventMapping = jsonBuilder().startObject()
                .startObject(IndexType.event.getName())
                  .startObject("properties")
                    .startObject("t")
                    	.field("type", "string")
                    	.field("store",true)
                    .endObject()
                    .startObject("u")
                    	.field("type", "string")
                    	.field("store",true)
                    .endObject()
                    .startObject("ti")
                    	.field("type", "date")
                    	.field("store", true)
                    	.field("format", "basic_date_time")
                    .endObject()
                    .startObject("id")
                    	.field("type", "long")
                    	.field("store", true)
                    .endObject()
                    .startObject("v")
                    	.field("type", "long")
                    	.field("store",true)
                    .endObject()
                    .startObject("n")
                    	.field("type", "string")
                    	.field("store", true)
                    .endObject()
                  .endObject()
             .endObject();

        Map<String, XContentBuilder> mappings = new HashMap<>();
//        mappings.put(IndexType.node.getName(), nodeMapping);
        mappings.put(IndexType.content.getName(), contentMapping);
        mappings.put(IndexType.event.getName(), eventMapping);
        createIndex(indexName, false, mappings);
	}

    public void init(boolean checkIndexes) throws Exception
    {
        if(checkIndexes)
        {
        	checkIndexes();
        }
    }

    public void shutdown()
    {
        client.close();
    }

	public void deleteIndex(String name) throws IOException
	{
        if(indexExists(name))
        {
			DeleteIndexResponse delete = client.admin().indices().delete(
					new DeleteIndexRequest(name)).actionGet();
			if (!delete.isAcknowledged())
			{
				throw new RuntimeException("Index wasn't deleted");
			}
        }
        else
        {
        	logger.debug("Index " + name + " not deleted because it does not exist");
        }
	}

	public boolean indexExists(String name) throws IOException
	{
		boolean exists = false;

		ActionFuture<IndicesExistsResponse> res = client.admin().indices().exists(new IndicesExistsRequest(name));
        IndicesExistsResponse indicesExistsResp = res.actionGet(2000);
        if(indicesExistsResp.isExists())
        {
        	exists = true;
        }

        return exists;
	}

	public void createIndex(String name, boolean deleteIfExists,
			Map<String, XContentBuilder> mappings) throws IOException
	{
		boolean indexExists = indexExists(name);
		boolean createIndex = false;

        if(indexExists)
        {
        	if(deleteIfExists)
        	{
	        	logger.debug("Index " + name + " exists, deleting");
	        	deleteIndex(name);
	        	createIndex = true;
        	}
        }
        else
        {
        	logger.debug("Index " + name + " does not exist");
        	createIndex = true;
        }

        if(createIndex)
        {
	        CreateIndexRequestBuilder builder = client.admin().indices()
	        		.prepareCreate(name)
	        		.setSettings(
	        				ImmutableSettings.settingsBuilder()
	        				.put("number_of_shards", 1)
	        				.put("index.numberOfReplicas", 1));
	        for(Map.Entry<String, XContentBuilder> mapping : mappings.entrySet())
	        {
	        	builder.addMapping(mapping.getKey(), mapping.getValue());
	        }
	
	        CreateIndexResponse createIndexResp = builder.execute().actionGet();
	        
	        logger.debug("Created index " + name + ", " + createIndexResp);
	    }
	}
	
	public IndexResponse index(String indexName, String id, IndexType indexType, String json, boolean refresh)
	{
        IndexResponse response = client.prepareIndex(indexName, indexType.getName(), id)
        		.setRefresh(refresh)
                .setSource(json)
                .execute()
                .actionGet();
        return response;
	}

	public UpdateResponse reindex(String indexName, String id, IndexType indexType, String json, boolean refresh)
	{
        UpdateResponse response = client.prepareUpdate(indexName, indexType.getName(), id)
        		.setRefresh(refresh)
        		.setDocAsUpsert(true)
        		.setDoc(json)
                .execute()
                .actionGet();
        return response;
	}

	public UpdateResponse reindexContent(String indexName, String id, String json, boolean refresh)
	{
        UpdateResponse response = client.prepareUpdate(indexName, IndexType.content.getName(), id)
        		.setRefresh(refresh)
        		.setDocAsUpsert(true)
        		.setDoc(json)
                .execute()
                .actionGet();
        return response;
	}

//	public SearchResponse moreLikeThis()
//	{
//		QueryBuilders.moreLikeThisQuery("names")      // Fields
//        	.likeText("text like this one")                 // Text
//        	.minTermFreq(1);         
//
//        MoreLikeThisRequestBuilder builder = new MoreLikeThisRequestBuilder(client);
//        builder.setSearchIndices("alfresco");
//        builder.setField("names");
//        builder.set
//        MoreLikeThisRequest mltRequest = builder.request();
//        SearchResponse response = client.moreLikeThis(mltRequest).actionGet();
//        return response;
//	}

	public DeleteResponse unindex(String indexName, String id, IndexType indexType)
	{
        DeleteResponse response = client.prepareDelete(indexName, indexType.getName(), id)
        		.execute()
        		.actionGet();
        return response;
	}

	public SearchResponse namesSearch(String indexName, String... names)
	{
		QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        FilterBuilder filterBuilder = FilterBuilders.termsFilter("names", names);
        queryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);

        SearchRequestBuilder builder = client.prepareSearch(indexName)
    	        .addFields("id","n","v")
    	        .setSearchType(SearchType.QUERY_THEN_FETCH)
    	        .setQuery(queryBuilder)             // Query
    	        .setFrom(0).setSize(60);
        SearchResponse response = builder.execute().actionGet();
        return response;
	}

	public SearchResponse matchEvents(String indexName, String username, int skip, int maxItems)
	{
		String[] types = new String[] {IndexType.event.toString()};
		String[] fields = new String[] {"t", "u", "ct", "tx", "txnId", "ti", "id", "v", "n"};
    	QueryBuilder queryBuilder = QueryBuilders.boolQuery()
    			.must(QueryBuilders.matchQuery("u", username));
//    			.must(QueryBuilders.matchQuery("t", NodeContentGetEvent.EVENT_TYPE));

    	SortBuilder sortBuilder = SortBuilders
    			.fieldSort("ti").order(SortOrder.DESC);

        SearchRequestBuilder builder = client.prepareSearch(indexName)
        		.addFields(fields)
    	        .setTypes(types)
    	        .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(queryBuilder)             // Query
                .addSort(sortBuilder)
                .setFrom(skip).setSize(maxItems);
        SearchResponse response = builder.execute()
                .actionGet(); 
        return response;
	}

	public SearchResponse matchEventsForNode(String indexName, String nodeId, int skip, int maxItems)
	{
		String[] types = new String[] {IndexType.event.toString()};
		String[] fields = new String[] {"t", "u", "ct", "tx", "txnId", "ti", "id", "v", "n"};
    	QueryBuilder queryBuilder = QueryBuilders.boolQuery()
    			.must(QueryBuilders.matchQuery("n", nodeId))
    			.must(QueryBuilders.matchQuery("t", NodeContentGetEvent.EVENT_TYPE));

    	SortBuilder sortBuilder = SortBuilders
    			.fieldSort("ti").order(SortOrder.DESC);

        SearchRequestBuilder builder = client.prepareSearch(indexName)
        		.addFields(fields)
    	        .setTypes(types)
    	        .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(queryBuilder)             // Query
                .addSort(sortBuilder)
                .setFrom(skip).setSize(maxItems);
        SearchResponse response = builder.execute()
                .actionGet(); 
        return response;
	}

	public SearchResponse match(String indexName, String text)
	{
		String[] types = new String[] {IndexType.content.toString()};
		String[] fields = new String[] {"id","n","v","l","t"};
    	QueryBuilder queryBuilder = QueryBuilders.boolQuery()
    			.must(QueryBuilders.matchQuery("c", text));

        SearchRequestBuilder builder = client.prepareSearch(indexName)
        		.addFields(fields)
    	        .setTypes(types)
    	        .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(queryBuilder)             // Query
                .setFrom(0).setSize(60);
        SearchResponse response = builder.execute()
                .actionGet(); 
        return response;
	}

	public SearchResponse search(String indexName, QueryBuilder queryBuilder, AggregationBuilder aggregation,
			List<IndexType> types, String[] fields)
	{
//        logger.debug("Searching index " + indexName);

//    	queryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);

        List<String> typesList = new ArrayList<>();
        if(types != null && types.size() > 0)
        {
        	typesList = new ArrayList<>(types.size());
        	for(IndexType type : types)
        	{
        		typesList.add(type.getName());
        	}
//            logger.debug("Searching types " + typesList);
        }

        SearchRequestBuilder builder = client.prepareSearch(indexName)
        		.addFields(fields)
//    	        .addFields("id","n","v")
//    	        .setPostFilter(postFilter)
    	        .setTypes(typesList.toArray(new String[0]));
        if(aggregation != null)
        {
        	builder = builder.addAggregation(aggregation);
        }

        builder = builder
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(queryBuilder)             // Query
                .setFrom(0).setSize(60);

//		.addHighlightedField("c")
        //.setPostFilter(FilterBuilders.rangeFilter("age").from(12).to(18))   // Filter
        //                .setExplain(true);

        SearchResponse response = builder.execute()
                .actionGet(); 

        return response;
	}
}
