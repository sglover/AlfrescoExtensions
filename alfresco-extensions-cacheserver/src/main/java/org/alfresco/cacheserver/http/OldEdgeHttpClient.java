/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.http;


/**
 * 
 * @author sglover
 *
 */
public class OldEdgeHttpClient
{
//    private static final Log logger = LogFactory.getLog(EdgeHttpClient.class);
//    
//    public static final String ALFRESCO_DEFAULT_BASE_URL = "/alfresco";
//    
//    public static final int DEFAULT_SAVEPOST_BUFFER = 4096;
//
//    // Remote Server access
//    private Map<EdgeServer, HttpClient> httpClients = new ConcurrentHashMap<>();
//
//    private int socketTimeout;
//    private String baseUrl = ALFRESCO_DEFAULT_BASE_URL;
//
//    private HttpConnectionManager connectionManager;
//
//    public EdgeHttpClient(int socketTimeout, int maxTotalConnections, int maxHostConnections,
//    		int connectionTimeout, String baseUrl)
//    {
//        this.socketTimeout = socketTimeout;
//        this.baseUrl = baseUrl;
//        if(this.baseUrl == null || this.baseUrl.equals("null"))
//        {
//        	this.baseUrl = "";
//        }
//
//        this.connectionManager = new MultiThreadedHttpConnectionManager();
//        HttpConnectionManagerParams connectionManagerParams = connectionManager.getParams();
////        connectionManagerParams.setMaxTotalConnections(maxTotalConnections);
////        connectionManagerParams.setDefaultMaxConnectionsPerHost(maxHostConnections);
////        connectionManagerParams.setConnectionTimeout(connectionTimeout);
//        connectionManagerParams.setMaxTotalConnections(20);
//        connectionManagerParams.setDefaultMaxConnectionsPerHost(20);
//        connectionManagerParams.setConnectionTimeout(60000);
//    }
//
//    private static class EdgeServer
//    {
//    	private String hostname;
//    	private int port;
//
//		public EdgeServer(String hostname, int port)
//        {
//	        super();
//	        this.hostname = hostname;
//	        this.port = port;
//        }
//		@SuppressWarnings("unused")
//        public String getHostname()
//		{
//			return hostname;
//		}
//		@SuppressWarnings("unused")
//        public int getPort()
//		{
//			return port;
//		}
//		@Override
//        public int hashCode()
//        {
//	        final int prime = 31;
//	        int result = 1;
//	        result = prime * result
//	                + ((hostname == null) ? 0 : hostname.hashCode());
//	        result = prime * result + port;
//	        return result;
//        }
//		@Override
//        public boolean equals(Object obj)
//        {
//	        if (this == obj)
//		        return true;
//	        if (obj == null)
//		        return false;
//	        if (getClass() != obj.getClass())
//		        return false;
//	        EdgeServer other = (EdgeServer) obj;
//	        if (hostname == null)
//	        {
//		        if (other.hostname != null)
//			        return false;
//	        } else if (!hostname.equals(other.hostname))
//		        return false;
//	        if (port != other.port)
//		        return false;
//	        return true;
//        }
//    }
//
//    protected HttpClient getHttpClient(Credentials credentials, String hostname, int port)
//    {
//    	EdgeServer edgeServer = new EdgeServer(hostname, port);
//    	HttpClient httpClient = httpClients.get(edgeServer);
//    	if(httpClient == null)
//    	{
//            AuthScope authScope = new AuthScope(hostname, port, AuthScope.ANY_REALM);
//
//	        HttpClientParams params = new HttpClientParams();
//	        params.setBooleanParameter(HttpConnectionParams.TCP_NODELAY, true);
//	        params.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, true);
//	        params.setSoTimeout(socketTimeout);
//	        params.setAuthenticationPreemptive(true);
//    		httpClient = new HttpClient(params, connectionManager);	
//	        httpClient.getState().setCredentials(authScope, credentials);
//
//	        httpClients.put(edgeServer, httpClient);
//    	}
//
//        return httpClient;
//    }
//
//    private boolean isRedirect(HttpMethod method)
//    {
//        switch (method.getStatusCode()) {
//        case HttpStatus.SC_MOVED_TEMPORARILY:
//        case HttpStatus.SC_MOVED_PERMANENTLY:
//        case HttpStatus.SC_SEE_OTHER:
//        case HttpStatus.SC_TEMPORARY_REDIRECT:
//            if (method.getFollowRedirects()) {
//                return true;
//            } else {
//                return false;
//            }
//        default:
//            return false;
//        }
//    }
//    
//    /**
//     * Send Request to the repository
//     */
//    protected HttpMethod sendRemoteRequest(HttpRequest req) throws AuthenticationException, IOException
//    {
//        if (logger.isDebugEnabled())
//        {
//            logger.debug("");
//            logger.debug("* Request: " + req.getMethod() + " " + req.getFullUri()
//            		+ (req.getBody() == null ? "" : "\n" + new String(req.getBody(), "UTF-8")));
//        }
//
//        HttpMethod method = createMethod(req);
//
//        Credentials creds = new UsernamePasswordCredentials(req.getUsername(), req.getPassword());
//
//        // execute method
//        executeMethod(creds, req.getHostname(), req.getPort(), method);
//
//        // Deal with redirect
//        if(isRedirect(method))
//        {
//            Header locationHeader = method.getResponseHeader("location");
//            if (locationHeader != null)
//            {
//                String redirectLocation = locationHeader.getValue();
//                method.setURI(new URI(redirectLocation, true));
//                executeMethod(creds, req.getHostname(), req.getPort(), method);
//            }
//        }
//
//        return method;
//    }
//
//    protected long executeMethod(Credentials credentials, String hostname, int port, HttpMethod method) throws HttpException, IOException
//    {
//        // execute method
//
//        long startTime = System.currentTimeMillis();
//
//        // TODO: Pool, and sent host configuration and state on execution
//        getHttpClient(credentials, hostname, port).executeMethod(method);
//
//        return System.currentTimeMillis() - startTime;
//    }
//
//    protected HttpMethod createMethod(HttpRequest req) throws IOException
//    {
//        StringBuilder url = new StringBuilder(128);
//        url.append(baseUrl);
//        url.append(req.getFullUri());
//
//        // construct method
//        HttpMethod httpMethod = null;
//        String method = req.getMethod();
//        if(method.equalsIgnoreCase("GET"))
//        {
//            GetMethod get = new GetMethod(url.toString());
//            httpMethod = get;
//            httpMethod.setFollowRedirects(true);
//        }
//        else if(method.equalsIgnoreCase("POST"))
//        {
//            PostMethod post = new PostMethod(url.toString());
//            httpMethod = post;
//            ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(req.getBody(), req.getType());
//            if (req.getBody().length > DEFAULT_SAVEPOST_BUFFER)
//            {
//                post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
//            }
//            post.setRequestEntity(requestEntity);
//            // Note: not able to automatically follow redirects for POST, this is handled by sendRemoteRequest
//        }
//        else if(method.equalsIgnoreCase("HEAD"))
//        {
//            HeadMethod head = new HeadMethod(url.toString());
//            httpMethod = head;
//            httpMethod.setFollowRedirects(true);
//        }
//        else
//        {
//            throw new AlfrescoRuntimeException("Http Method " + method + " not supported");
//        }
//
//        if (req.getHeaders() != null)
//        {
//            for (Map.Entry<String, String> header : req.getHeaders().entrySet())
//            {
//                httpMethod.setRequestHeader(header.getKey(), header.getValue());
//            }
//        }
//        
//        return httpMethod;
//    }
//
//    /* (non-Javadoc)
//     * @see org.alfresco.httpclient.AlfrescoHttpClient#close()
//     */
//    public void shutdown()
//    {
//    	for(HttpClient httpClient : httpClients.values())
//    	{
//    		HttpConnectionManager connectionManager = httpClient.getHttpConnectionManager();
//    		if(connectionManager instanceof MultiThreadedHttpConnectionManager)
//    		{
//    			((MultiThreadedHttpConnectionManager)connectionManager).shutdown();
//    		}
//    	}
//    }
//
//    /**
//     * Send Request to the repository
//     */
//    public HttpResponse getNodeById(String hostname, int port, String username, String password, String nodeId, String nodeVersion)
//    		throws AuthenticationException, IOException
//    {
//		HttpRequest request = new GetNodeHttpRequest("GET", hostname, port, username, password, nodeId, nodeVersion);
//		Map<String, String> headers = new HashMap<>();
//		request.setHeaders(headers);
//
//        HttpMethod method = sendRemoteRequest(request);
//        return new HttpMethodResponse(method);
//    }
}
