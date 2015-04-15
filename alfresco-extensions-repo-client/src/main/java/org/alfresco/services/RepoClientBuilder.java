/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.httpclient.HttpClientFactory.SecureCommsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class RepoClientBuilder
{
    private static final Log logger = LogFactory.getLog(RepoClientBuilder.class);

//    private String alfrescoHost = "localhost";
//    private int alfrescoPort = 8080;
//    private int alfrescoPortSSL = 8443;
//    private int maxTotalConnections = 10;
//    private int maxHostConnections = 10;
//    private int socketTimeout = 60000;
//
//    private SecureCommsType commsType;
//    private String sslKeyStoreType = "JCEKS";
//    private String sslKeyStoreProvider = null;
//    private String sslKeyStoreLocation = "classpath:ssl.repo.client.keystore";
//    private String sslKeyStorePasswordFileLocation = "classpath:ssl-keystore-passwords.properties";
//    private String sslTrustStoreType = "JCEKS";
//    private String sslTrustStoreProvider = null;
//    private String sslTrustStorePasswordFileLocation = "classpath:ssl-truststore-passwords.properties";
//    private String sslTrustStoreLocation = "classpath:ssl.repo.client.truststore";

    private String host;
    private int port;
    private HttpClientFactory factory;

    public RepoClientBuilder(String secureCommsType, SSLEncryptionParameters sslEncryptionParameters,
            KeyResourceLoader keyResourceLoader, String host, int port, int sslPort, int maxTotalConnections,
            int maxHostConnections, int socketTimeout)
    {
    	this.host = host;
    	this.port = port;
    	this.factory = new HttpClientFactory(SecureCommsType.getType(secureCommsType), sslEncryptionParameters,
    			keyResourceLoader, null, null, host, port, sslPort, maxTotalConnections, maxHostConnections,
    			socketTimeout);
    }

    public AlfrescoHttpClient getRepoClient()
    {
    	AlfrescoHttpClient client = factory.getRepoClient(host, port);
    	return client;
    }

//    public AlfrescoHttpClient build()
//    {
//    	KeyStoreParameters keyStoreParameters = new KeyStoreParameters("SSL Key Store", sslKeyStoreType,
//        		sslKeyStoreProvider, sslKeyStorePasswordFileLocation, sslKeyStoreLocation);
//        KeyStoreParameters trustStoreParameters = new KeyStoreParameters("SSL Trust Store",
//        		sslTrustStoreType, sslTrustStoreProvider, sslTrustStorePasswordFileLocation, sslTrustStoreLocation);
//        SSLEncryptionParameters sslEncryptionParameters = new SSLEncryptionParameters(keyStoreParameters,
//        		trustStoreParameters);
//
//        KeyResourceLoaderImpl keyResourceLoader = new KeyResourceLoaderImpl();
//
//        HttpClientFactory httpClientFactory = new HttpClientFactory(commsType,
//                sslEncryptionParameters, keyResourceLoader, null, null, alfrescoHost,
//                alfrescoPort, alfrescoPortSSL, maxTotalConnections, maxHostConnections, socketTimeout);
//
//        // TODO need to make port configurable depending on secure comms, or just make redirects
//        // work
//        AlfrescoHttpClient repoClient = httpClientFactory.getRepoClient(alfrescoHost, alfrescoPortSSL);
//        return repoClient;
//    }
//
//    public void setCommsType(SecureCommsType commsType)
//	{
//		this.commsType = commsType;
//	}
//
//	public HttpClientBuilder setAlfrescoHost(String alfrescoHost)
//    {
//    	this.alfrescoHost = alfrescoHost;
//    	return this;
//    }
//
//    public HttpClientBuilder setAlfrescoPort(int alfrescoPort)
//    {
//    	this.alfrescoPort = alfrescoPort;
//    	return this;
//    }
//
//    public HttpClientBuilder setAlfrescoSSLPort(int alfrescoPortSSL)
//    {
//    	this.alfrescoPortSSL = alfrescoPortSSL;
//    	return this;
//    }
//
//    public HttpClientBuilder setMaxTotalConnections(int maxTotalConnections)
//    {
//    	this.maxTotalConnections = maxTotalConnections;
//    	return this;
//    }
//
//    public HttpClientBuilder setMaxHostConnections(int maxHostConnections)
//    {
//    	this.maxHostConnections = maxHostConnections;
//    	return this;
//    }
//
//    public HttpClientBuilder setSocketTimeout(int socketTimeout)
//    {
//    	this.socketTimeout = socketTimeout;
//    	return this;
//    }
//
//	public HttpClientBuilder setSslKeyStoreType(String sslKeyStoreType)
//	{
//		this.sslKeyStoreType = sslKeyStoreType;
//		return this;
//	}
//
//	public HttpClientBuilder setSslKeyStoreProvider(String sslKeyStoreProvider)
//	{
//		this.sslKeyStoreProvider = sslKeyStoreProvider;
//		return this;
//	}
//
//	public HttpClientBuilder setSslKeyStoreLocation(String sslKeyStoreLocation)
//	{
//		this.sslKeyStoreLocation = sslKeyStoreLocation;
//		return this;
//	}
//
//	public HttpClientBuilder setSslKeyStorePasswordFileLocation(String sslKeyStorePasswordFileLocation)
//	{
//		this.sslKeyStorePasswordFileLocation = sslKeyStorePasswordFileLocation;
//		return this;
//	}
//
//	public HttpClientBuilder setSslTrustStoreType(String sslTrustStoreType)
//	{
//		this.sslTrustStoreType = sslTrustStoreType;
//		return this;
//	}
//
//	public HttpClientBuilder setSslTrustStoreProvider(String sslTrustStoreProvider)
//	{
//		this.sslTrustStoreProvider = sslTrustStoreProvider;
//		return this;
//	}
//
//	public HttpClientBuilder setSslTrustStorePasswordFileLocation(String sslTrustStorePasswordFileLocation)
//	{
//		this.sslTrustStorePasswordFileLocation = sslTrustStorePasswordFileLocation;
//		return this;
//	}
//
//	public HttpClientBuilder setSslTrustStoreLocation(String sslTrustStoreLocation)
//	{
//		this.sslTrustStoreLocation = sslTrustStoreLocation;
//		return this;
//	}
//
//	public HttpClientBuilder()
//    {
//    }
}
