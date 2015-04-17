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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.entities.dao.EntitiesDAO;
import org.alfresco.entities.dao.SimilarityDAO;
import org.alfresco.entities.values.Node;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.TransactionCommittedEvent;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.alfresco.services.nlp.EntityExtracter;
import org.alfresco.services.nlp.EntityTaggerCallback;
import org.alfresco.services.nlp.minhash.MinHash;
import org.alfresco.services.nlp.minhash.MinHashImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.util.EqualsHelper;
import org.json.JSONException;

/**
 * 
 * @author sglover
 *
 */
public class EntitiesServiceImpl implements EntitiesService
{
	private static final Log logger = LogFactory.getLog(EntitiesServiceImpl.class);

	private EntitiesDAO entitiesDAO;
	private SimilarityDAO similarityDAO;
	private EntityExtracter entityExtracter;

	private ExecutorService executorService;

	public EntitiesServiceImpl()
	{
		this.executorService = Executors.newFixedThreadPool(10);
	}

    public void setSimilarityDAO(SimilarityDAO similarityDAO)
	{
		this.similarityDAO = similarityDAO;
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
    	final String txnId = nodeEvent.getTxnId();
    	final String nodeId = nodeEvent.getNodeId();
    	final String nodeVersion = nodeEvent.getVersionLabel();
    	final String nodeType = nodeEvent.getNodeType();

    	EntityTaggerCallback callback = new EntityTaggerCallback()
		{
			
			@Override
			public void onSuccess(Entities entities)
			{
				Node node = new Node(nodeId, nodeVersion);
				entitiesDAO.addEntities(txnId, node, entities);

				List<Entities> allEntities = entitiesDAO.getEntities();
				for(Entities e : allEntities)
				{
					double similarity = similarity(entities, e);
	
					Node node2 = new Node(e.getNodeId(), e.getNodeVersion());
					similarityDAO.saveSimilarity(node, node2, similarity);
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
			final String txnId = nodeEvent.getTxnId();
			final Node node = new Node(nodeId, nodeVersion);
			entitiesDAO.addEntities(txnId, node, entities);
		}
    }

    private class CalculateSimilarities implements Runnable
    {
    	private String txnId;

    	private CalculateSimilarities(String txnId)
    	{
    		this.txnId = txnId;
    	}

    	public void run()
    	{
    		List<Entities> allEntities = entitiesDAO.getEntities();

        	List<Entities> txnEntities = entitiesDAO.getEntitiesForTxn(txnId);

        	for(Entities e : txnEntities)
        	{
    			for(Entities e1 : allEntities)
    			{
    				logger.debug("Computing similarity for " + e.getNodeId() + "." + e.getNodeVersion() + " and " + e1.getNodeId() + "." + e1.getNodeVersion());

    				boolean same = e.getNodeId().equals(e1.getNodeId());
    				if(same)
    				{
    					same = EqualsHelper.nullSafeEquals(e.getNodeVersion(), e1.getNodeVersion());
    				}
    				if(!same)
    				{
    					double similarity = similarity(e, e1);

    					logger.debug("Similarity for "
    							+ e1.getNodeId()+ "." + e1.getNodeVersion()
    							+ " and "
    							+ e.getNodeId() + "." + e.getNodeVersion()
    							+ " is " + similarity);
    	
    					Node node1 = new Node(e.getNodeId(), e.getNodeVersion());
    					Node node2 = new Node(e1.getNodeId(), e1.getNodeVersion());
    					similarityDAO.saveSimilarity(node1, node2, similarity);
    				}
    			}
        	}
    	}
    }

    private void calculateSimilarities(String txnId)
    {
    	CalculateSimilarities cs = new CalculateSimilarities(txnId);
    	executorService.execute(cs);
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
		double similarity = 0.0;

		Set<String> entitiesSet1 = entities1.getEntitiesAsSet();
		Set<String> entitiesSet2 = entities2.getEntitiesAsSet();
		if(entitiesSet1.size() > 0 && entitiesSet2.size() > 0)
		{
			MinHash<String> minHash = new MinHashImpl<String>(entitiesSet1.size()+entitiesSet2.size());
			similarity = minHash.similarity(entitiesSet1, entitiesSet2);
		}

		return similarity;
	}

	@Override
	public double getSimilarity(Node node1, Node node2)
	{
		return similarityDAO.getSimilarity(node1, node2);
	}

	@Override
	public void txnCommitted(TransactionCommittedEvent event)
	{
		entitiesDAO.txnCommitted(event);

		calculateSimilarities(event.getTxnId());
	}
}