/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.common.identity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.alfresco.util.GUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author sglover
 *
 */
public class ServerIdentityImpl implements ServerIdentity
{
	private static Log logger = LogFactory.getLog(ServerIdentityImpl.class);

	private String id;
	private String hostname;
	private int port;

	private String dataFilename = "cacheserver.json";

	public ServerIdentityImpl(String hostname, int port, String dataFilename)
	{
		this.hostname = hostname;
		this.port = port;
		this.dataFilename = dataFilename;

		File dataFile = dataFile();

		try
		{
			Data data = readData();
			this.id = data.getId();
		}
		catch(IOException e)
		{
			logger.error("Unable to create data file " + dataFile, e);
			throw new RuntimeException("Unable to create data file " + dataFile, e);
		}
	}

	private File dataFile()
	{
//		File tmpDir = TempFileProvider.getTempDir();
//		File dataFile = new File(tmpDir, dataFilename);
		File dataFile = new File(dataFilename);
		return dataFile;
	}

	@SuppressWarnings("unchecked")
	private JSONObject toJSON(Data data)
	{
		String id = data.getId();
		JSONObject ret = new JSONObject();
		ret.put("cacheServerId", id);
		return ret;
	}

	private Data fromJSON(JSONObject syncDataObject)
	{
		String id = (String)syncDataObject.get("cacheServerId");
		Data ret = new Data(id);
		return ret;
	}

	private Data readData() throws IOException
	{
		Data data = null;
		File dataFile = dataFile();

		logger.debug("Reading data from file " + dataFile.getAbsolutePath());

		if(dataFile.exists())
		{
			try
			{
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(dataFile));
				try
				{
					String string = IOUtils.toString(in, "UTF-8");
			        JSONParser parser = new JSONParser();
			        JSONObject json = (JSONObject)parser.parse(string);
					data = fromJSON(json);
				}
				finally
				{
					if(in != null)
					{
						in.close();
					}
				}
			}
			catch(IOException|ParseException e)
			{
				// fall back - try to re-create re-create
				data = createData(dataFile);
			}
		}
		else
		{
			data = createData(dataFile);
		}

		logger.debug("Data " + data);

		return data;
	}

	private Data createData(File syncFile) throws IOException
	{
		String id = GUID.generate();
		Data data = new Data(id);
		JSONObject dataObject = toJSON(data);

		Writer writer = new FileWriter(syncFile);
		try
		{
			dataObject.writeJSONString(writer);
		}
		finally
		{
			if(writer != null)
			{
				writer.close();
			}
		}

		return data;
	}

	private void persistData()
	{
		File dataFile = dataFile();

		try
		{
			Data data = new Data(id);
			JSONObject syncDataObject = toJSON(data);

			logger.debug("Persisting data " + data + " to " + dataFile.getAbsolutePath());
	
			Writer writer = new FileWriter(dataFile);
			try
			{
				syncDataObject.writeJSONString(writer);
			}
			finally
			{
				if(writer != null)
				{
					writer.close();
				}
			}
		}
		catch(IOException e)
		{
			logger.warn("Unable to persist data to " + dataFile.getAbsolutePath());
		}
	}

	public void shutdown()
	{
		persistData();
	}

	@Override
    public String getId()
    {
	    return id;
    }

	private static class Data
	{
		private String id;

		public Data(String id)
        {
	        super();
	        this.id = id;
        }

		public String getId()
		{
			return id;
		}
	}

	@Override
	public String getHostname()
	{
		return hostname;
	}

	@Override
	public int getPort()
	{
		return port;
	}
}
