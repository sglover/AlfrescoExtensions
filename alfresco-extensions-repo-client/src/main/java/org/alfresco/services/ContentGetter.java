/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.IOException;

import org.alfresco.extensions.common.Content;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.services.solr.GetTextContentResponse;

/**
 * 
 * @author sglover
 *
 */
public interface ContentGetter
{
	Content getContentByNodeId(String nodeId, String nodeVersion) throws IOException;
//	Content getContentByNodePath(String path) throws IOException;
	GetTextContentResponse getTextContent(long nodeId) throws AuthenticationException, IOException;
}
