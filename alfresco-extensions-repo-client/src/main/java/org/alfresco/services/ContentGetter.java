/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.IOException;

/**
 * 
 * @author sglover
 *
 */
public interface ContentGetter
{
    Content getContentByNodeId(String nodeId, Long nodeVersion) throws IOException;
//    GetTextContentResponse getTextContent(String nodeId, long nodeVersion)
//            throws AuthenticationException, IOException;
}
