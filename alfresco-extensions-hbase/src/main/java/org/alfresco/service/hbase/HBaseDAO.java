/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.hbase;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 
 * @author sglover
 *
 */
public class HBaseDAO
{
	private Connection connection;
	private Admin admin;

	public void init() throws IOException
	{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.master", "localhost:60010");

		this.connection = ConnectionFactory.createConnection(conf);
		this.admin = connection.getAdmin();

		if(!tableExists("events"))
		{
			createTable("events");
			addColumnFamily("events");
			enableTable("events");
		}
	}

	public void shutdown() throws IOException
	{
		connection.close();
	}

	private boolean tableExists(String tableName) throws IOException
	{
		return admin.tableExists(TableName.valueOf(tableName));
	}

	private HTableDescriptor createTable(String tableName) throws IOException
	{
		HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
		tableDescriptor.addFamily(new HColumnDescriptor("personal"));
		tableDescriptor.addFamily(new HColumnDescriptor("contactinfo"));
		tableDescriptor.addFamily(new HColumnDescriptor("creditcard"));
		admin.createTable(tableDescriptor);
		return tableDescriptor;
	}

	private void enableTable(String tableName) throws IOException
	{
		admin.enableTable(TableName.valueOf(tableName));
	}

	private void addColumnFamily(String name) throws IOException
	{
		HTableDescriptor tableDescriptor = admin.getTableDescriptor(TableName.valueOf(name));
		tableDescriptor.addFamily(new HColumnDescriptor(name));
	}
	
	private Table getTable(String name) throws IOException
	{
		Table table = connection.getTable(TableName.valueOf(name));
		return table;
	}

	public interface WithTable
	{
		void with(Table table) throws IOException;
	}

	private void withTable(String name, WithTable with) throws IOException
	{
		Table table = getTable(name);
		try
		{
			if(table == null)
			{
				throw new RuntimeException();
			}
			with.with(table);
		}
		finally
		{
			if(table != null)
			{
				table.close();
			}
		}
	}

	public void add(final String txnId, final String nodeId, final long eventTimestamp, final String path) throws IOException
	{
		WithTable with = new WithTable()
		{
			public void with(Table table) throws IOException
            {
				Put put = new Put(Bytes.toBytes(nodeId));
				put.add(Bytes.toBytes("events"), Bytes.toBytes("txnId"), Bytes.toBytes(txnId));
				put.add(Bytes.toBytes("events"), Bytes.toBytes("path"), Bytes.toBytes(path));
				put.add(Bytes.toBytes("events"), Bytes.toBytes("eventTimestamp"), Bytes.toBytes(eventTimestamp));

				table.put(put);
            }
		};
		withTable("events", with);
	}

	public List<String> getEvents(String nodeId, long beforeEventTimestamp, String pathPrefix) throws IOException
	{
		final List<String> ret = new LinkedList<String>();

		WithTable with = new WithTable()
		{
			public void with(Table table) throws IOException
            {
				Scan scan = new Scan();
				ResultScanner scanner = table.getScanner(scan);
				try
				{
					for (Result result : scanner)
					{
						StringBuilder sb = new StringBuilder();
						sb.append(", ");
						sb.append("");

						{
							byte[] b = result.getValue(Bytes.toBytes("events"), Bytes.toBytes("txnId"));
							String txnId = Bytes.toString(b);
							sb.append(txnId);
							sb.append(",");
						}

						{
							byte[] b = result.getValue(Bytes.toBytes("events"), Bytes.toBytes("path"));
							String path = Bytes.toString(b);
							sb.append(path);
							sb.append(",");
						}

						{
							byte[] b = result.getValue(Bytes.toBytes("events"), Bytes.toBytes("eventTimestamp"));
							long eventTimestamp = Bytes.toLong(b);
							sb.append(eventTimestamp);
						}
						

						ret.add(sb.toString());
					}
				}
				finally
				{
					if(scanner != null)
					{
						scanner.close();
					}
				}
            }
		};
		withTable("events", with);

		return ret;
	}
}
