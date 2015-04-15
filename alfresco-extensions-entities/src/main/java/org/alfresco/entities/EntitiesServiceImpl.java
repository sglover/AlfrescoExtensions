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
import java.util.List;
import java.util.Set;

import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.alfresco.services.nlp.EntityExtracter;
import org.alfresco.services.nlp.EntityTaggerCallback;
import org.alfresco.services.nlp.minhash.MinHash;
import org.alfresco.services.nlp.minhash.MinHashImpl;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.gytheio.util.EqualsHelper;
import org.json.JSONException;

/**
 * 
 * @author sglover
 *
 */
public class EntitiesServiceImpl implements EntitiesService
{
	private EntitiesDAO entitiesDAO;
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
    }

    @Override
    public void getEntitiesAsync(final NodeEvent nodeEvent) throws AuthenticationException, IOException
    {
    	final long nodeInternalId = nodeEvent.getNodeInternalId();
    	final String nodeId = nodeEvent.getNodeId();
    	final String nodeVersion = nodeEvent.getVersionLabel();
    	final String nodeType = nodeEvent.getNodeType();

    	EntityTaggerCallback callback = new EntityTaggerCallback()
		{
			
			@Override
			public void onSuccess(Entities entities)
			{
				Node node = new Node(nodeId, nodeVersion);
				entitiesDAO.addEntities(node, entities);

				List<Entities> allEntities = entitiesDAO.getEntities();
				for(Entities e : allEntities)
				{
					double similarity = similarity(entities, e);
	
					Node node2 = new Node(e.getNodeId(), e.getNodeVersion());
					entitiesDAO.saveSimilarity(node, node2, similarity);
				}
			}
			
			@Override
			public void onFailure(Throwable ex)
			{
				// TODO Auto-generated method stub
				
			}
		};

		entityExtracter.getEntities(nodeInternalId, nodeType, callback);
    }

    @Override
    public void getEntities(final NodeEvent nodeEvent) throws AuthenticationException, IOException
    {
    	final long nodeInternalId = nodeEvent.getNodeInternalId();
    	final String nodeId = nodeEvent.getNodeId();
    	final String nodeVersion = nodeEvent.getVersionLabel();
//    	final String nodeType = nodeEvent.getNodeType();

		Entities entities = entityExtracter.getEntities(nodeInternalId/*, nodeType*/);
		if(entities != null)
		{
			Node node = new Node(nodeId, nodeVersion);
			entitiesDAO.addEntities(node, entities);
	
			List<Entities> allEntities = entitiesDAO.getEntities();
			for(Entities e : allEntities)
			{
				boolean same = e.getNodeId().equals(nodeId);
				if(same)
				{
					same = EqualsHelper.nullSafeEquals(e.getNodeVersion(), nodeVersion);
				}
				if(!same)
				{
					double similarity = similarity(entities, e);
		
					Node node2 = new Node(e.getNodeId(), e.getNodeVersion());
					entitiesDAO.saveSimilarity(node, node2, similarity);
				}
			}
		}
    }

	@Override
	public Collection<Entity<String>> getNames(Node node)
	{
		return entitiesDAO.getNames(node);
	}

	public double similarity(String nodeId1, String nodeVersion1, String nodeId2, String nodeVersion2) throws Exception
	{
		Node node = new Node(nodeId1, nodeVersion1);
		Entities entities1 = entitiesDAO.getEntities(node, null);
//		Entities entities1 = getEntities(nodeInternalId1, null);
		Set<String> locations1 = entities1.getLocationsAsSet();
		Node node2 = new Node(nodeId2, nodeVersion2);
		Entities entities2 = entitiesDAO.getEntities(node2, null);
//		Entities entities2 = getEntities(nodeInternalId2, null);
		Set<String> locations2 = entities2.getLocationsAsSet();

		MinHash<String> minHash = new MinHashImpl<String>(locations1.size()+locations2.size());
		double similarity = minHash.similarity(locations1, locations2);
		return similarity;
	}

	public double similarity(Entities entities1, Entities entities2)
	{
		Set<String> locations1 = entities1.getLocationsAsSet();
		Set<String> locations2 = entities2.getLocationsAsSet();

		MinHash<String> minHash = new MinHashImpl<String>(locations1.size()+locations2.size());
		double similarity = minHash.similarity(locations1, locations2);
		return similarity;
	}

	@Override
    public double getSimilarity(Node node1, Node node2)
    {
		return entitiesDAO.getSimilarity(node1, node2);
    }
}
