/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities;

import java.io.IOException;
import java.util.Collection;

import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.alfresco.services.nlp.EntityExtracter;
import org.alfresco.services.nlp.EntityTaggerCallback;
import org.json.JSONException;

/**
 * 
 * @author sglover
 *
 */
public class EntitiesServiceImpl implements EntitiesService
{
	private EntitiesDAO entitiesDAO;
//    private AlfrescoApi alfrescoApi;
//    private ContentGetter contentGetter;
	private EntityExtracter entityExtracter;

    public EntitiesServiceImpl()
    {
    }

    public void setEntitiesDAO(EntitiesDAO entitiesDAO)
	{
		this.entitiesDAO = entitiesDAO;
	}

    public void setEntityExtracter(EntityExtracter entityExtracter)
	{
		this.entityExtracter = entityExtracter;
	}

	public void init() throws AuthenticationException, IOException, JSONException
    {
//    	this.alfrescoApi = new AlfrescoApi();
//		this.contentGetter = alfrescoApi.getContentGetter();
    }

    @Override
    public void getEntities(final NodeEvent nodeEvent) throws AuthenticationException, IOException
    {
//    	InputStream is = contentGetter.getTextContent(nodeId);

    	final long nodeInternalId = nodeEvent.getNodeInternalId();
    	final long nodeVersion = nodeEvent.getNodeVersion();
    	final String nodeType = nodeEvent.getNodeType();

//    	if(is != null)
//    	{
	    	EntityTaggerCallback callback = new EntityTaggerCallback()
			{
				
				@Override
				public void onSuccess(Entities entities)
				{
	//				Collection<Entity<String>> namesEntities = entities.getNames();
	//				Set<String> names = new HashSet<>();
	//				for(Entity<String> nameEntity : namesEntities)
	//				{
	//					String name = nameEntity.getEntity();
	////					List<EntityLocation> locations = nameEntity.getLocations();
	//					names.add(name);
	//				}

					entitiesDAO.addEntities(nodeInternalId, nodeVersion, entities);
	//				Serializable namesProperty = entitiesService.getNamesProperty(names);
	//				putRawValue("names", namesProperty, rawProperties);
	
				}
				
				@Override
				public void onFailure(Throwable ex)
				{
					// TODO Auto-generated method stub
					
				}
			};
	
	//        InputStreamReader r = (in != null ? new InputStreamReader(in) : null);
	
	//        if(r != null)
	//        {
//	        	String content = IOUtils.toString(is, "UTF-8");
		        entityExtracter.getEntities(nodeInternalId, nodeVersion, nodeType, callback);
	//        }
//    	}
    }

	@Override
    public Collection<Entity<String>> getNames(long nodeId, long nodeVersion)
    {
	    return entitiesDAO.getNames(nodeId, nodeVersion);
    }

}
