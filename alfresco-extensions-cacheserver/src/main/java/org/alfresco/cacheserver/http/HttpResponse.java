/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author sglover
 *
 */
public interface HttpResponse
{
    InputStream getContentAsStream() throws IOException;
    
    String getHeader(String name);
    
    String getContentType();
    
    int getStatus();
    
    void release() throws IOException;
}
