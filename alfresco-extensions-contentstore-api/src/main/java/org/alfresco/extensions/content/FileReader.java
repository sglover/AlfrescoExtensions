/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FileReader implements ContentReader
{
    private static Log logger = LogFactory.getLog(FileReader.class);
    
    private ContentStore store;
    private ContentReference ref;
    

    public FileReader(ContentStore store, ContentReference ref)
    {
        if (!ref.isResolved())
        {
            throw new IllegalArgumentException("Cannot create reader for unresolved content reference " + ref);
        }
        this.store = store;
        this.ref = ref;
    }

    @Override
    public ContentReference getReference()
    {
        return ref;
    }
    
    @Override
    public ContentStore getStore()
    {
        return store;
    }

    @Override
    public File readFile()
    {
        if (logger.isDebugEnabled())
            logger.debug("Reading from " + ref);
        
        return FileStore.getFile(ref);
    }
    
    @Override
    public InputStream readStream()
    {
        File file = readFile();
        try
        {
            return FileUtils.openInputStream(file);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to open stream onto " + file.getAbsolutePath());
        }
    }
}