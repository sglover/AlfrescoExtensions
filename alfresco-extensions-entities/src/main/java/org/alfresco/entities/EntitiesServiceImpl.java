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
import org.alfresco.extensions.common.Node;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.minhash.MinHash;
import org.alfresco.services.minhash.MinHashImpl;
import org.alfresco.services.nlp.CoreNLPEntityTagger;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.Entity;
import org.alfresco.services.nlp.EntityExtracter;
import org.alfresco.services.nlp.EntityTagger;
import org.alfresco.services.nlp.EntityTaggerCallback;
import org.alfresco.services.nlp.ModelLoader;
import org.alfresco.services.nlp.StanfordEntityTagger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.util.EqualsHelper;

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
	private EntityTagger entityTagger;
	private EntityExtracter entityExtracter;
	private ModelLoader modelLoader;
	private ExecutorService executorService;

	public EntitiesServiceImpl(String extracterTypeStr, ModelLoader modelLoader,
			EntitiesDAO entitiesDAO, SimilarityDAO similarityDAO, ContentGetter contentGetter)
	{
		this.modelLoader = modelLoader;
		ExtracterType extracterType = ExtracterType.valueOf(extracterTypeStr);
		this.entityTagger = buildEntityTagger(extracterType);
		this.entitiesDAO = entitiesDAO;
		this.similarityDAO = similarityDAO;
		this.executorService = Executors.newFixedThreadPool(10);
		this.entityExtracter = buildEntityExtracter(executorService, entityTagger, contentGetter);
	}

	public static enum ExtracterType
	{
		CoreNLP, StanfordNLP;
	};

	private EntityTagger buildEntityTagger(ExtracterType extracterType)
	{
		EntityTagger entityTagger = null;

        logger.debug("extracterType = " + extracterType);

        switch(extracterType)
        {
        case CoreNLP:
        {
    		entityTagger = new CoreNLPEntityTagger(modelLoader, 8);
        	break;
        }
        case StanfordNLP:
        {
        	entityTagger = StanfordEntityTagger.build();
        	break;
        }
        default:
        	throw new IllegalArgumentException("Invalid entity.extracter.type");
        }

        return entityTagger;
	}

	private EntityExtracter buildEntityExtracter(ExecutorService executorService, EntityTagger entityTagger,
			ContentGetter contentGetter)
	{
		EntityExtracter entityExtracter = new EntityExtracter(contentGetter, entityTagger, executorService);
        return entityExtracter;
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

    @Override
    public void calculateSimilarities(String txnId)
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
		Set<String> locations1 = entities1.getLocationsAsSet();
		Node node2 = new Node(nodeId2, nodeVersion2);
		Entities entities2 = entitiesDAO.getEntities(node2, null);
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
	public void getEntities(final Node node) throws AuthenticationException, IOException
	{
		EntityTaggerCallback callback = new EntityTaggerCallback()
		{
			@Override
			public void onSuccess(Entities entities)
			{
				logger.debug("Got entities for node " + node + ", " + entities);
				entitiesDAO.addEntities(null, node, entities);
			}

			@Override
			public void onFailure(Throwable ex)
			{
				logger.error(ex);
			}
		};
		entityExtracter.getEntities(node.getNodeInternalId(), callback);
	}

	@Override
	public void getEntitiesAsync(final Node node)
	{
		executorService.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					getEntities(node);
				}
				catch(AuthenticationException e)
				{
					// TODO
				}
				catch(IOException e)
				{
					// TODO
				}
			}
		});
	}

//	@Override
//	public void getEntities(String txnId, String nodeId, String nodeVersion, String content) throws IOException
//	{
//		Entities entities = entityTagger.getEntities(content);
//		if(entities != null)
//		{
//			final Node node = new Node(nodeId, nodeVersion);
//			entitiesDAO.addEntities(txnId, node, entities);
//		}
//	}

//	@Override
//	public void getEntities(String txnId, long nodeInternalId, String nodeId, String nodeVersion)
//			throws IOException, AuthenticationException
//	{
//		Entities entities = entityExtracter.getEntities(nodeId, nodeVersion);
//		if(entities != null)
//		{
//			final Node node = new Node(nodeId, nodeVersion);
//			entitiesDAO.addEntities(txnId, node, entities);
//
//			List<Entities> allEntities = entitiesDAO.getEntities();
//			for(Entities e : allEntities)
//			{
//				double similarity = similarity(entities, e);
//
//				Node node2 = new Node(e.getNodeId(), e.getNodeVersion());
//				similarityDAO.saveSimilarity(node, node2, similarity);
//			}
//		}
//	}

	private void getEntitiesForEventAsync(final String txnId, final long nodeInternalId, final String nodeId,
			final String nodeVersion) throws AuthenticationException, IOException
    {
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

		entityExtracter.getEntities(nodeInternalId, callback);
    }
}