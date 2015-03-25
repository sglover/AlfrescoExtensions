/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.providers;

import org.alfresco.services.AlfrescoApi;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Provider;
import org.elasticsearch.common.settings.Settings;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoProvider implements Provider<AlfrescoApi>
{
	private Settings settings;

	private AlfrescoApi alfrescoApi;

	@Inject public AlfrescoProvider(Settings settings)
	{
		this.settings = settings;
		buildAlfrescoApi();
	}

	private void buildAlfrescoApi()
	{
		String alfrescoHost = settings.get("alfrescoHost", "localhost");
		int alfrescoPort = settings.getAsInt("alfrescoPort", 8080);
		int alfrescoSSLPort = settings.getAsInt("alfrescoSSLPort", 8080);
		int socketTimeout = settings.getAsInt("socketTimeout", 60000);
		int maxHostConnections = settings.getAsInt("maxHostConnections", 10);
		int maxTotalConnections = settings.getAsInt("maxTotalConnections", 10);
		String sslKeyStoreLocation = settings.get("repo.sslKeyStoreLocation", "classpath:ssl.repo.client.keystore");
		String sslTrustStoreLocation = settings.get("repo.ssltrustStoreLocation", "classpath:ssl.repo.client.truststore");
		String sslKeyStoreType = settings.get("repo.sslKeyStoreType", "JCEKS");
		String sslTrustStoreType = settings.get("repo.sslTrustStoreType", "JCEKS");
		String sslKeyStoreProvider = settings.get("repo.sslKeyStoreProvider", null);
		String sslTrustStoreProvider = settings.get("repo.sslTrustStoreProvider", null);
		String sslKeyStorePasswordFileLocation = settings.get("repo.sslKeyStorePasswordFileLocation", "classpath:ssl-keystore-passwords.properties");
		String sslTrustStorePasswordFileLocation = settings.get("repo.sslTrustStorePasswordFileLocation", "classpath:ssl-truststore-passwords.properties");

		this.alfrescoApi = AlfrescoApi
			.build()
			.setAlfrescoHost(alfrescoHost)
			.setAlfrescoPort(alfrescoPort)
			.setSocketTimeout(socketTimeout)
			.setAlfrescoSSLPort(alfrescoSSLPort)
			.setMaxHostConnections(maxHostConnections)
			.setMaxTotalConnections(maxTotalConnections)
			.setSslKeyStoreLocation(sslKeyStoreLocation)
			.setSslTrustStoreLocation(sslTrustStoreLocation)
			.setSslKeyStoreType(sslKeyStoreType)
			.setSslTrustStoreType(sslTrustStoreType)
			.setSslKeyStoreProvider(sslKeyStoreProvider)
			.setSslTrustStoreProvider(sslTrustStoreProvider)
			.setSslKeyStorePasswordFileLocation(sslKeyStorePasswordFileLocation)
			.setSslTrustStorePasswordFileLocation(sslTrustStorePasswordFileLocation);
	}

	@Override
    public AlfrescoApi get()
    {
	    return alfrescoApi;
    }

}
