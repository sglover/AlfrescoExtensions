/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * 
 * @author sglover
 *
 */
public class MongoFilesImpl implements Files
{
	protected GridFS myFS;

	public MongoFilesImpl(DB db)
	{
		this.myFS = (db != null ? new GridFS(db) : null);
	}

	@Override
    public DBObject createFile(long nodeId, long nodeVersion, String propertyName, byte[] bytes)
    {
        GridFSInputFile f = myFS.createFile(bytes);
        f.put("nodeId", nodeId);
        f.put("nodeVersion", nodeVersion);
        f.put("propertyQName", propertyName);
        f.save();
        DBObject dbObject = BasicDBObjectBuilder
                .start("type", "serialized")
                .add("id", f.getId())
                .get();
	    return dbObject;
    }
}
