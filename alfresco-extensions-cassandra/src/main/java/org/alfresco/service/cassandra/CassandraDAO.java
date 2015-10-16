/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.cassandra;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select.Where;

/**
 * 
 * @author sglover
 *
 */
public class CassandraDAO
{
	private Cluster cluster;
	private Session session ;

	private boolean dropKeyspaceOnInit = false;

	public void setDropKeyspaceOnInit(boolean dropKeyspaceOnInit)
	{
		this.dropKeyspaceOnInit = dropKeyspaceOnInit;
	}

	public void init()
	{
		// Connect to the cluster and keyspace "alfresco"
		this.cluster = Cluster
				.builder()
				.addContactPoint("127.0.0.1")
				.withRetryPolicy(DefaultRetryPolicy.INSTANCE)
				.withLoadBalancingPolicy(
                         new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
				.build();

		this.session = cluster.connect();

		KeyspaceMetadata keySpace = cluster.getMetadata().getKeyspace("alfresco");
		if(dropKeyspaceOnInit && keySpace != null)
		{
			session.execute("DROP KEYSPACE alfresco;");
			keySpace = null;
		}

		if(keySpace == null)
		{
			session.execute("CREATE KEYSPACE alfresco WITH replication "
		            + "= {'class':'SimpleStrategy', 'replication_factor':3};");

		    session.execute("CREATE TABLE alfresco.events (" + "txnid text, "
		            + "path0 text," + " path1 text," + " path2 text, "
		            + "path3 text," + " path4 text," + " path5 text, "
		    		+ "eventTimestamp bigint, "
		            + "PRIMARY KEY(txnId, path0, path1, path2, path3, path4, path5) "
		            + ");");

//		    session.execute("CREATE TABLE alfresco.events (txnId text, "
//		    		+ "PRIMARY KEY(txnId)) ");
//		    		+ "with comparator=text "
//		    		+ "and key_validation_class=text ");
//		    		+ "and default_validation_class=text");

		}
	}

	public void shutdown()
	{
		cluster.close();
	}

	public void add(String txnId, long eventTimestamp, String path)
	{
		StringTokenizer st = new StringTokenizer(path, "/");

		StringBuilder sb = new StringBuilder("INSERT INTO alfresco.events (txnId, path0, path1, path2, path3, path4, path5, eventTimestamp) ");
		StringBuilder values = new StringBuilder(" VALUES('");
		values.append(txnId);
		values.append("', ");

		int remainder = 6 - st.countTokens();
		while(st.hasMoreTokens())
		{
			String pathElement = st.nextToken();

			values.append("'");
			values.append(pathElement);
			values.append("'");
			if(st.hasMoreTokens())
			{
				values.append(", ");
			}
		}

		for(int i = 1; i <= remainder; i++)
		{
			values.append(", ''");
//			if(i < remainder)
//			{
//				values.append(", ");
//			}
		}

		values.append(", ");
		values.append(eventTimestamp);

		values.append(")");

		session.execute(sb.toString() + values.toString());
	}

	private void buildPathQuery(Where where, String path)
	{
		StringTokenizer st = new StringTokenizer(path, "/");
		int i = 0;
		while(st.hasMoreTokens())
		{
			String pathElement = st.nextToken();
			where.and(QueryBuilder.eq("path" + i, pathElement));
			i++;
		}
		for(int j = i; j < 6; j++)
		{
			where.and(QueryBuilder.gte("path" + j, ""));
		}
	}

	public List<String> getEvents(String txnId, long beforeEventTimestamp, String pathPrefix)
	{
		List<String> ret = new LinkedList<>();

		Where query = QueryBuilder
			.select()
//			.all()
			.from("alfresco", "events")
			.where(QueryBuilder.eq("txnId", txnId));
		buildPathQuery(query, pathPrefix);
		query.and(QueryBuilder.lt("eventTimestamp", beforeEventTimestamp));

		System.out.println("query = " + query.toString());

		ResultSet results = session.execute(query);
		for(Row event : results.all())
		{
			String eventTxnId = event.getString("txnId");

		    StringBuilder eventPath = new StringBuilder("");
		    for(int idx = 0; idx < 6; idx++)
		    {
		    	String key = "path" + idx;
		    	if(event.getColumnDefinitions().contains(key))
		    	{
				    String pathElement = event.getString(key);
				    if(pathElement != null && !pathElement.equals(""))
				    {
				    	eventPath.append("/");
				    	eventPath.append(pathElement);
				    }
		    	}
		    }

			StringBuilder sb = new StringBuilder(eventTxnId);
			sb.append(", ");
			sb.append(eventPath.toString());

			ret.add(sb.toString());
		}

		return ret;
	}
}
