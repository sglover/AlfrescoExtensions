/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.httpclient.GetRequest;
import org.alfresco.httpclient.Response;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.services.solr.GetTextContentResponse;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * 
 * @author sglover
 *
 */
public class ContentGetterImpl implements ContentGetter
{
    private static final String GET_CONTENT = "api/solr/textContent";

    private static final Log logger = LogFactory.getLog(ContentGetterImpl.class);

    private String repoUserName;
    private String repoPassword;
    private AlfrescoHttpClient repoClient;
    private SessionFactoryImpl cmisFactory;
    private Session cmisSession;

//    private static AlfrescoHttpClient buildClient(String repoHost, int repoPort, int repoSSLPort, String repoUserName,
//    		String repoPassword, int maxTotalConnections, int maxHostConnections, int socketTimeout,
//    		String sslKeyStoreLocation)
//    {
//    	AlfrescoHttpClient client = HttpClientBuilder
//        		.start()
//        		.setAlfrescoHost(repoHost)
//        		.setAlfrescoPort(repoPort)
//        		.setAlfrescoSSLPort(repoSSLPort)
//        		.setMaxHostConnections(maxHostConnections)
//        		.setSocketTimeout(socketTimeout)
//        		.setMaxTotalConnections(maxTotalConnections)
//        		.setSslKeyStoreLocation(sslKeyStoreLocation)
//        		.
//        		.build();
//    	return client;
//    }

    public ContentGetterImpl(String repoHost, int repoPort, int repoSSLPort, String repoUserName,
    		String repoPassword, RepoClientBuilder clientBuilder)
    {
        this(repoHost, repoPort, repoUserName, repoPassword, clientBuilder.getRepoClient());
    }

	public ContentGetterImpl(String repoHost, int repoPort, String repoUserName, String repoPassword, AlfrescoHttpClient repoClient)
	{
		this.repoUserName = repoUserName;
		this.repoPassword = repoPassword;
		this.repoClient = repoClient;
		this.cmisFactory = SessionFactoryImpl.newInstance();
	}

    private Session getCMISSession()
    {
    	if(this.cmisSession == null)
    	{
    		Map<String, String> parameters = new HashMap<String, String>();
    		parameters.put(SessionParameter.USER, repoUserName);
    		parameters.put(SessionParameter.PASSWORD, repoPassword);
    		parameters.put(SessionParameter.BROWSER_URL, "http://localhost:8080/alfresco/api/-default-/public/cmis/versions/1.1/browser");
    		parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
    		parameters.put(SessionParameter.REPOSITORY_ID, "-default-");

    		this.cmisSession = cmisFactory.createSession(parameters);
    	}

    	return this.cmisSession;
    }

    @Override
	public Content getContent(String nodeId, String nodeVersion)
	{
    	Content content = null;

		StringBuilder sb = new StringBuilder(nodeId);
		if(nodeVersion != null)
		{
			sb.append(";");
			sb.append(nodeVersion);
		}
		ObjectId objectId = new ObjectIdImpl(sb.toString());

		Session session = getCMISSession();
		try
		{
			Document document = (Document)session.getObject(objectId);
			if(document != null)
			{
				if(document.isLatestVersion())
				{
			    	String mimeType = (String)document.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE).getFirstValue();
			    	BigInteger size = (BigInteger)document.getProperty(PropertyIds.CONTENT_STREAM_LENGTH).getFirstValue();
			    	ContentStream stream = session.getContentStream(objectId);
			    	if(stream != null)
			    	{
			    		InputStream is = stream.getStream();
			        	content = new Content(is, mimeType, size.longValue());
			    	}
				}
				else
				{
					logger.warn("Node " + nodeId + "." + nodeVersion + " not latest version");
				}
			}
			else
			{
				logger.warn("Node " + nodeId + "." + nodeVersion + " not found");
			}
		}
		catch(CmisObjectNotFoundException e)
		{
			logger.warn("Node " + nodeId + "." + nodeVersion + " not found");
		}

    	return content;
	}

	public GetTextContentResponse getTextContent(Long nodeId, QName propertyQName, Long modifiedSince) throws AuthenticationException, IOException
    {
        StringBuilder url = new StringBuilder(128);
        url.append(GET_CONTENT);
        
        StringBuilder args = new StringBuilder(128);
        if(nodeId != null)
        {
            args.append("?");
            args.append("nodeId");
            args.append("=");
            args.append(nodeId);            
        }
        else
        {
            throw new NullPointerException("getTextContent(): nodeId cannot be null.");
        }
        if(propertyQName != null)
        {
            if(args.length() == 0)
            {
                args.append("?");
            }
            else
            {
                args.append("&");
            }
            args.append("propertyQName");
            args.append("=");
            args.append(URLEncoder.encode(propertyQName.toString()));
        }
        
        url.append(args);
        
        GetRequest req = new GetRequest(url.toString());
        
        if(modifiedSince != null)
        {
            Map<String, String> headers = new HashMap<String, String>(1, 1.0f);
            headers.put("If-Modified-Since", String.valueOf(DateUtil.formatDate(new Date(modifiedSince))));
            req.setHeaders(headers);
        }

        Response response = repoClient.sendRequest(req);

        if(response.getStatus() != HttpServletResponse.SC_NOT_MODIFIED &&
                response.getStatus() != HttpServletResponse.SC_NO_CONTENT &&
                response.getStatus() != HttpServletResponse.SC_OK)
        {
            throw new AlfrescoRuntimeException("GetTextContentResponse return status is " + response.getStatus());
        }

        return new GetTextContentResponse(response);
	}

    public GetTextContentResponse getTextContent(long nodeId) throws AuthenticationException, IOException
    {
    	long start = System.currentTimeMillis();

    	GetTextContentResponse response = null;

    	try
    	{
    		response = getTextContent(nodeId, ContentModel.PROP_CONTENT, 0l);
	    	return response;
    	}
    	finally
    	{
        	long end = System.currentTimeMillis();
        	logger.debug("Text content get time = " + (end - start));
    	}
    }
}
