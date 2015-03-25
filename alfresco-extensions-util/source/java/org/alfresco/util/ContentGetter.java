/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.util;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.httpclient.AuthenticationException;

/**
 * 
 * @author sglover
 *
 */
public interface ContentGetter
{
	InputStream getContent(String nodeId) throws AuthenticationException, IOException;
    InputStream getTextContent(long nodeId) throws AuthenticationException, IOException;
}
