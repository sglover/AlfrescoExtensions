/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.common.elasticsearch.entities;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Collection;

import org.alfresco.service.common.elasticsearch.IndexType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;
import org.sglover.nlp.EntityTaggerCallback;

/**
 * Entity tagger callback. Persists extracted entities in ElasticSearch.
 * 
 * @author sglover
 *
 */
public class EntityTaggerCallbackImpl implements EntityTaggerCallback
{
	private static final Log logger = LogFactory.getLog(EntityTaggerCallbackImpl.class);

	private Client client;
	private long nodeInternalId;
	private long nodeVersion;
	private String versionLabel;
	private String indexId;
//	private ElasticSearchEntitiesGetter entitiesService;

	public EntityTaggerCallbackImpl(Client client, /*ElasticSearchEntitiesGetter entitiesService,*/
			long nodeInternalId, long nodeVersion, String versionLabel,
	        String indexId)
	{
//		this.entitiesService = entitiesService;
		this.client = client;
		this.nodeInternalId = nodeInternalId;
		this.nodeVersion = nodeVersion;
		this.versionLabel = versionLabel;
		this.indexId = indexId;
	}

	private void addEntities(XContentBuilder xb, String fieldName, Collection<Entity<String>> entities) throws IOException
	{
		xb.startArray(fieldName);
		for(Entity<String> entity : entities)
		{
//            xb
//            	.startObject()
//            	.field("n", entity.getEntity())
//            	.field("c", entity.getCount())
//            	.endObject();
            xb
            	.startObject()
            	.field("n", entity.getEntity())
            	.field("c", entity.getCount())
            	.endObject();
		}
		xb.endArray();
	}

	private XContentBuilder toBuilder(Entities namedEntities) throws IOException
	{
        XContentBuilder xb = jsonBuilder()
                .startObject();
        addEntities(xb, "names", namedEntities.getNames());
        addEntities(xb, "locations", namedEntities.getLocations());
        addEntities(xb, "dates", namedEntities.getDates());
        addEntities(xb, "orgs", namedEntities.getOrgs());
        addEntities(xb, "money", namedEntities.getMoney());
        addEntities(xb, "misc", namedEntities.getMisc());
        xb.endObject();

        return xb;
	}

	@Override
    public void onSuccess(Entities namedEntities)
    {
        logger.debug(namedEntities.toString());

        try
        {
        	XContentBuilder xb = toBuilder(namedEntities);

            UpdateResponse response = client.prepareUpdate("alfresco", IndexType.node.toString(), indexId)
            	.setDoc(xb)
            	.setDocAsUpsert(true)
            	.execute()
            	.actionGet();

            logger.debug("Indexed entities "
            		+ ", nodeInternalId = " + nodeInternalId
            		+ ", nodeVersion = " + nodeVersion
            		+ "response " + response.getId() + ", " + response.getType() + ", "
                    + response.getIndex() + ", " + response.getVersion());
        }
        catch(Throwable t)
        {
        	logger.warn("", t);
        }
    }

	@Override
    public void onFailure(Throwable ex)
    {
        logger.warn("Entity extraction failed", ex);
    }
}
