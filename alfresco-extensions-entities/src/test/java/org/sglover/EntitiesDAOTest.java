/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover;

import java.util.UUID;

import org.alfresco.service.common.mongo.MongoDbFactory;
import org.junit.Before;
import org.junit.Test;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.EntitiesDAO;
import org.sglover.entities.dao.mongo.MongoEntitiesDAO;
import org.sglover.nlp.Entities;

import com.mongodb.DB;

/**
 * 
 * @author sglover
 *
 */
public class EntitiesDAOTest
{
    private EntitiesDAO entitiesDAO;

    @Before
    public void before() throws Exception
    {
        boolean useEmbeddedMongo = ("true".equals(System.getProperty("useEmbeddedMongo")) ? true
                : false);
        final MongoDbFactory factory = new MongoDbFactory(true, null, "test", useEmbeddedMongo);
        final DB db = factory.createInstance();

        this.entitiesDAO = new MongoEntitiesDAO(db, "entities" + System.currentTimeMillis());
    }

    @Test
    public void test1() throws Exception
    {
        Entities entities1 = Entities.empty()
                .addName("Steve Glover", "Steve Glover works for Alfresco", 1.0)
                .addOrg("Alfresco", "Steve Glover works for Alfresco", 1.0);

        Entities entities2 = Entities.empty().addName("Steve Glover",
                "Steve Glover works for Alfresco", 1.0);

        Entities entities3 = Entities.empty().addOrg("Alfresco", "Steve Glover works for Alfresco",
                1.0);

        String txnId = UUID.randomUUID().toString();
        String node1Id = "1";
        String node1Version = "1";
        String node2Id = "2";
        String node2Version = "2";
        String node3Id = "3";
        String node3Version = "3";

        entitiesDAO.addEntities(Node.build().nodeId(node1Id).versionLabel(node1Version), entities1);
        entitiesDAO.addEntities(Node.build().nodeId(node2Id).versionLabel(node2Version), entities2);
        entitiesDAO.addEntities(Node.build().nodeId(node3Id).versionLabel(node3Version), entities3);

        // EntityCounts<String> counts = entitiesDAO.overlap(node1InternalId,
        // node1Version);
        // System.out.println(counts);
    }
}
