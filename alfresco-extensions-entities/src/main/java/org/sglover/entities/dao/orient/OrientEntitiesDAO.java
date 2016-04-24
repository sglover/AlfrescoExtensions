/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao.orient;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.EntitiesDAO;
import org.sglover.entities.values.EntityCounts;
import org.sglover.nlp.Entities;
import org.sglover.nlp.Entity;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

/**
 * 
 * @author sglover
 *
 */
public class OrientEntitiesDAO //implements EntitiesDAO
{
//	private String hostname;
//	private String dbName;
//	private String username;
//	private String password;
//	private boolean recreate;
//
//	private OServerAdmin serverAdmin;
//	private OServerAdmin dbServerAdmin;
//	private OPartitionedDatabasePool pool;
//
//	public OrientEntitiesDAO(String hostname, String dbName, String username, String password, boolean recreate)
//    {
//	    super();
//	    this.hostname = hostname;
//	    this.dbName = dbName;
//	    this.username = username;
//	    this.password = password;
//	    this.recreate = recreate;
//    }
//
//	public void init() throws Exception
//	{
//		this.serverAdmin = new OServerAdmin("remote:" + hostname);
//		serverAdmin.connect("root", "tasnu573");
//
//		OPartitionedDatabasePoolFactory poolFactory = new OPartitionedDatabasePoolFactory(10);
//		this.pool = poolFactory.get("remote:" + hostname + "/" + dbName, username, password);
////		this.pool = new OPartitionedDatabasePool();
////		pool.setup(1,10);
//
//		Map<String, String> databases = serverAdmin.listDatabases();
//		if(databases.get(dbName) == null)
//		{
//			serverAdmin.createDatabase(dbName, "document", "plocal");
//			dbServerAdmin = new OServerAdmin("remote:" + hostname + "/" + dbName);
//			dbServerAdmin.connect("root", "admin");
//		}
//		else
//		{
//			dbServerAdmin = new OServerAdmin("remote:" + hostname + "/" + dbName);
//			dbServerAdmin.connect("root", "tasnu573");
//
//			if(recreate)
//			{
//				dropDatabase();
//				createDatabase();
//			}
//		}
//	}
//
//	private void createDatabase() throws IOException
//	{
//		serverAdmin.createDatabase(dbName, "document", "plocal");
//	}
//
//	private void dropDatabase() throws IOException
//	{
//		dbServerAdmin.dropDatabase("plocal");
//	}
//
//	private ODatabaseDocumentTx getDB()
//	{
//		ODatabaseDocumentTx db = pool.acquire();
//		return db;
//	}
//
//	public void shutdown()
//	{
//		serverAdmin.close();
//		pool.close();
//	}
//
//	public interface Callback<T>
//	{
//		T execute(ODatabaseDocumentTx db);
//	}
//
//	private <T> T withDBTX(Callback<T> callback)
//	{
//		ODatabaseDocumentTx db = getDB();
//		db.begin();
//		try
//		{
//			return callback.execute(db);
//		}
//		finally
//		{
//			db.commit();
//			db.close();
//		}
//	}
//
//	@Override
//    public List<Node> matchingNodes(String type, String name)
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
//	@Override
//    public Entities getEntities(Node node, Set<String> types)
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
//	@Override
//    public Collection<Entity<String>> getNames(Node node)
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
//	@Override
//    public void addEntities(String txnId, Node node, Entities entities)
//    {
//	    // TODO Auto-generated method stub
//	    
//    }
//
//	@Override
//    public EntityCounts<String> getEntityCounts(Node node)
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
//	@Override
//    public List<Entities> getEntities()
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
//	@Override
//    public List<Entities> unprocessedEntites()
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
////	@Override
////    public void saveSimilarity(Node node1, Node node2, double similarity)
////    {
////		ODocument doc = new ODocument();
////		doc.field("n1", node1.getNodeId());
////		doc.field("v1", node1.getNodeVersion());
////		doc.field("n2", node2.getNodeId());
////		doc.field("v2", node2.getNodeVersion());
////		doc.field("s", similarity);
////	    
////    }
////
////	@Override
////    public double getSimilarity(final Node node1, final Node node2)
////    {
////		Double similarity = withDBTX(new Callback<Double>()
////		{
////			@Override
////            public Double execute(ODatabaseDocumentTx db)
////            {
////				StringBuilder sb = new StringBuilder("select * from Similarity where ");
////				sb.append("(n1 = '" + node1.getNodeId() + "'");
////				sb.append(" AND v1 = '" + node1.getNodeVersion() + "')");
////				sb.append("OR (n2 = '" + node2.getNodeId() + "'");
////				sb.append(" AND v2 = '" + node2.getNodeVersion() + "')");
////				OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sb.toString());
////				List<ODocument> result = db.query(query);
////				if(result.size() != 1)
////				{
////					throw new RuntimeException("Got " + result.size() + " results, expected 1");
////				}
////				ODocument doc = result.get(0);
////	            return doc.field("s");
////            }
////		});
////	    return similarity;
////    }
//
//	@Override
//    public EntityCounts<String> getNodeMatches(Entities entities)
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
//
//	@Override
//    public List<Entities> getEntitiesForTxn(String txnId)
//    {
//	    // TODO Auto-generated method stub
//	    return null;
//    }
}
