/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;

public class MongoDbFactory
{
    /**
     * Logger for this class
     */
    private static final Log logger = LogFactory.getLog(MongoDbFactory.class.getName()); 

//    private MongodForTestsFactory mongoFactory;

    private boolean enabled = true;
    private String mongoURI;
    private String dbName;
    private boolean embedded;

    private MongodExecutable mongodExecutable;
    private MongodProcess mongodProcess;

    public MongoDbFactory(boolean enabled, String mongoURI, String dbName, boolean embedded) throws IOException
    {
        this.enabled = enabled;
        this.mongoURI = mongoURI;
        this.dbName = dbName;
        this.embedded = embedded;

        if(embedded)
        {
            final Logger mongoLogger = Logger.getLogger(MongoDbFactory.class.getName());
            mongoLogger.setLevel(Level.WARNING); 
            final MongodStarter runtime = MongodStarter.getInstance(new RuntimeConfigBuilder()
                    .defaultsWithLogger(Command.MongoD, mongoLogger)
                    .build());
            mongodExecutable = runtime.prepare(newMongodConfig(Version.Main.PRODUCTION));
            mongodProcess = mongodExecutable.start();

//            mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
        }
    }

    protected IMongodConfig newMongodConfig(final IFeatureAwareVersion version) throws UnknownHostException, IOException {
        return new MongodConfigBuilder().version(version).build();
    }

    public void shutdown()
    {
        mongodProcess.stop();
        mongodExecutable.stop();
//        if(mongoFactory != null)
//        {
//            mongoFactory.shutdown();
//        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    private MongoClient newMongo() throws UnknownHostException, MongoException {
        return new MongoClient(new ServerAddress(mongodProcess.getConfig().net().getServerAddress(),
                mongodProcess.getConfig().net().getPort()));
    }

    public DB createInstance() throws Exception
    {
        DB db = null;

        try
        {
            if(enabled)
            {
                if(embedded)
                {
                    Mongo mongo = newMongo();
//                    Mongo mongo = mongoFactory.newMongo();
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
        }
        catch(MongoException.Network e)
        {
            throw new MongoDbUnavailableException("Mongo network exception, database down?", e);
        }
        catch(UnknownHostException e)
        {
            throw new MongoDbUnavailableException("Mongo host not found", e);
        }
        return db;
    }
}
