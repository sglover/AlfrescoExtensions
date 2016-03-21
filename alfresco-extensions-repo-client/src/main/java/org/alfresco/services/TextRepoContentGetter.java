/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.GetRequest;
import org.alfresco.httpclient.Response;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.services.solr.GetTextContentResponse;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthenticationException;

/**
 * 
 * @author sglover
 *
 */
public class TextRepoContentGetter implements TextContentGetter
{
    private static final String GET_CONTENT = "api/solr/textContent";

    private static final Log logger = LogFactory.getLog(TextRepoContentGetter.class);

    private AlfrescoHttpClient repoClient;

    public TextRepoContentGetter(RepoClientBuilder clientBuilder)
    {
        this.repoClient = clientBuilder.getRepoClient();
    }

//    private static AlfrescoHttpClient buildClient(String repoHost, int
//            repoPort, int repoSSLPort, String repoUserName,
//            String repoPassword, int maxTotalConnections, int maxHostConnections, int
//            socketTimeout,
//            String sslKeyStoreLocation)
//    {
//        AlfrescoHttpClient client = HttpClientBuilder
//                .start()
//                .setAlfrescoHost(repoHost)
//                .setAlfrescoPort(repoPort)
//                .setAlfrescoSSLPort(repoSSLPort)
//                .setMaxHostConnections(maxHostConnections)
//                .setSocketTimeout(socketTimeout)
//                .setMaxTotalConnections(maxTotalConnections)
//                .setSslKeyStoreLocation(sslKeyStoreLocation)
//                .
//                .build();
//        return client;
//    }

    @Override
    public Content getContentByNodeId(long nodeId)
    {
        Content content = null;

        long start = System.currentTimeMillis();

        try
        {
            GetTextContentResponse response;
            try
            {
                response = getTextContent(nodeId, ContentModel.PROP_CONTENT, 0l);
                if(response != null)
                {
                    ReadableByteChannel channel= response.getContent();
                    content = new Content(channel, -1l); // TODO -1
                }
            }
            catch (AuthenticationException | IOException
                    | org.alfresco.httpclient.AuthenticationException e)
            {
                logger.warn(e);
            }

            return content;
        }
        finally
        {
            long end = System.currentTimeMillis();
            logger.debug("Text content get time = " + (end - start));
        }
    }

    public GetTextContentResponse getTextContent(Long nodeId,
            QName propertyQName, Long modifiedSince) throws AuthenticationException, IOException,
            org.alfresco.httpclient.AuthenticationException
    {
        StringBuilder url = new StringBuilder(128);
        url.append(GET_CONTENT);

        StringBuilder args = new StringBuilder(128);
        if (nodeId != null)
        {
            args.append("?");
            args.append("nodeId");
            args.append("=");
            args.append(nodeId);
        }
        else
        {
            throw new NullPointerException(
                    "getTextContent(): nodeId cannot be null.");
        }
        if (propertyQName != null)
        {
            if (args.length() == 0)
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

        if (modifiedSince != null)
        {
            Map<String, String> headers = new HashMap<String, String>(1, 1.0f);
            headers.put("If-Modified-Since", String.valueOf(DateUtil
                    .formatDate(new Date(modifiedSince))));
            req.setHeaders(headers);
        }

        Response response = repoClient.sendRequest(req);

        if (response.getStatus() != HttpServletResponse.SC_NOT_MODIFIED
                && response.getStatus() != HttpServletResponse.SC_NO_CONTENT
                && response.getStatus() != HttpServletResponse.SC_OK)
        {
            throw new AlfrescoRuntimeException(
                    "GetTextContentResponse return status is "
                            + response.getStatus());
        }

        return new GetTextContentResponse(response);
    }
}
