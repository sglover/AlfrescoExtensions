/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.dao.orient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.alfresco.cacheserver.dao.ContentDAO;
import org.alfresco.cacheserver.entity.NodeInfo;
import org.alfresco.cacheserver.entity.NodeUsage;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * 
 * @author sglover
 *
 */
public class OrientContentDAO implements ContentDAO
{
	private String hostname;
	private String dbName;
	private String username;
	private String password;
	private boolean recreate;

	private OServerAdmin serverAdmin;
	private OServerAdmin dbServerAdmin;
	private OPartitionedDatabasePool pool;

	public OrientContentDAO(String hostname, String dbName, String username, String password, boolean recreate)
    {
	    super();
	    this.hostname = hostname;
	    this.dbName = dbName;
	    this.username = username;
	    this.password = password;
	    this.recreate = recreate;
    }

	public void init() throws Exception
	{
		this.serverAdmin = new OServerAdmin("remote:" + hostname);
		serverAdmin.connect("root", "admin");

		OPartitionedDatabasePoolFactory poolFactory = new OPartitionedDatabasePoolFactory(10);
		this.pool = poolFactory.get("remote:" + hostname + "/" + dbName, username, password);
//		this.pool = new OPartitionedDatabasePool();
//		pool.setup(1,10);

		Map<String, String> databases = serverAdmin.listDatabases();
		if(databases.get(dbName) == null)
		{
			serverAdmin.createDatabase(dbName, "document", "plocal");
			dbServerAdmin = new OServerAdmin("remote:" + hostname + "/" + dbName);
			dbServerAdmin.connect("root", "admin");
		}
		else
		{
			dbServerAdmin = new OServerAdmin("remote:" + hostname + "/" + dbName);
			dbServerAdmin.connect("root", "admin");

			if(recreate)
			{
				dropDatabase();
				createDatabase();
			}
		}
	}

	private void createDatabase() throws IOException
	{
		serverAdmin.createDatabase(dbName, "document", "plocal");
	}

	private void dropDatabase() throws IOException
	{
		dbServerAdmin.dropDatabase("plocal");
	}

	private ODatabaseDocumentTx getDB()
	{
		ODatabaseDocumentTx db = pool.acquire();
		return db;
	}

	private void updateFromNodeInfo(NodeInfo nodeInfo, ODocument doc)
	{
		doc.field("p", nodeInfo.getNodePath());
		doc.field("c", nodeInfo.getContentPath());
		doc.field("n", nodeInfo.getNodeId());
		doc.field("v", nodeInfo.getNodeVersion());
		doc.field("m", nodeInfo.getMimeType());
		doc.field("s", nodeInfo.getSize());
	}

	private ODocument fromNodeInfo(NodeInfo nodeInfo)
	{
		ODocument doc = new ODocument("Content");
		doc.field("p", nodeInfo.getNodePath());
		doc.field("c", nodeInfo.getContentPath());
		doc.field("n", nodeInfo.getNodeId());
		doc.field("v", nodeInfo.getNodeVersion());
		doc.field("m", nodeInfo.getMimeType());
		doc.field("s", nodeInfo.getSize());
		return doc;
	}

	private NodeInfo toNodeInfo(ODocument doc)
	{
		String nodePath = doc.field("p");
		String contentPath = doc.field("c");
		String nodeId = doc.field("n");
		long nodeInternalId = doc.field("ni");
		String nodeVersion = doc.field("v");
		String mimeType = doc.field("m");
		Long size = doc.field("s");

		NodeInfo nodeInfo = new NodeInfo(nodeId, nodeInternalId, nodeVersion, nodePath, contentPath, mimeType, size);
		return nodeInfo;
	}

	public void shutdown()
	{
		serverAdmin.close();
		pool.close();
	}

	public interface Callback<T>
	{
		T execute(ODatabaseDocumentTx db);
	}

	private <T> T withDBTX(Callback<T> callback)
	{
		ODatabaseDocumentTx db = getDB();
		db.begin();
		try
		{
			return callback.execute(db);
		}
		finally
		{
			db.commit();
			db.close();
		}
	}

//	@Override
//	public void addNode(final NodeInfo nodeInfo)
//	{
//		withDBTX(new Callback<Void>()
//		{
//			@Override
//            public Void execute(ODatabaseDocumentTx db)
//            {
//				ODocument doc = fromNodeInfo(nodeInfo);
//				doc.save();
//
//	            return null;
//            }
//		});
//	}

	@Override
    public NodeInfo getByNodePath(final String contentURL)
    {
		NodeInfo nodeInfo = withDBTX(new Callback<NodeInfo>()
		{
			@Override
            public NodeInfo execute(ODatabaseDocumentTx db)
            {
				OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Content where c = " + contentURL);
				List<ODocument> result = db.query(query);
				if(result.size() != 1)
				{
					throw new RuntimeException();
				}
				ODocument doc = result.get(0);
				NodeInfo nodeInfo = toNodeInfo(doc);
	            return nodeInfo;
            }
		});

	    return nodeInfo;
    }
	
    private ODocument getDocByNodeId(final String nodeId, final String nodeVersion)
    {
    	ODocument doc = withDBTX(new Callback<ODocument>()
		{
			@Override
            public ODocument execute(ODatabaseDocumentTx db)
            {
				OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Content where n = '" + nodeId
						+ "' and v = " + nodeVersion);
				List<ODocument> result = db.query(query);
				if(result.size() != 1)
				{
					throw new RuntimeException("Got " + result.size() + " results, expected 1");
				}
				ODocument doc = result.get(0);
	            return doc;
            }
		});

	    return doc;
    }


	@Override
    public NodeInfo getByNodeId(final String nodeId, final String nodeVersion, final boolean isPrimary)
    {
		NodeInfo nodeInfo = withDBTX(new Callback<NodeInfo>()
		{
			@Override
            public NodeInfo execute(ODatabaseDocumentTx db)
            {
				OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Content where n = '" + nodeId
						+ "' and v = " + nodeVersion + " and ip = " + isPrimary);
				List<ODocument> result = db.query(query);
				if(result.size() != 1)
				{
					throw new RuntimeException("Got " + result.size() + " results, expected 1");
				}
				ODocument doc = result.get(0);
				NodeInfo nodeInfo = toNodeInfo(doc);
	            return nodeInfo;
            }
		});

		return nodeInfo;
	}

	@Override
    public void updateNode(final NodeInfo nodeInfo)
    {
		withDBTX(new Callback<Void>()
		{
			@Override
            public Void execute(ODatabaseDocumentTx db)
            {
				String nodeId = nodeInfo.getNodeId();
				String nodeVersion = nodeInfo.getNodeVersion();
				ODocument doc = getDocByNodeId(nodeId, nodeVersion);
				updateFromNodeInfo(nodeInfo, doc);

	            return null;
            }
		});
    }

	@Override
    public void addUsage(NodeUsage nodeUsage)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public NodeInfo getByNodeId(long nodeInternalId, String mimeType)
    {
	    // TODO Auto-generated method stub
	    return null;
    }
}
