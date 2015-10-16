/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.content;

import java.io.File;

public interface ContentStore
{
    ContentReader createReader(ContentReference ref);
    
    ContentWriter createWriter(ContentReference ref);
    
    ContentReference create(MimeType mimetype, String encoding);
    
    File createTemp(MimeType mimetype);
    
    void cleanup();
}
