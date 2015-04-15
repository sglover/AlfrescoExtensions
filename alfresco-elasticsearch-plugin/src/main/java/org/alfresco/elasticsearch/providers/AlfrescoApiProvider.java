/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.providers;

import org.alfresco.services.AlfrescoApi;
import org.alfresco.services.AlfrescoApiImpl;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Provider;
import org.elasticsearch.common.settings.Settings;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoApiProvider implements Provider<AlfrescoApi>
{
	private Settings settings;

	private AlfrescoApi alfrescoApi;

	@Inject public AlfrescoApiProvider(Settings settings)
	{
		this.settings = settings;
		buildAlfrescoApi();
	}

	private void buildAlfrescoApi()
	{
		String repoHost = settings.get("alfrescoHost", "localhost");
		int repoPort = settings.getAsInt("alfrescoPort", 8080);
		String repoUsername = settings.get("alfrescoUsername", "admin");
		String repoPassword = settings.get("alfrescoPassword", "admin");

		this.alfrescoApi = new AlfrescoApiImpl(repoHost, repoPort, repoUsername, repoPassword);
	}

	@Override
    public AlfrescoApi get()
    {
	    return alfrescoApi;
    }

}
