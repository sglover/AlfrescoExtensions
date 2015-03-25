/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.alfresco.encryption.KeyResourceLoader;

public class KeyResourceLoaderImpl implements KeyResourceLoader
{
	public KeyResourceLoaderImpl()
	{
	}

	@Override
	public InputStream getKeyStore(String location)
			throws FileNotFoundException
	{
		return null;
	}

	@Override
	public Properties loadKeyMetaData(String location) throws IOException
	{
		return null;
	}
}