/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.checksum.dao.mongo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.checksum.Checksum;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.checksum.dao.ChecksumDAO;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * 
 * @author sglover
 *
 */
public class MongoChecksumDAO implements ChecksumDAO
{
	private DB db;

	private DBCollection checksums;
	private String checksumsCollectionName;

	public MongoChecksumDAO(DB db, String checksumsCollectionName) throws Exception
	{
		this.db = db;
		this.checksumsCollectionName = checksumsCollectionName;
		init();
	}

	public void drop()
	{
		checksums.drop();
	}

	protected DBCollection getCollection(DB db, String collectionName, WriteConcern writeConcern)
	{
	    if(!db.collectionExists(collectionName))
	    {
	        DBObject options = new BasicDBObject();
	        db.createCollection(collectionName, options);
	    }
	    DBCollection collection = db.getCollection(collectionName);
	    collection.setWriteConcern(writeConcern);

	    return collection;
	}

    protected DBCollection getCappedCollection(DB db, String collectionName, Integer maxCollectionSize, Integer maxDocuments, WriteConcern writeConcern)
    {
        if(!db.collectionExists(collectionName))
        {
            BasicDBObjectBuilder builder = BasicDBObjectBuilder
                    .start();

            builder.add("capped", true);
            
            if(maxCollectionSize != null)
            {
                builder.add("size", maxCollectionSize);
            }

            if(maxDocuments != null)
            {
                builder.add("max", maxDocuments);
            }

            DBObject options = builder.get();
            db.createCollection(collectionName, options);
        }
        DBCollection collection = db.getCollection(collectionName);
        collection.setWriteConcern(writeConcern);

        return collection;
    }

	protected void checkResult(WriteResult result, int expectedNum)
	{
	    boolean ok = result.getLastError().ok();
	    if(!ok)
	    {
	        throw new RuntimeException("Mongo write failed");
	    }
	    if(expectedNum != result.getN())
	    {
	        throw new RuntimeException("Mongo write failed, expected " + expectedNum + " writes, got " + result.getN());
	    }
	}

	
	protected void checkResult(WriteResult result)
	{
        boolean ok = result.getLastError().ok();
        if(!ok)
        {
            throw new RuntimeException("Mongo write failed");
        }
	}

	private void init()
	{
        if (db == null)
        {
            throw new RuntimeException("Mongo DB must not be null");
        }

		this.checksums = getCollection(db, checksumsCollectionName, WriteConcern.ACKNOWLEDGED);

//		{
//	        DBObject keys = BasicDBObjectBuilder
//	        		.start("e", 1)
//	                .add("n", 1)
//	                .add("v", 1)
//	                .get();
//	        this.checksums.ensureIndex(keys, "byNodeId", false);
//		}
	}

    private Checksum toChecksum(DBObject dbObject)
	{
		int blockIndex = (Integer)dbObject.get("i");
		int hash = (Integer)dbObject.get("h");
		int adler32 = (Integer)dbObject.get("a");
		String md5 = (String)dbObject.get("m");
		Checksum checksum = new Checksum(blockIndex, hash, adler32, md5);
		return checksum;
	}

	@SuppressWarnings("unchecked")
    private NodeChecksums toChecksums(DBObject dbObject)
	{
	    NodeChecksums documentChecksums = null;
	    if(dbObject != null)
	    {
    		String nodeId = (String)dbObject.get("n");
    		Long nodeInternalId = (Long)dbObject.get("ni");
            Long nodeVersion = (Long)dbObject.get("v");
    		String versionLabel = (String)dbObject.get("l");
    		String contentUrl = (String)dbObject.get("u");
    		int blockSize = (Integer)dbObject.get("b");
    		long numBlocks = (Long)dbObject.get("nb");
    
    		documentChecksums = new NodeChecksums(nodeId, nodeInternalId, nodeVersion, versionLabel,
    				contentUrl, blockSize, numBlocks);
    
    		DBObject documentChecksumDBObject = (DBObject)dbObject.get("c");
    		for(String hashStr : documentChecksumDBObject.keySet())
    		{
    			List<DBObject> checksumDBObjects = (List<DBObject>)documentChecksumDBObject.get(hashStr);
    			for(DBObject checksumDBObject : checksumDBObjects)
    			{
    				Checksum checksum = toChecksum(checksumDBObject);
    				documentChecksums.addChecksum(checksum);
    			}
    		}
	    }

		return documentChecksums;
	}

	private DBObject toDBObject(Checksum checksum)
	{
		DBObject dbObject = BasicDBObjectBuilder
				.start("i", checksum.getBlockIndex())
				.add("h", checksum.getHash())
				.add("a", checksum.getAdler32())
				.add("m", checksum.getMd5())
				.get();
		return dbObject;
	}

	private DBObject toDBObject(NodeChecksums documentChecksums)
	{
		BasicDBObjectBuilder checksumsObjectBuilder = BasicDBObjectBuilder.start();
		for(Map.Entry<Integer, List<Checksum>> checksums : documentChecksums.getChecksums().entrySet())
		{
			List<DBObject> checksumDBObjects = new LinkedList<>();
			for(Checksum checksum : checksums.getValue())
			{
				DBObject checksumDBObject = toDBObject(checksum);
				checksumDBObjects.add(checksumDBObject);
			}
			checksumsObjectBuilder.add(String.valueOf(checksums.getKey()), checksumDBObjects);
		}
		DBObject dbObject = BasicDBObjectBuilder
				.start("n", documentChecksums.getNodeId())
				.add("ni", documentChecksums.getNodeInternalId())
                .add("v", documentChecksums.getNodeVersion())
                .add("l", documentChecksums.getVersionLabel())
				.add("u", documentChecksums.getContentUrl())
				.add("b", documentChecksums.getBlockSize())
				.add("nb", documentChecksums.getNumBlocks())
				.add("c", checksumsObjectBuilder.get())
				.get();
		return dbObject;
	}

	public void saveChecksums(NodeChecksums checksums)
	{
		DBObject dbObject = toDBObject(checksums);
		this.checksums.insert(dbObject);
	}

	@Override
	public NodeChecksums getChecksums(String nodeId, long nodeVersion)
	{
		DBObject query = QueryBuilder
				.start("n").is(nodeId)
				.and("v").is(nodeVersion)
				.get();

		DBObject dbObject = checksums.findOne(query);
		NodeChecksums checksums = toChecksums(dbObject);
		return checksums;
	}
}
