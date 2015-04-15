/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch.entities;


/**
 * 
 * @author sglover
 *
 */
public class ElasticSearchEntitiesGetter //implements EntitiesGetter
{
//    private static final Log logger = LogFactory.getLog(ElasticSearchEntitiesGetter.class);
//
//	private ElasticSearchClient elasticSearchClient;
//	
//	public ElasticSearchEntitiesGetter(ElasticSearchClient elasticSearchClient)
//	{
//		this.elasticSearchClient = elasticSearchClient;
//	}
//
//	private Entities findEntities(QueryBuilder queryBuilder, Set<EntityType> entityTypes)
//	{
////		Map<String, ElasticSearchEntity> entities = new HashMap<>();
//		Entities entities = Entities.empty();
//
//    	List<String> fields = new LinkedList<>();
//    	fields.add("id");
//    	fields.add("n");
//    	fields.add("v");
//    	if(entityTypes != null)
//    	{
//    		if(entityTypes.size() > 0)
//    		{
//		    	for(EntityType entityType : entityTypes)
//		    	{
//		        	fields.add(entityType.getName());
//		    	}
//    		}
//    	}
//    	else
//    	{
//        	fields.add(EntityType.names.getName());
//        	fields.add(EntityType.locations.getName());
//        	fields.add(EntityType.orgs.getName());
//        	fields.add(EntityType.dates.getName());
//        	fields.add(EntityType.misc.getName());
//        	fields.add(EntityType.money.getName());
//    	}
//
//    	SearchResponse response = elasticSearchClient.search("alfresco", queryBuilder, null,
//    			Arrays.asList(IndexType.node), fields.toArray(new String[0]));
////        SearchRequestBuilder builder = elasticSearchClient.prepareSearch("alfresco")
////    	        .addFields(fields.toArray(new String[0]))
////    	        .setTypes(IndexType.node.getName());
////        builder = builder
////                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
////                .setQuery(queryBuilder)             // Query
////                .setFrom(0).setSize(60);
//
////        SearchResponse response = builder.execute()
////                .actionGet();
//
//        for(SearchHit hit : response.getHits().hits())
//        {
//        	SearchHitField field = hit.field("id");
//        	String nodeId = (field != null ? (String)field.getValue() : "null");
//        	
//        	field = hit.field("n");
//        	Integer idxNodeInternalId = (field != null ? (Integer)field.getValue() : -1);
//
//        	logger.debug("testEntities hit " + idxNodeInternalId + ", " + nodeId + ", " + field.values());
//
//        	field = hit.field("names");
//        	if(field != null)
//        	{
////	        	ElasticSearchEntity names = new ElasticSearchEntity("names", field.values());
////	        	entities.put("names", names);
//	        	for(Object value : field.values())
//	        	{
//	        		String name = (String)value;
//	        		entities.addName(name, null);
//	        	}
//        	}
//
//        	field = hit.field("money");
//        	if(field != null)
//        	{
////	        	ElasticSearchEntity names = new ElasticSearchEntity("money", field.values());
////	        	entities.put("money", names);
//	        	for(Object value : field.values())
//	        	{
//	        		String name = (String)value;
//	        		entities.addMoney(name, null);
//	        	}
//        	}
//
//        	field = hit.field("misc");
//        	if(field != null)
//        	{
////	        	ElasticSearchEntity names = new ElasticSearchEntity("misc", field.values());
////	        	entities.put("misc", names);
//        		for(Object value : field.values())
//	        	{
//	        		String name = (String)value;
//	        		entities.addMisc(name, null);
//	        	}
//        	}
//
//        	field = hit.field("locations");
//        	if(field != null)
//        	{
////	        	ElasticSearchEntity locations = new ElasticSearchEntity("locations", field.values());
////	        	entities.put("locations", locations);
//        		for(Object value : field.values())
//	        	{
//	        		String name = (String)value;
//	        		entities.addLocation(name, null);
//	        	}
//        	}
//
//        	field = hit.field("orgs");
//        	if(field != null)
//        	{
////	        	ElasticSearchEntity orgs = new ElasticSearchEntity("orgs", field.values());
////	        	entities.put("orgs", orgs);
//        		for(Object value : field.values())
//	        	{
//	        		String name = (String)value;
//	        		entities.addOrg(name, null);
//	        	}
//        	}
//
//        	field = hit.field("dates");
//        	if(field != null)
//        	{
////	        	ElasticSearchEntity dates = new ElasticSearchEntity("dates", field.values());
////	        	entities.put("dates", dates);
//        		for(Object value : field.values())
//	        	{
//	        		String name = (String)value;
//	        		entities.addDate(name, null);
//	        	}
//        	}
//        }
//
//        return entities;
//	}
//
//	@Override
//	public Entities entities(String nodeId, Set<EntityType> entityTypes)
//	{
//    	QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//    			.must(QueryBuilders.matchQuery("nid", nodeId));
//    	return findEntities(queryBuilder, entityTypes);
//	}
//
//	@Override
//	public Entities entities(long nodeInternalId/*, long nodeVersion*/, Set<EntityType> entityTypes)
//	{
//    	QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//    			.must(QueryBuilders.termQuery("n", nodeInternalId));
////    			.must(QueryBuilders.matchQuery("v", nodeVersion));
//    	return findEntities(queryBuilder, entityTypes);
//	}
//
//	private void addEntities(XContentBuilder xb, String fieldName, Collection<Entity<String>> entities) throws IOException
//	{
//		xb.startArray(fieldName);
//		for(Entity<String> entity : entities)
//		{
////            xb
////            	.startObject()
////            	.field("n", entity.getEntity())
////            	.field("c", entity.getCount())
////            	.endObject();
//            xb
//            	.startObject()
//            	.field("n", entity.getEntity())
//            	.field("c", entity.getCount())
//            	.endObject();
//		}
//		xb.endArray();
//	}
//
//	public XContentBuilder toBuilder(Entities namedEntities) throws IOException
//	{
//        XContentBuilder xb = jsonBuilder()
//                .startObject();
//        addEntities(xb, "names", namedEntities.getNames());
//        addEntities(xb, "locations", namedEntities.getLocations());
//        addEntities(xb, "dates", namedEntities.getDates());
//        addEntities(xb, "orgs", namedEntities.getOrgs());
//        addEntities(xb, "money", namedEntities.getMoney());
//        addEntities(xb, "misc", namedEntities.getMisc());
//        xb.endObject();
//
//        return xb;
//	}
}
