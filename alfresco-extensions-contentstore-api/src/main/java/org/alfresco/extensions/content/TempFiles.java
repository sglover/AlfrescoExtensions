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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TempFiles
{
    private static Log logger = LogFactory.getLog(TempFiles.class);
    
    /** the system property key giving us the location of the temp directory */
    private static final String SYSTEM_KEY_TEMP_DIR = "java.io.tmpdir";
    private static File tempDir;
    
    static
    {
        String tempDirPath = System.getProperty(SYSTEM_KEY_TEMP_DIR);
        if (tempDirPath == null)
        {
            throw new AlfrescoRuntimeException("System property not available: " + SYSTEM_KEY_TEMP_DIR);
        }
        tempDir = new File(tempDirPath);
    }

    private List<File> tempFiles = new ArrayList<>();

    
    public File createTempFile(String prefix, String suffix)
    {
        try
        {
            File temp = File.createTempFile(prefix, "." + suffix);
            
            if (logger.isDebugEnabled())
                logger.debug("Created temp file " + temp.getAbsolutePath());
            
            tempFiles.add(temp);
            return temp;
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to create temp storage file", e);
        }
    }
    
    public static File getTempDir()
    {
        return tempDir;
    }
    
    public void cleanup()
    {
        for (File temp : tempFiles)
        {
            if (temp.exists())
            {
                temp.delete();
                
                if (logger.isDebugEnabled())
                    logger.debug("Deleted temp file " + temp.getAbsolutePath());
            }
        }
        tempFiles.clear();
    }
}
