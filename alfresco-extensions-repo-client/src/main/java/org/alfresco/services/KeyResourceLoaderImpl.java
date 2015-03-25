/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.alfresco.encryption.KeyResourceLoader;

/**
 * Load SSL keys from the ElasticSearch/plugin classpath
 * 
 * @author sglover
 *
 */
public class KeyResourceLoaderImpl implements KeyResourceLoader
{
	public KeyResourceLoaderImpl()
	{
	}

	@Override
	public InputStream getKeyStore(String location)
			throws FileNotFoundException
	{
		InputStream in = null;

		if(location.startsWith("classpath:"))
		{
			// on the classpath
			String classpath = location.substring("classpath:".length());
			in = getClass().getClassLoader().getResourceAsStream(classpath);
		}
		else
		{
			// absolute path
			File file = new File(location);
			if(file.exists())
			{
				in = new BufferedInputStream(new FileInputStream(file));
			}
			else
			{
				throw new IllegalArgumentException("Unable to find file " + location);
			}
		}

		return in;
	}

	@Override
	public Properties loadKeyMetaData(String location) throws IOException
	{
		Properties properties = new Properties();

		if(location.startsWith("classpath:"))
		{
			// on the classpath
			String classpath = location.substring("classpath:".length());
			InputStream in = getClass().getClassLoader().getResourceAsStream(classpath);
			properties.load(in);
		}
		else
		{
			// absolute path
			File file = new File(location);
			if(file.exists())
			{
				InputStream in = new BufferedInputStream(new FileInputStream(file));
				properties.load(in);
			}
			else
			{
				throw new IllegalArgumentException("Unable to find file " + location);
			}
		}

		return properties;
	}
}