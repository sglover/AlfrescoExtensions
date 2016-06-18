/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.alfresco.services.minhash.MinHash;
import org.alfresco.services.minhash.MinHashImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.EntitiesDAO;
import org.sglover.entities.dao.SimilarityDAO;
import org.sglover.nlp.CoreNLPEntityTagger;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;
import org.sglover.nlp.EntityExtracter;
import org.sglover.nlp.EntityTagger;
import org.sglover.nlp.EntityTaggerCallback;
import org.sglover.nlp.ModelLoader;
import org.sglover.nlp.StanfordEntityTagger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 
 * @author sglover
 *
 */
@Component
public class EntitiesServiceImpl implements EntitiesService
{
    private static final Log logger = LogFactory.getLog(EntitiesServiceImpl.class);

    @Autowired
    @Qualifier(value="titanEntitiesDAO")
    private EntitiesDAO entitiesDAO;

    @Autowired
    @Qualifier(value="titanSimilarityDAO")
    private SimilarityDAO similarityDAO;

    @Autowired()
    @Qualifier(value="coreNLPEntityTagger")
    private EntityTagger entityTagger;

    @Autowired()
    private EntityExtracter entityExtracter;

    @Autowired
    private ModelLoader modelLoader;

    public EntitiesServiceImpl()
    {
    }

    public EntitiesServiceImpl(String entityTaggerTypeStr, ModelLoader modelLoader,
            EntitiesDAO entitiesDAO, SimilarityDAO similarityDAO)
    {
        this.modelLoader = modelLoader;
        this.entitiesDAO = entitiesDAO;
        this.similarityDAO = similarityDAO;
        EntityTaggerType entityTaggerType = EntityTaggerType.valueOf(entityTaggerTypeStr);
        this.entityTagger = buildEntityTagger(entityTaggerType);
        this.entityExtracter = buildEntityExtracter(entityTagger);
    }

    @PostConstruct
    public void init()
    {
    }

    public static enum EntityTaggerType
    {
        CoreNLP, StanfordNLP;
    };

    private EntityTagger buildEntityTagger(EntityTaggerType entityTaggerType)
    {
        EntityTagger entityTagger = null;

        logger.debug("entityTaggerType = " + entityTaggerType);

        switch (entityTaggerType)
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
            throw new IllegalArgumentException("Invalid entityTaggerType");
        }

        return entityTagger;
    }

    private EntityExtracter buildEntityExtracter(EntityTagger entityTagger)
    {
        EntityExtracter entityExtracter = new EntityExtracter(entityTagger);
        return entityExtracter;
    }

//    private class CalculateSimilarities implements Runnable
//    {
//        private String txnId;
//
//        private CalculateSimilarities(String txnId)
//        {
//            this.txnId = txnId;
//        }
//
//        public void run()
//        {
//            List<Entities> allEntities = entitiesDAO.getEntities();
//
//            List<Entities> txnEntities = entitiesDAO.getEntitiesForTxn(txnId);
//
//            for (Entities e : txnEntities)
//            {
//                for (Entities e1 : allEntities)
//                {
//                    logger.debug("Computing similarity for " + e.getNodeId()
//                            + "." + e.getNodeVersion() + " and "
//                            + e1.getNodeId() + "." + e1.getNodeVersion());
//
//                    boolean same = e.getNodeId().equals(e1.getNodeId());
//                    if (same)
//                    {
//                        same = EqualsHelper.nullSafeEquals(e.getNodeVersion(),
//                                e1.getNodeVersion());
//                    }
//                    if (!same)
//                    {
//                        double similarity = similarity(e, e1);
//
//                        logger.debug("Similarity for " + e1.getNodeId() + "."
//                                + e1.getNodeVersion() + " and " + e.getNodeId()
//                                + "." + e.getNodeVersion() + " is "
//                                + similarity);
//
//                        Node node1 = new Node(e.getNodeId(),
//                                e.getNodeVersion());
//                        Node node2 = new Node(e1.getNodeId(),
//                                e1.getNodeVersion());
//                        similarityDAO.saveSimilarity(node1, node2, similarity);
//                    }
//                }
//            }
//        }
//    }

