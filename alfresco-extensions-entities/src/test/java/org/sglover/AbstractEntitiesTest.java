/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover;

import org.alfresco.service.common.mongo.MongoDbFactory;

import com.mongodb.DB;

/**
 * 
 * @author sglover
 *
 */
public class AbstractEntitiesTest
{
    protected DB db;

    public void init() throws Exception
    {
        boolean useEmbeddedMongo = ("true".equals(System.getProperty("useEmbeddedMongo")) ? true
                : false);
        final MongoDbFactory factory = new MongoDbFactory(true, null, "test", useEmbeddedMongo);
        this.db = factory.createInstance();
    }
}
