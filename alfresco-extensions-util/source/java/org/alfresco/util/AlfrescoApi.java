/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.httpclient.HttpClientFactory.SecureCommsType;
import org.alfresco.repo.dictionary.CompiledModelsCache;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.solr.client.SOLRAPIClient;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoApi
{
	private String username = "admin";
	private String password = "admin";

    private String alfrescoHost = "localhost";
    private String baseUrl = "/alfresco";
    private int alfrescoPort = 8080;
    private int alfrescoPortSSL = 8443;
    private int maxTotalConnections = 2;
    private int maxHostConnections = 2;
    private int socketTimeout = 1000;

    private String sslKeyStoreType = "JCEKS";
    private String sslKeyStoreProvider = null;
    private String sslKeyStoreLocation = "ssl.repo.client.keystore";
    private String sslKeyStorePasswordFileLocation = "ssl-keystore-passwords.properties";
    private String sslTrustStoreType = "JCEKS";
    private String sslTrustStoreProvider = null;
    private String sslTrustStorePasswordFileLocation = "ssl-truststore-passwords.properties";
    private String sslTrustStoreLocation = "ssl.repo.client.truststore";

    private DictionaryNamespaceComponent namespaceService;
    private DictionaryDAO dictionaryDAO;
    private NamespaceDAO namespaceDAO;
    private DictionaryComponent dictionaryService;

    private WrappingDictionaryDAO wrappingDictionaryDAO;

    private SOLRAPIClient solrAPIClient;

    private ContentGetter contentGetter;

    public AlfrescoApi()
    {
    	buildDictionary();
    	buildSOLRAPIClient();
    	buildWrappingDictionary();
    	buildContentGetter();
    }

    protected AlfrescoHttpClient getRepoClient(SecureCommsType commsType)
    {
        // TODO i18n
        KeyStoreParameters keyStoreParameters = new KeyStoreParameters("SSL Key Store", sslKeyStoreType, sslKeyStoreProvider, sslKeyStorePasswordFileLocation, sslKeyStoreLocation);
        KeyStoreParameters trustStoreParameters = new KeyStoreParameters("SSL Trust Store", sslTrustStoreType, sslTrustStoreProvider, sslTrustStorePasswordFileLocation, sslTrustStoreLocation);
        SSLEncryptionParameters sslEncryptionParameters = new SSLEncryptionParameters(keyStoreParameters, trustStoreParameters);
        
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

    private void buildDictionary()
    {
    	TenantService tenantService = new SingleTServiceImpl();

    	DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
    	dictionaryDAO.setTenantService(tenantService);

    	CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
    	compiledModelsCache.setDictionaryDAO(dictionaryDAO);
    	compiledModelsCache.setTenantService(tenantService);
    	compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());

    	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
    			2,
    			4,
    			1000,
    			TimeUnit.MILLISECONDS,
    			new LinkedBlockingQueue<Runnable>()
    			);
    	compiledModelsCache.setThreadPoolExecutor(threadPoolExecutor);

    	dictionaryDAO.setDictionaryRegistryCache(compiledModelsCache);
    	dictionaryDAO.setResourceClassLoader(getClass().getClassLoader());
    	dictionaryDAO.init();

    	this.dictionaryDAO = dictionaryDAO;
    	this.namespaceDAO = dictionaryDAO;

    	this.dictionaryService = new DictionaryComponent();
    	dictionaryService.setDictionaryDAO(dictionaryDAO);

    	namespaceService = new DictionaryNamespaceComponent();
    	namespaceService.setNamespaceDAO(namespaceDAO);
    }

    private void buildWrappingDictionary()
    {
    	this.wrappingDictionaryDAO = new WrappingDictionaryDAO(dictionaryDAO, namespaceDAO, solrAPIClient);
    }

    private void buildSOLRAPIClient()
    {
    	AlfrescoHttpClient httpClient = getRepoClient(SecureCommsType.NONE);
        SOLRAPIClient solrAPIClient = new SOLRAPIClient(httpClient, dictionaryService, namespaceDAO);
        this.solrAPIClient = solrAPIClient;
    }

    private void buildContentGetter()
    {
    	this.contentGetter = new ContentGetterImpl(username, password, namespaceDAO, dictionaryService);
    }

    public DictionaryDAO getDictionaryDAO()
    {
    	return wrappingDictionaryDAO;
    }

    public ContentGetter getContentGetter()
    {
    	return contentGetter;
    }
}
