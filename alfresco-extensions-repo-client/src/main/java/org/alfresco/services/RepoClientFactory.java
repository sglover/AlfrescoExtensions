/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.httpclient.HttpClientFactory.SecureCommsType;

/**
 * 
 * @author sglover
 *
 */
public class RepoClientFactory
{
    private String alfrescoHost = "localhost";
    private String baseUrl = "/alfresco";
    private int alfrescoPort = 8080;
    private int alfrescoPortSSL = 8443;
    private int maxTotalConnections = 10;
    private int maxHostConnections = 10;
    private int socketTimeout = 60000;

    private SecureCommsType commsType;
    private String sslKeyStoreType = "JCEKS";
    private String sslKeyStoreProvider = null;
    private String sslKeyStoreLocation = "classpath:ssl.repo.client.keystore";
    private String sslKeyStorePasswordFileLocation = "classpath:ssl-keystore-passwords.properties";
    private String sslTrustStoreType = "JCEKS";
    private String sslTrustStoreProvider = null;
    private String sslTrustStorePasswordFileLocation = "classpath:ssl-truststore-passwords.properties";
    private String sslTrustStoreLocation = "classpath:ssl.repo.client.truststore";

//    public void close()
//    {
//    	if(repoClient != null)
//    	{
//    		repoClient.close();
//    	}
//    }

    public RepoClientFactory setAlfrescoHost(String alfrescoHost)
	{
		this.alfrescoHost = alfrescoHost;
		return this;
	}

	public RepoClientFactory setCommsType(SecureCommsType commsType)
	{
		this.commsType = commsType;
		return this;
	}

	public RepoClientFactory setCommsType(String commsType)
	{
		this.commsType = SecureCommsType.valueOf(commsType);
		return this;
	}


	public RepoClientFactory setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
		return this;
	}


	public RepoClientFactory setAlfrescoPort(int alfrescoPort)
	{
		this.alfrescoPort = alfrescoPort;
		return this;
	}


	public RepoClientFactory setAlfrescoPortSSL(int alfrescoPortSSL)
	{
		this.alfrescoPortSSL = alfrescoPortSSL;
		return this;
	}


	public RepoClientFactory setMaxTotalConnections(int maxTotalConnections)
	{
		this.maxTotalConnections = maxTotalConnections;
		return this;
	}


	public RepoClientFactory setMaxHostConnections(int maxHostConnections)
	{
		this.maxHostConnections = maxHostConnections;
		return this;
	}


	public RepoClientFactory setSocketTimeout(int socketTimeout)
	{
		this.socketTimeout = socketTimeout;
		return this;
	}


	public RepoClientFactory setSslKeyStoreType(String sslKeyStoreType)
	{
		this.sslKeyStoreType = sslKeyStoreType;
		return this;
	}


	public RepoClientFactory setSslKeyStoreProvider(String sslKeyStoreProvider)
	{
		this.sslKeyStoreProvider = sslKeyStoreProvider;
		return this;
	}


	public RepoClientFactory setSslKeyStoreLocation(String sslKeyStoreLocation)
	{
		this.sslKeyStoreLocation = sslKeyStoreLocation;
		return this;
	}


	public RepoClientFactory setSslKeyStorePasswordFileLocation(
	        String sslKeyStorePasswordFileLocation)
	{
		this.sslKeyStorePasswordFileLocation = sslKeyStorePasswordFileLocation;
		return this;
	}


	public RepoClientFactory setSslTrustStoreType(String sslTrustStoreType)
	{
		this.sslTrustStoreType = sslTrustStoreType;
		return this;
	}


	public RepoClientFactory setSslTrustStoreProvider(String sslTrustStoreProvider)
	{
		this.sslTrustStoreProvider = sslTrustStoreProvider;
		return this;
	}


	public RepoClientFactory setSslTrustStorePasswordFileLocation(
	        String sslTrustStorePasswordFileLocation)
	{
		this.sslTrustStorePasswordFileLocation = sslTrustStorePasswordFileLocation;
		return this;
	}


	public RepoClientFactory setSslTrustStoreLocation(String sslTrustStoreLocation)
	{
		this.sslTrustStoreLocation = sslTrustStoreLocation;
		return this;
	}


	public AlfrescoHttpClient get()
    {
        // TODO i18n
        KeyStoreParameters keyStoreParameters = new KeyStoreParameters("SSL Key Store", sslKeyStoreType,
        		sslKeyStoreProvider, sslKeyStorePasswordFileLocation, sslKeyStoreLocation);
        KeyStoreParameters trustStoreParameters = new KeyStoreParameters("SSL Trust Store",
        		sslTrustStoreType, sslTrustStoreProvider, sslTrustStorePasswordFileLocation, sslTrustStoreLocation);
        SSLEncryptionParameters sslEncryptionParameters = new SSLEncryptionParameters(keyStoreParameters,
        		trustStoreParameters);

        KeyResourceLoaderImpl keyResourceLoader = new KeyResourceLoaderImpl();

        HttpClientFactory httpClientFactory = new HttpClientFactory(commsType,
                sslEncryptionParameters, keyResourceLoader, null, null, alfrescoHost,
                alfrescoPort, alfrescoPortSSL, maxTotalConnections, maxHostConnections, socketTimeout);

        // TODO need to make port configurable depending on secure comms, or just make redirects
        // work
        AlfrescoHttpClient repoClient = httpClientFactory.getRepoClient(alfrescoHost, alfrescoPortSSL);
        repoClient.setBaseUrl(baseUrl);
        return repoClient;
    }
}
