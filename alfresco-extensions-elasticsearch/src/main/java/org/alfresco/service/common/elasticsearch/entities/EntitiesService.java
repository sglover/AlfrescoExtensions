/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.common.elasticsearch.ElasticSearchClient;
import org.alfresco.service.common.elasticsearch.IndexType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

/**
 * 
 * @author sglover
 *
 */
public class EntitiesService
{
    private static final Log logger = LogFactory.getLog(EntitiesService.class);

	private ElasticSearchClient elasticSearchClient;
	
	public EntitiesService(ElasticSearchClient elasticSearchClient)
	{
		this.elasticSearchClient = elasticSearchClient;
	}

	private Map<String, ElasticSearchEntity> findEntities(QueryBuilder queryBuilder, Set<EntityType> entityTypes)
	{
		Map<String, ElasticSearchEntity> entities = new HashMap<>();

    	List<String> fields = new LinkedList<>();
    	fields.add("id");
    	fields.add("n");
    	fields.add("v");
    	if(entityTypes != null)
    	{
    		if(entityTypes.size() > 0)
    		{
		    	for(EntityType entityType : entityTypes)
		    	{
		        	fields.add(entityType.getName());
		    	}
    		}
    	}
    	else
    	{
        	fields.add(EntityType.names.getName());
        	fields.add(EntityType.locations.getName());
        	fields.add(EntityType.orgs.getName());
        	fields.add(EntityType.dates.getName());
        	fields.add(EntityType.misc.getName());
        	fields.add(EntityType.money.getName());
    	}

    	SearchResponse response = elasticSearchClient.search("alfresco", queryBuilder, null,
    			Arrays.asList(IndexType.node), fields.toArray(new String[0]));
//        SearchRequestBuilder builder = elasticSearchClient.prepareSearch("alfresco")
//    	        .addFields(fields.toArray(new String[0]))
//    	        .setTypes(IndexType.node.getName());
//        builder = builder
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setQuery(queryBuilder)             // Query
//                .setFrom(0).setSize(60);

//        SearchResponse response = builder.execute()
//                .actionGet();
        for(SearchHit hit : response.getHits().hits())
        {
        	SearchHitField field = hit.field("id");
        	String nodeId = (field != null ? (String)field.getValue() : "null");
        	
        	field = hit.field("n");
        	Integer idxNodeInternalId = (field != null ? (Integer)field.getValue() : -1);

        	logger.debug("testEntities hit " + idxNodeInternalId + ", " + nodeId + ", " + field.values());

        	field = hit.field("names");
        	if(field != null)
        	{
	        	ElasticSearchEntity names = new ElasticSearchEntity("names", field.values());
	        	entities.put("names", names);
        	}

        	field = hit.field("money");
        	if(field != null)
        	{
	        	ElasticSearchEntity names = new ElasticSearchEntity("money", field.values());
	        	entities.put("money", names);
        	}

        	field = hit.field("misc");
        	if(field != null)
        	{
	        	ElasticSearchEntity names = new ElasticSearchEntity("misc", field.values());
	        	entities.put("misc", names);
        	}

        	field = hit.field("locations");
        	if(field != null)
        	{
	        	ElasticSearchEntity locations = new ElasticSearchEntity("locations", field.values());
	        	entities.put("locations", locations);
        	}

        	field = hit.field("orgs");
        	if(field != null)
        	{
	        	ElasticSearchEntity orgs = new ElasticSearchEntity("orgs", field.values());
	        	entities.put("orgs", orgs);
        	}

        	field = hit.field("dates");
        	if(field != null)
        	{
	        	ElasticSearchEntity dates = new ElasticSearchEntity("dates", field.values());
	        	entities.put("dates", dates);
        	}
        }

        return entities;
	}

	public Map<String, ElasticSearchEntity> entities(String nodeId, Set<EntityType> entityTypes)
	{
    	QueryBuilder queryBuilder = QueryBuilders.boolQuery()
    			.must(QueryBuilders.matchQuery("nid", nodeId));
    	return findEntities(queryBuilder, entityTypes);
	}

	public Map<String, ElasticSearchEntity> entities(long nodeInternalId/*, long nodeVersion*/, Set<EntityType> entityTypes)
	{
    	QueryBuilder queryBuilder = QueryBuilders.boolQuery()
    			.must(QueryBuilders.termQuery("n", nodeInternalId));
//    			.must(QueryBuilders.matchQuery("v", nodeVersion));
    	return findEntities(queryBuilder, entityTypes);
	}
}
