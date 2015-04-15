/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import org.alfresco.httpclient.AlfrescoHttpClient;

/**
 * 
 * @author sglover
 *
 */
public class ContentGetterFactory
{
    private String alfrescoHost = "localhost";
    private int alfrescoPort = 8080;
    private String alfrescoUserName = "admin";
    private String alfrescoPassword = "admin";
    private AlfrescoHttpClient repoClient;

	public ContentGetterFactory(String alfrescoHost, int alfrescoPort, String alfrescoUserName,
            String alfrescoPassword, AlfrescoHttpClient repoClient)
    {
	    super();
	    this.alfrescoHost = alfrescoHost;
	    this.alfrescoPort = alfrescoPort;
	    this.alfrescoUserName = alfrescoUserName;
	    this.alfrescoPassword = alfrescoPassword;
	    this.repoClient = repoClient;
    }

	public ContentGetter getObject()
	{
    	ContentGetter contentGetter = new ContentGetterImpl(alfrescoHost, alfrescoPort, alfrescoUserName, alfrescoPassword, repoClient);
    	return contentGetter;
	}
}
