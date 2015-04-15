/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.providers;

import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.HttpClientFactory.SecureCommsType;
import org.alfresco.services.RepoClientFactory;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Provider;
import org.elasticsearch.common.settings.Settings;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoHttpClientProvider implements Provider<AlfrescoHttpClient>
{
	private Settings settings;

	private AlfrescoHttpClient repoClient;

	@Inject public AlfrescoHttpClientProvider(Settings settings)
	{
		this.settings = settings;
		buildAlfrescoHttpClient();
	}

	private void buildAlfrescoHttpClient()
	{
		String repoHost = settings.get("alfrescoHost", "localhost");
		int repoPort = settings.getAsInt("alfrescoPort", 8080);
		int repoSSLPort = settings.getAsInt("alfrescoSSLPort", 8080);
		int socketTimeout = settings.getAsInt("socketTimeout", 60000);
		int maxHostConnections = settings.getAsInt("maxHostConnections", 10);
		int maxTotalConnections = settings.getAsInt("maxTotalConnections", 10);
		String commsType = settings.get("secureCommsType", SecureCommsType.HTTPS.toString());
		String sslKeyStoreLocation = settings.get("repo.sslKeyStoreLocation", "classpath:ssl.repo.client.keystore");
		String sslTrustStoreLocation = settings.get("repo.ssltrustStoreLocation", "classpath:ssl.repo.client.truststore");
		String sslKeyStoreType = settings.get("repo.sslKeyStoreType", "JCEKS");
		String sslTrustStoreType = settings.get("repo.sslTrustStoreType", "JCEKS");
		String sslKeyStoreProvider = settings.get("repo.sslKeyStoreProvider", null);
		String sslTrustStoreProvider = settings.get("repo.sslTrustStoreProvider", null);
		String sslKeyStorePasswordFileLocation = settings.get("repo.sslKeyStorePasswordFileLocation", "classpath:ssl-keystore-passwords.properties");
		String sslTrustStorePasswordFileLocation = settings.get("repo.sslTrustStorePasswordFileLocation", "classpath:ssl-truststore-passwords.properties");

		RepoClientFactory repoClientFactory = new RepoClientFactory();
		repoClientFactory
			.setAlfrescoHost(repoHost)
			.setAlfrescoPort(repoPort)
			.setAlfrescoPortSSL(repoSSLPort)
			.setCommsType(commsType)
			.setMaxHostConnections(maxHostConnections)
						.setMaxTotalConnections(maxTotalConnections)
			.setSslKeyStoreLocation(sslKeyStoreLocation)
			.setSslTrustStoreLocation(sslTrustStoreLocation)
			.setSslKeyStoreType(sslKeyStoreType)
			.setSslTrustStoreType(sslTrustStoreType)
			.setSslKeyStoreProvider(sslKeyStoreProvider)
			.setSslTrustStoreProvider(sslTrustStoreProvider)
			.setSslKeyStorePasswordFileLocation(sslKeyStorePasswordFileLocation)
			.setSslTrustStorePasswordFileLocation(sslTrustStorePasswordFileLocation)
			.setSocketTimeout(socketTimeout);
		this.repoClient = repoClientFactory.get();
	}

	@Override
    public AlfrescoHttpClient get()
    {
	    return repoClient;
    }

}
