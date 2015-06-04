/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.entities;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.cacheserver.dao.EntitiesDAO;
import org.alfresco.cacheserver.entity.Node;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.nlp.CoreNLPEntityTagger;
import org.alfresco.services.nlp.Entities;
import org.alfresco.services.nlp.EntityExtracter;
import org.alfresco.services.nlp.EntityTagger;
import org.alfresco.services.nlp.EntityTaggerCallback;
import org.alfresco.services.nlp.ModelLoader;
import org.alfresco.services.nlp.StanfordEntityTagger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class EntitiesServiceImpl implements EntitiesService
{
	private static Log logger = LogFactory.getLog(EntitiesServiceImpl.class);

//	private String repoUsername;
//	private String repoPassword;
//	private String repoHost;
//	private int repoPort;
//	private int repoSSLPort;
//	private int socketTimeout;
//	private int maxHostConnections;
//	private int maxTotalConnections;
//	private String commsType;
//	private String sslKeyStoreLocation;
//	private String sslTrustStoreLocation;
//	private String sslKeyStoreType;
//	private String sslTrustStoreType;
//	private String sslKeyStoreProvider;
//	private String sslTrustStoreProvider;
//	private String sslKeyStorePasswordFileLocation;
//	private String sslTrustStorePasswordFileLocation;

//	private RepoClientBuilder repoClientBuilder;

	private EntityTagger entityTagger;
	private ModelLoader modelLoader;

	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	private EntityExtracter entityExtracter;
//	private AlfrescoHttpClient repoClient;
	private ContentGetter contentGetter;
	private EntitiesDAO entitiesDAO;

//	public void setRepoUsername(String repoUsername)
//	{
//		this.repoUsername = repoUsername;
//	}
//
//	public void setRepoPassword(String repoPassword)
//	{
//		this.repoPassword = repoPassword;
//	}
//
//	public void setRepoHost(String repoHost)
//	{
//		this.repoHost = repoHost;
//	}
//
//	public void setRepoPort(int repoPort)
//	{
//		this.repoPort = repoPort;
//	}
//
//	public void setRepoSSLPort(int repoSSLPort)
//	{
//		this.repoSSLPort = repoSSLPort;
//	}
//
//	public void setSocketTimeout(int socketTimeout)
//	{
//		this.socketTimeout = socketTimeout;
//	}
//
//	public void setMaxHostConnections(int maxHostConnections)
//	{
//		this.maxHostConnections = maxHostConnections;
//	}
//
//	public void setMaxTotalConnections(int maxTotalConnections)
//	{
//		this.maxTotalConnections = maxTotalConnections;
//	}
//
//	public void setCommsType(String commsType)
//	{
//		this.commsType = commsType;
//	}
//
//	public void setSslKeyStoreLocation(String sslKeyStoreLocation)
//	{
//		this.sslKeyStoreLocation = sslKeyStoreLocation;
//	}
//
//	public void setSslTrustStoreLocation(String sslTrustStoreLocation)
//	{
//		this.sslTrustStoreLocation = sslTrustStoreLocation;
//	}
//
//	public void setSslKeyStoreType(String sslKeyStoreType)
//	{
//		this.sslKeyStoreType = sslKeyStoreType;
//	}
//
//	public void setSslTrustStoreType(String sslTrustStoreType)
//	{
//		this.sslTrustStoreType = sslTrustStoreType;
//	}
//
//	public void setSslKeyStoreProvider(String sslKeyStoreProvider)
//	{
//		this.sslKeyStoreProvider = sslKeyStoreProvider;
//	}
//
//	public void setSslTrustStoreProvider(String sslTrustStoreProvider)
//	{
//		this.sslTrustStoreProvider = sslTrustStoreProvider;
//	}
//
//	public void setSslKeyStorePasswordFileLocation(
//	        String sslKeyStorePasswordFileLocation)
//	{
//		this.sslKeyStorePasswordFileLocation = sslKeyStorePasswordFileLocation;
//	}
//
//	public void setSslTrustStorePasswordFileLocation(
//	        String sslTrustStorePasswordFileLocation)
//	{
//		this.sslTrustStorePasswordFileLocation = sslTrustStorePasswordFileLocation;
//	}

	public void setExecutorService(ExecutorService executorService)
	{
		this.executorService = executorService;
	}

	public void setEntityExtracter(EntityExtracter entityExtracter)
	{
		this.entityExtracter = entityExtracter;
	}

//	public void setRepoClient(AlfrescoHttpClient repoClient)
//	{
//		this.repoClient = repoClient;
//	}

	public void setContentGetter(ContentGetter contentGetter)
	{
		this.contentGetter = contentGetter;
	}

	public static enum ExtracterType
	{
		CoreNLP, StanfordNLP;
	};

	private EntityTagger buildEntityTagger(ExtracterType extracterType)
	{
		EntityTagger entityTagger = null;

        logger.debug("extracterType = " + extracterType);

        switch(extracterType)
        {
        case CoreNLP:
        {
    		entityTagger = new CoreNLPEntityTagger(modelLoader, 8);
        	break;
        }
        case StanfordNLP:
        {
        	entityTagger = StanfordEntityTagger.build();
        	break;
        }
        default:
        	throw new IllegalArgumentException("Invalid entity.extracter.type");
        }

        return entityTagger;
	}

	private EntityExtracter buildEntityExtracter(ExecutorService executorService, EntityTagger entityTagger,
			ContentGetter contentGetter)
	{
		EntityExtracter entityExtracter = new EntityExtracter(contentGetter, entityTagger, executorService);
        return entityExtracter;
	}

	public EntitiesServiceImpl(String extracterTypeStr, ModelLoader modelLoader, ContentGetter contentGetter,
			EntitiesDAO entitiesDAO)
	{
		this.modelLoader = modelLoader;
		ExtracterType extracterType = ExtracterType.valueOf(extracterTypeStr);
		this.entityTagger = buildEntityTagger(extracterType);
		this.contentGetter = contentGetter;
		this.entitiesDAO = entitiesDAO;
	}

	public void init() throws Exception
	{
//		this.repoClient = repoClientBuilder.getRepoClient();
//		this.repoClient = buildAlfrescoHttpClient();
//		this.contentGetter = buildContentGetter(repoClient);
		this.entityExtracter = buildEntityExtracter(executorService, entityTagger, contentGetter);
	}

	public void setModelLoader(ModelLoader modelLoader)
	{
		this.modelLoader = modelLoader;
	}

//	private AlfrescoHttpClient buildAlfrescoHttpClient()
//	{
//		RepoClientFactory repoClientFactory = new RepoClientFactory();
//		repoClientFactory
//			.setAlfrescoHost(repoHost)
//			.setAlfrescoPort(repoPort)
//			.setAlfrescoPortSSL(repoSSLPort)
//			.setCommsType(commsType)
//			.setMaxHostConnections(maxHostConnections)
//						.setMaxTotalConnections(maxTotalConnections)
//			.setSslKeyStoreLocation(sslKeyStoreLocation)
//			.setSslTrustStoreLocation(sslTrustStoreLocation)
//			.setSslKeyStoreType(sslKeyStoreType)
//			.setSslTrustStoreType(sslTrustStoreType)
//			.setSslKeyStoreProvider(sslKeyStoreProvider)
//			.setSslTrustStoreProvider(sslTrustStoreProvider)
//			.setSslKeyStorePasswordFileLocation(sslKeyStorePasswordFileLocation)
//			.setSslTrustStorePasswordFileLocation(sslTrustStorePasswordFileLocation)
//			.setSocketTimeout(socketTimeout);
//		AlfrescoHttpClient repoClient = repoClientFactory.get();
//		return repoClient;
//	}

//	private ContentGetter buildContentGetter(AlfrescoHttpClient repoClient)
//	{
//		ContentGetterFactory contentGetterFactory = new ContentGetterFactory(repoHost, repoPort, repoUsername,
//				repoPassword, repoClient);
//		ContentGetter contentGetter = contentGetterFactory.getObject();
//		return contentGetter;
//	}

	@Override
	public void getEntities(final Node node) throws AuthenticationException, IOException
	{
		EntityTaggerCallback callback = new EntityTaggerCallback()
		{
			
			@Override
			public void onSuccess(Entities entities)
			{
				entitiesDAO.addEntities(null, node, entities);
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(Throwable ex)
			{
				// TODO Auto-generated method stub
				
			}
		};
		entityExtracter.getEntities(node.getNodeInternalId(), callback);
	}

	public void getEntitiesAsync(final Node node)
	{
		executorService.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					getEntities(node);
				}
				catch(AuthenticationException e)
				{
					// TODO
				}
				catch(IOException e)
				{
					// TODO
				}
			}
		});
	}
}
