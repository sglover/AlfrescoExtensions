/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.mail.MessagingException;
//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLException;

import org.alfresco.checksum.PatchDocument;
import org.alfresco.contentstore.patch.PatchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.multipart.MultiPart;

/**
 * 
 * @author sglover
 *
 */
public class CacheHttpClient
{
    private static final Log logger = LogFactory.getLog(CacheHttpClient.class);

    private PatchService patchService;

    public CacheHttpClient(PatchService patchService)
    {
        super();
        this.patchService = patchService;
    }

    private CloseableHttpClient getHttpClient(HttpHost target, HttpClientContext localContext, String username, String password)
    {
    	ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {

    	    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
    	        // Honor 'keep-alive' header
    	        HeaderElementIterator it = new BasicHeaderElementIterator(
    	                response.headerIterator(HTTP.CONN_KEEP_ALIVE));
    	        while (it.hasNext()) {
    	            HeaderElement he = it.nextElement();
    	            String param = he.getName();
    	            String value = he.getValue();
    	            if (value != null && param.equalsIgnoreCase("timeout")) {
    	                try {
    	                    return Long.parseLong(value) * 1000;
    	                } catch(NumberFormatException ignore) {
    	                }
    	            }
    	        }
    	        HttpHost target = (HttpHost) context.getAttribute(
    	                HttpClientContext.HTTP_TARGET_HOST);
    	        if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
    	            // Keep alive for 5 seconds only
    	            return 5 * 1000;
    	        } else {
    	            // otherwise keep alive for 30 seconds
    	            return 30 * 1000;
    	        }
    	    }

    	};

        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {

            public boolean retryRequest(
                    IOException exception,
                    int executionCount,
                    HttpContext context) {
                if (executionCount >= 5) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    // Timeout
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    // Connection refused
                    return false;
                }
                if (exception instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };

    	CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(username, password));
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true)
//                .setStaleConnectionCheckEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                .build();
        CloseableHttpClient httpclient = HttpClients.custom()
        		.setDefaultRequestConfig(defaultRequestConfig)
                .setDefaultCredentialsProvider(credsProvider)
                .setKeepAliveStrategy(keepAliveStrategy)
                .setRetryHandler(retryHandler)
                .build();
        return httpclient;
    }

    public void getNodeById(String hostname, int port, String username, String password,
    		String nodeId, String nodeVersion, HttpCallback callback) throws IOException
    {
    	HttpHost target = new HttpHost(hostname, port, "http");

    	// Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);
        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

    	CloseableHttpClient httpClient = getHttpClient(target, localContext, username, password);
        try
        {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000)
                    .setConnectTimeout(1000)
                    .build();

    		String uri = "http://"
    				+ hostname
    				+ ":"
    				+ port
    				+ "/alfresco/api/-default-/private/alfresco/versions/1/contentByNodeId/"
    				+ nodeId
    				+ "/"
    				+ nodeVersion;
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Content-Type", "text/plain");
            httpGet.setConfig(requestConfig);



            System.out.println("Executing request " + httpGet.getRequestLine());
            CloseableHttpResponse response = httpClient.execute(target, httpGet, localContext);
            try
            {
            	callback.execute(response.getEntity().getContent());
//                EntityUtils.consume(response.getEntity());
            } 
            finally
            {
                response.close();
            }
        }
        finally 
        {
            httpClient.close();
        }
    }

/*    public List<Patch> getPatches(String hostname, int port, String username, String password,
    		String nodeId, long nodeVersion, HttpCallback callback) throws IOException, MessagingException
    {
		final ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);
		final Client client = Client.create(config);
		client.addFilter(new HTTPBasicAuthFilter(username, password));

		String uri = "http://"
				+ hostname
				+ ":"
				+ port
				+ "/alfresco/api/-default-/private/alfresco/versions/1/patch/"
				+ nodeId
				+ "/"
				+ nodeVersion;

		List<Patch> patches = new LinkedList<>();

		final WebResource resource = client.resource(uri);

//		final HttpEntity response = resource.get(HttpEntity.class);

//		EntityUtils.
		final MimeMultipart response = resource.get(MimeMultipart.class);
		// This will iterate the individual parts of the multipart response
		for (int i = 0; i < response.getCount(); i++)
		{
		    final BodyPart part = response.getBodyPart(i);

			int lastMatchIndex = (Integer)part.getContent();
			int size = (Integer)part.getContent();
			InputStream is = part.getInputStream();
			Patch patch = new Patch(lastMatchIndex, size, is);
			patches.add(patch);

//		    System.out.printf(
//		            "Embedded Body Part [Mime Type: %s, Length: %s]\n",
//		            part.getContentType(), part.getSize());
		}

		return patches;

//    	HttpHost target = new HttpHost(hostname, port, "http");
//
//    	// Create AuthCache instance
//        AuthCache authCache = new BasicAuthCache();
//        // Generate BASIC scheme object and add it to the local
//        // auth cache
//        BasicScheme basicAuth = new BasicScheme();
//        authCache.put(target, basicAuth);
//        // Add AuthCache to the execution context
//        HttpClientContext localContext = HttpClientContext.create();
//        localContext.setAuthCache(authCache);
//
//    	CloseableHttpClient httpClient = getHttpClient(target, localContext, username, password);
//        try
//        {
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setSocketTimeout(1000)
//                    .setConnectTimeout(1000)
//                    .build();
//
//    		String uri = "http://"
//    				+ hostname
//    				+ ":"
//    				+ port
//    				+ "/alfresco/api/-default-/private/alfresco/versions/1/patch/"
//    				+ nodeId
//    				+ "/"
//    				+ nodeVersion;
//            HttpGet httpGet = new HttpGet(uri);
//            httpGet.setHeader("Content-Type", "text/plain");
//            httpGet.setConfig(requestConfig);
//
//            System.out.println("Executing request " + httpGet.getRequestLine());
//            CloseableHttpResponse response = httpClient.execute(target, httpGet, localContext);
//            try
//            {
//            	callback.execute(response.getEntity().getContent());
////                EntityUtils.consume(response.getEntity());
//            } 
//            finally
//            {
//                response.close();
//            }
//        }
//        finally 
//        {
//            httpClient.close();
//        }
    }*/

	public PatchDocument getPatches(String hostname, int port, String username, String password,
			String nodeId, long nodeVersion) throws MessagingException, IOException
	{
		StringBuilder sb = new StringBuilder("http://");
		sb.append(hostname);
		sb.append(":");
		sb.append(port);
		sb.append("/alfresco/api/-default-/private/alfresco/versions/1/patch/");
		sb.append(nodeId);
		sb.append("/");
		sb.append(nodeVersion);
		String url = sb.toString();

		final ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);
		final Client client = Client.create(config);

		final WebResource resource = client.resource(url);
		final MultiPart response = resource.get(MultiPart.class);
		return patchService.getPatch(response);
	}
}
