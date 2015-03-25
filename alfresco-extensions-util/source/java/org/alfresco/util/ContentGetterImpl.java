/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.httpclient.HttpClientFactory.SecureCommsType;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.solr.client.SOLRAPIClient;
import org.alfresco.solr.client.SOLRAPIClient.GetTextContentResponse;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class ContentGetterImpl implements ContentGetter
{
    private static final Log logger = LogFactory.getLog(ContentGetterImpl.class);

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

	private String username;
	private String password;
    private SessionFactory cmisFactory;
    private Session cmisSession;

    private SOLRAPIClient alfrescoClient;

	public ContentGetterImpl(String username, String password, NamespaceDAO namespaceDAO,
			DictionaryService dictionaryService)
	{
		this.username = username;
		this.password = password;
		this.cmisFactory = SessionFactoryImpl.newInstance();

        this.alfrescoClient = new SOLRAPIClient(getRepoClient(SecureCommsType.NONE), 
                dictionaryService, namespaceDAO);
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

    private Session getCMISSession()
    {
    	if(cmisSession == null)
    	{
    		Map<String, String> parameters = new HashMap<String, String>();
    		parameters.put(SessionParameter.USER, username);
    		parameters.put(SessionParameter.PASSWORD, password);
    		parameters.put(SessionParameter.BROWSER_URL, "http://localhost:8080/alfresco/api/-default-/public/cmis/versions/1.1/browser");
    		parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
    		parameters.put(SessionParameter.REPOSITORY_ID, "-default-");

//    		try
//    		{
    			cmisSession = cmisFactory.createSession(parameters);
//    		}
//    		catch(ConnectException e)
//    		{
//    			
//    		}
    	}

    	return cmisSession;
    }

	public InputStream getContent(String nodeId)
	{
		InputStream is = null;

		ObjectId objectId = new ObjectIdImpl(nodeId);

    	ContentStream stream = getCMISSession().getContentStream(objectId);
    	if(stream != null)
    	{
	    	is = stream.getStream();
    	}

    	return is;
	}
	
    public InputStream getTextContent(long nodeId) throws AuthenticationException, IOException
    {
    	GetTextContentResponse response = alfrescoClient.getTextContent(nodeId, ContentModel.PROP_CONTENT, 0l);
    	return response.getContent();
    }
}