//    @Override
//    public void calculateSimilarities(String txnId)
//    {
//        CalculateSimilarities cs = new CalculateSimilarities(txnId);
//        cs.run();
////        executorService.execute(cs);
//    }

    @Override
    public Stream<Entity<String>> getNames(Node node)
    {
        return entitiesDAO.getNames(node);
    }

    @Override
    public Stream<Entity<String>> getOrgs(Node node)
    {
        return entitiesDAO.getOrgs(node);
    }

    public double similarity(String nodeId1, String nodeVersion1,
            String nodeId2, String nodeVersion2) throws Exception
    {
        Node node = new Node(nodeId1, nodeVersion1);
        Entities entities1 = entitiesDAO.getEntities(node);
        Set<String> locations1 = entities1.getLocationsAsSet();
        Node node2 = new Node(nodeId2, nodeVersion2);
        Entities entities2 = entitiesDAO.getEntities(node2);
        Set<String> locations2 = entities2.getLocationsAsSet();

        MinHash<String> minHash = new MinHashImpl<String>(
                locations1.size() + locations2.size());
        double similarity = minHash.similarity(locations1, locations2);
        return similarity;
    }

    public double similarity(Entities entities1, Entities entities2)
    {
        double similarity = 0.0;

        Set<String> entitiesSet1 = entities1.getEntitiesAsSet();
        Set<String> entitiesSet2 = entities2.getEntitiesAsSet();
        if (entitiesSet1.size() > 0 && entitiesSet2.size() > 0)
        {
            MinHash<String> minHash = new MinHashImpl<String>(
                    entitiesSet1.size() + entitiesSet2.size());
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
    public void getEntities(final Node node, final ReadableByteChannel channel) throws IOException
    {
        EntityTaggerCallback callback = new EntityTaggerCallback()
        {
            @Override
            public void onSuccess(Entities entities)
            {
                logger.debug("Got entities for node " + node + ", " + entities);
                entitiesDAO.addEntities(node, entities);
            }

            @Override
            public void onFailure(Throwable ex)
            {
                logger.error(ex);
            }
        };
        entityExtracter.getEntities(node, channel, callback);
    }

//    @Override
//    public void getEntitiesAsync(final Node node, final ReadableByteChannel channel)
//    {
//        executorService.submit(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try
//                {
//                    getEntities(node, channel);
//                }
//                catch (IOException e)
//                {
//                    // TODO
//                }
//            }
//        });
//    }

    // @Override
    // public void getEntities(String txnId, String nodeId, String nodeVersion,
    // String content) throws IOException
    // {
    // Entities entities = entityTagger.getEntities(content);
    // if(entities != null)
    // {
    // final Node node = new Node(nodeId, nodeVersion);
    // entitiesDAO.addEntities(txnId, node, entities);
    // }
    // }

    // @Override
    // public void getEntities(String txnId, long nodeInternalId, String nodeId,
    // String nodeVersion)
    // throws IOException, AuthenticationException
    // {
    // Entities entities = entityExtracter.getEntities(nodeId, nodeVersion);
    // if(entities != null)
    // {
    // final Node node = new Node(nodeId, nodeVersion);
    // entitiesDAO.addEntities(txnId, node, entities);
    //
    // List<Entities> allEntities = entitiesDAO.getEntities();
    // for(Entities e : allEntities)
    // {
    // double similarity = similarity(entities, e);
    //
    // Node node2 = new Node(e.getNodeId(), e.getNodeVersion());
    // similarityDAO.saveSimilarity(node, node2, similarity);
    // }
    // }
    // }

//    private void getEntitiesForEventAsync(final String txnId,
//            final long nodeInternalId, final String nodeId,
//            final long nodeVersion)
//                    throws AuthenticationException, IOException
//    {
//        EntityTaggerCallback callback = new EntityTaggerCallback()
//        {
//
//            @Override
//            public void onSuccess(Entities entities)
//            {
//                Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
//                entitiesDAO.addEntities(txnId, node, entities);
//
//                List<Entities> allEntities = entitiesDAO.getEntities();
//                for (Entities e : allEntities)
//                {
//                    double similarity = similarity(entities, e);
//
//                    Node node2 = new Node(e.getNodeId(), e.getNodeVersion());
//                    similarityDAO.saveSimilarity(node, node2, similarity);
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable ex)
//            {
//                // TODO Auto-generated method stub
//
//            }
//        };
//
//        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
//        entityExtracter.getEntities(node, channel, callback);
//    }
}