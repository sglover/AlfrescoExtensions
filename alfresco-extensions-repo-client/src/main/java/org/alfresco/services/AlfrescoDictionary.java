/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoDictionary
{
    private static final Log logger = LogFactory.getLog(AlfrescoDictionary.class);

    private AlfrescoHttpClient repoClient;

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    public AlfrescoDictionary(AlfrescoHttpClient repoClient)
	{
		this.repoClient = repoClient;
		buildDictionary();
	}

    private void buildDictionary()
    {
        WrappingDictionaryService wrappingDictionaryService = new WrappingDictionaryService(repoClient);
        this.dictionaryService = wrappingDictionaryService;
        this.namespaceService = wrappingDictionaryService;
    }

	public NamespaceService getNamespaceService()
	{
		return namespaceService;
	}

	public DictionaryService getDictionaryService()
	{
		return dictionaryService;
	}
}
