/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author sglover
 *
 */
public class Settings
{
	private Map<String, String> stringSettings = new HashMap<>();
	private Map<String, Integer> intSettings = new HashMap<>();
	private Map<String, Long> longSettings = new HashMap<>();

	public Settings()
	{
		stringSettings.put("alfrescoHost", "localhost");
		intSettings.put("alfrescoPort", 8080);
		intSettings.put("alfrescoSSLPort", 8080);
		intSettings.put("socketTimeout", 60000);
		intSettings.put("maxHostConnections", 10);
		intSettings.put("maxTotalConnections", 10);
		stringSettings.put("repo.sslKeyStoreLocation", "classpath:ssl.repo.client.keystore");
		stringSettings.put("repo.ssltrustStoreLocation", "classpath:ssl.repo.client.truststore");
		stringSettings.put("repo.sslKeyStoreType", "JCEKS");
		stringSettings.put("repo.sslTrustStoreType", "JCEKS");
		stringSettings.put("repo.sslKeyStoreProvider", null);
		stringSettings.put("repo.sslTrustStoreProvider", null);
		stringSettings.put("repo.sslKeyStorePasswordFileLocation", "classpath:ssl-keystore-passwords.properties");
		stringSettings.put("repo.sslTrustStorePasswordFileLocation", "classpath:ssl-truststore-passwords.properties");
	}

	public String getAsString(String name, String defaultValue)
	{
		String value = stringSettings.get(name);
		if(value == null)
		{
			value = defaultValue;
		}
		return value;
	}

	public Integer getAsInt(String name, Integer defaultValue)
	{
		Integer value = intSettings.get(name);
		if(value == null)
		{
			value = defaultValue;
		}
		return value;
	}

	public long getAsLong(String name, long defaultValue)
	{
		Long value = longSettings.get(name);
		if(value == null)
		{
			value = defaultValue;
		}
		return value;
	}
}
