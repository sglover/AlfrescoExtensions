/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.transform;

import java.io.IOException;

import org.alfresco.extensions.transformations.client.TransformationCallback;
import org.sglover.alfrescoextensions.common.MimeType;

/**
 * 
 * @author sglover
 *
 */
public interface TransformService
{
	void transformToText(String path, MimeType mimeType, TransformationCallback callback) throws IOException;
	void transformToTextAsync(String path, MimeType mimeType, TransformationCallback callback) throws IOException;
//	InputStream getContent(String contentPath) throws IOException;
}
