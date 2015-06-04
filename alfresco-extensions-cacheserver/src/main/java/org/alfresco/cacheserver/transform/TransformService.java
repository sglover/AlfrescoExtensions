/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.transform;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.transformation.api.MimeType;
import org.alfresco.transformation.client.TransformationCallback;

/**
 * 
 * @author sglover
 *
 */
public interface TransformService
{
	String transform(String path, MimeType mimeType, TransformationCallback callback) throws IOException;
	InputStream getContent(String contentPath) throws IOException;
}
