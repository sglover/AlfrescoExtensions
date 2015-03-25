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
import org.alfresco.serializers.PropertySerializer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoApi
{
    private static final Log logger = LogFactory.getLog(AlfrescoApi.class);

    private String alfrescoHost = "localhost";
    private String baseUrl = "/alfresco";
    private int alfrescoPort = 8080;
    private int alfrescoPortSSL = 8443;
    private int maxTotalConnections = 10;
    private int maxHostConnections = 10;
    private int socketTimeout = 60000;

    private String sslKeyStoreType = "JCEKS";
    private String sslKeyStoreProvider = null;
    private String sslKeyStoreLocation = "classpath:ssl.repo.client.keystore";
    private String sslKeyStorePasswordFileLocation = "classpath:ssl-keystore-passwords.properties";
    private String sslTrustStoreType = "JCEKS";
    private String sslTrustStoreProvider = null;
    private String sslTrustStorePasswordFileLocation = "classpath:ssl-truststore-passwords.properties";
    private String sslTrustStoreLocation = "classpath:ssl.repo.client.truststore";

    private ContentGetter contentGetter;
    private AlfrescoHttpClient repoClient;

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    private PropertySerializer propertySerializer;

    public static AlfrescoApi build()
    {
    	AlfrescoApi alfrescoApi = new AlfrescoApi();
    	return alfrescoApi;
    }

    public AlfrescoApi setAlfrescoHost(String alfrescoHost)
    {
    	this.alfrescoHost = alfrescoHost;
    	return this;
    }

    public AlfrescoApi setAlfrescoPort(int alfrescoPort)
    {
    	this.alfrescoPort = alfrescoPort;
    	return this;
    }

    public AlfrescoApi setAlfrescoSSLPort(int alfrescoPortSSL)
    {
    	this.alfrescoPortSSL = alfrescoPortSSL;
    	return this;
    }

    public AlfrescoApi setMaxTotalConnections(int maxTotalConnections)
    {
    	this.maxTotalConnections = maxTotalConnections;
    	return this;
    }

    public AlfrescoApi setMaxHostConnections(int maxHostConnections)
    {
    	this.maxHostConnections = maxHostConnections;
    	return this;
    }

    public AlfrescoApi setSocketTimeout(int socketTimeout)
    {
    	this.socketTimeout = socketTimeout;
    	return this;
    }

	public AlfrescoApi setSslKeyStoreType(String sslKeyStoreType)
	{
		this.sslKeyStoreType = sslKeyStoreType;
		return this;
	}

	public AlfrescoApi setSslKeyStoreProvider(String sslKeyStoreProvider)
	{
		this.sslKeyStoreProvider = sslKeyStoreProvider;
		return this;
	}

	public AlfrescoApi setSslKeyStoreLocation(String sslKeyStoreLocation)
	{
		this.sslKeyStoreLocation = sslKeyStoreLocation;
		return this;
	}

	public AlfrescoApi setSslKeyStorePasswordFileLocation(String sslKeyStorePasswordFileLocation)
	{
		this.sslKeyStorePasswordFileLocation = sslKeyStorePasswordFileLocation;
		return this;
	}

	public AlfrescoApi setSslTrustStoreType(String sslTrustStoreType)
	{
		this.sslTrustStoreType = sslTrustStoreType;
		return this;
	}

	public AlfrescoApi setSslTrustStoreProvider(String sslTrustStoreProvider)
	{
		this.sslTrustStoreProvider = sslTrustStoreProvider;
		return this;
	}

	public AlfrescoApi setSslTrustStorePasswordFileLocation(String sslTrustStorePasswordFileLocation)
	{
		this.sslTrustStorePasswordFileLocation = sslTrustStorePasswordFileLocation;
		return this;
	}

	public AlfrescoApi setSslTrustStoreLocation(String sslTrustStoreLocation)
	{
		this.sslTrustStoreLocation = sslTrustStoreLocation;
		return this;
	}

	public AlfrescoApi()
    {
    	logger.debug("Building AlfrescoApi");

        buildRepoClient(SecureCommsType.HTTPS);
        buildContentGetter();
    	buildDictionary();
        buildPropertySerializer();
    }

    public void buildPropertySerializer()
    {
    	this.propertySerializer = new PropertySerializer(dictionaryService, namespaceService);
    }

    public void close()
    {
    	if(repoClient != null)
    	{
    		repoClient.close();
    	}
    }

    protected void buildRepoClient(SecureCommsType commsType)
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
        this.repoClient = httpClientFactory.getRepoClient(alfrescoHost, alfrescoPortSSL);
        repoClient.setBaseUrl(baseUrl);
    }

    private void buildDictionary()
    {
        WrappingDictionaryService wrappingDictionaryService = new WrappingDictionaryService(repoClient);
        this.dictionaryService = wrappingDictionaryService;
        this.namespaceService = wrappingDictionaryService;
//    	TenantService tenantService = new SingleTServiceImpl();
//
//    	DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
//    	dictionaryDAO.setTenantService(tenantService);
//
//    	CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
//    	compiledModelsCache.setDictionaryDAO(dictionaryDAO);
//    	compiledModelsCache.setTenantService(tenantService);
//    	compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());
//
//    	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
//    			2,
//    			4,
//    			1000,
//    			TimeUnit.MILLISECONDS,
//    			new LinkedBlockingQueue<Runnable>()
//    			);
//    	compiledModelsCache.setThreadPoolExecutor(threadPoolExecutor);
//
//    	dictionaryDAO.setDictionaryRegistryCache(compiledModelsCache);
//    	dictionaryDAO.setResourceClassLoader(getClass().getClassLoader());
//    	dictionaryDAO.init();
//
//    	this.dictionaryDAO = dictionaryDAO;
//    	this.namespaceDAO = dictionaryDAO;
    }

//    private void buildWrappingDictionary()
//    {
//        namespaceService = new DictionaryNamespaceComponent();
//        namespaceService.setNamespaceDAO(namespaceDAO);
//
//    	this.wrappingDictionaryDAO = new WrappingDictionaryDAO(dictionaryDAO, namespaceDAO,
//    	        namespaceService, solrAPIClient);
//    	
//    	new WrappingDictionaryService(dictionaryDAODelegate, namespaceDAODelegate, modelGetter)
//
//    	this.dictionaryService = new DictionaryComponent();
//    	dictionaryService.setDictionaryDAO(wrappingDictionaryDAO);
//
////    	namespaceService.setNamespaceDAO(wrappingDictionaryDAO);
//    }

//    private void buildSOLRAPIClient()
//    {
//        SOLRAPIClient solrAPIClient = new SOLRAPIClient(repoClient, dictionaryService, namespaceDAO);
//        this.solrAPIClient = solrAPIClient;
//    }

    private void buildContentGetter()
    {
    	this.contentGetter = new ContentGetterImpl(repoClient);
    }

    public ContentGetter getContentGetter()
    {
    	return contentGetter;
    }

	public NamespaceService getNamespaceService()
	{
		return namespaceService;
	}

	public DictionaryService getDictionaryService()
	{
		return dictionaryService;
	}

	public PropertySerializer getPropertySerializer()
	{
		return propertySerializer;
	}
}
