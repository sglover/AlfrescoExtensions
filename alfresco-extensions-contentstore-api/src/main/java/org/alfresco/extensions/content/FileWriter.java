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
import java.io.OutputStream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FileWriter implements ContentWriter
{
    private static Log logger = LogFactory.getLog(FileWriter.class);
    
    private ContentStore store;
    private ContentReference ref;
    

    public FileWriter(ContentStore store, ContentReference ref)
    {
        if (!ref.isResolved())
        {
            throw new IllegalArgumentException("Cannot create writer for unresolved content reference " + ref);
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
    public void writeFile(File file)
    {
        File targetFile = FileStore.createFile(ref);
        try
        {
            if (logger.isDebugEnabled())
                logger.debug("Writing to " + ref + " from " + file.getAbsolutePath());
            
            FileUtils.copyFile(file, targetFile);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to write to " + ref + " from " + file.getAbsolutePath(), e);
        }
    }
    
    @Override
    public void writeStream(InputStream stream)
    {
        File targetFile = FileStore.createFile(ref);
        OutputStream output = null;
        
        try
        {
            output = FileUtils.openOutputStream(targetFile);
            
            if (logger.isDebugEnabled())
                logger.debug("Writing to " + ref + " from stream");
            
            IOUtils.copy(stream, output);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to write to " + ref + " from stream", e);
        }
        finally
        {
            IOUtils.closeQuietly(output);
        }
    }
}