/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.providers;

import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.services.ContentGetter;
import org.alfresco.services.ContentGetterFactory;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Provider;
import org.elasticsearch.common.settings.Settings;

/**
 * 
 * @author sglover
 *
 */
public class ContentGetterProvider implements Provider<ContentGetter>
{
    private Settings settings;

    private ContentGetter contentGetter;

    @Inject
    public ContentGetterProvider(Settings settings,
            AlfrescoHttpClient repoClient)
    {
        this.settings = settings;
        buildContentGetter(repoClient);
    }

    private void buildContentGetter(AlfrescoHttpClient repoClient)
    {
        String repoHost = settings.get("alfrescoHost", "localhost");
        int repoPort = settings.getAsInt("alfrescoPort", 8080);
        String repoUsername = settings.get("alfrescoUsername", "admin");
        String repoPassword = settings.get("alfrescoPassword", "admin");

        ContentGetterFactory contentGetterFactory = new ContentGetterFactory(
                repoHost, repoPort, repoUsername, repoPassword);
        this.contentGetter = contentGetterFactory.getObject();
    }

    @Override
    public ContentGetter get()
    {
        return contentGetter;
    }

}
