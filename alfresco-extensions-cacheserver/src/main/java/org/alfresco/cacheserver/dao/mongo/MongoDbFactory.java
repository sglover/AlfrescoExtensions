/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dao.mongo;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
public class MongoDbFactory
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(MongoDbFactory.class);

    private boolean enabled = true;
    private String mongoURI;
    private String dbName;
    private Mongo mongo;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setMongoURI(String mongoURI)
    {
        this.mongoURI = mongoURI;
    }

    public void setDbName(String dbName)
    {
        this.dbName = dbName;
    }

    public void setMongo(Mongo mongo)
    {
        this.mongo = mongo;
    }

    public DB createInstance() throws Exception
    {
        DB db = null;
//        try
//        {
            if(enabled)
            {
                if(mongo != null)
                {
                    if(dbName != null)
                    {
                        db = mongo.getDB(dbName);
                    }
                    else
                    {
                        db = mongo.getDB(UUID.randomUUID().toString());
                    }
                }
                else
                {
                    if(mongoURI == null || dbName == null)
                    {
                        throw new RuntimeException("Must provide mongoURI and dbName or a mongo object");
                    }
                    MongoClientURI uri = new MongoClientURI(mongoURI);
                    MongoClient mongoClient = new MongoClient(uri);
                    db = mongoClient.getDB(dbName);
                }

                if (db == null)
                {
                    throw new InstantiationException("Could not instantiate a Mongo DB instance");
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug("Instatiated DB object for dbName '" + db.getName() + "'");
                }
                CommandResult stats = db.getStats();
                if (logger.isTraceEnabled())
                {
                    stats = db.getStats();
                    for (String key : stats.keySet())
                    {
                        logger.trace("\t" + key + " = " + stats.get(key).toString());
                    }
                }
            }
//        }
//        catch(MongoException.Network e)
//        {
//            throw new MongoDbUnavailableException("Mongo network exception, database down?", e);
//        }
//        catch(UnknownHostException e)
//        {
//            throw new MongoDbUnavailableException("Mongo host not found", e);
//        }
        return db;
    }
    
}
