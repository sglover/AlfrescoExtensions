/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.content;

import java.io.File;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.safehaus.uuid.UUIDGenerator;

public class FileStore implements ContentStoreAdmin
{
    private static Log logger = LogFactory.getLog(FileStore.class);

    private TempFiles tempFiles = new TempFiles();
    private File rootPath;
    
    public FileStore(ContentConfig config)
    {
        this(config == null ? null : config.getRoot());
    }
    
    private FileStore(String root)
    {
        if (root != null && root.length() > 0)
        {
            rootPath = new File(root);
            if (!rootPath.exists())
            {
                if (logger.isDebugEnabled())
                    logger.debug("Creating root path " + root);
                rootPath.mkdirs();
            }
        }

        if (rootPath == null)
        {
            rootPath = TempFiles.getTempDir();
        }
        
        if (logger.isDebugEnabled())
            logger.debug("File store created with root path " + rootPath.getAbsolutePath());
    }
    
    @Override
    public ContentStore createStore(String path)
    {
        File transientRoot = null;
        if (path == null || path.length() == 0)
        {
            transientRoot = rootPath;
        }
        else
        {
            // NOTE: root path starts with /, so this should not match relative 'path'
            String rootAbsolutePath = rootPath.getAbsolutePath();
            if (path.startsWith(rootAbsolutePath))
            {
                transientRoot = new File(path);
            }
            else
            {
                transientRoot = new File(rootPath, path);
            }
        }
        FileStore store = new FileStore(transientRoot.getAbsolutePath());
        return store;
    }
    
    @Override
    public FileReader createReader(ContentReference ref)
    {
        return new FileReader(this, absoluteReference(ref));
    }

    @Override
    public FileWriter createWriter(ContentReference ref)
    {
        return new FileWriter(this, absoluteReference(ref));
    }

    @Override
    public ContentReference create(MimeType mimetype, String encoding)
    {
        StringBuilder path = new StringBuilder(20);
        path.append(UUIDGenerator.getInstance().generateRandomBasedUUID().toString())
            .append("_" + ContentReference.TOKEN_INDEX);
        if (mimetype != null)
        {
            path.append(".")
                .append(mimetype.getExt());
        }
        
        return absoluteReference(new ContentReference(path.toString(), mimetype, encoding));
    }
    
    @Override
    public File createTemp(MimeType mimetype)
    {
        return tempFiles.createTempFile("filestore", mimetype.getExt());
    }
    
    @Override
    public void cleanup()
    {
        tempFiles.cleanup();
    }
    
    @Override
    public void shutDown()
    {
        cleanup();
    }
    
    private ContentReference absoluteReference(ContentReference reference)
    {
        if (reference != null && reference.getPath() != null)
        {
            String path = reference.getPath();
            if (!path.startsWith(File.separator))
            {
                return new ContentReference(absolutePath(path), reference.getMimetype(), reference.getEncoding(), reference.getIndex());
            }
        }
        return reference;
    }
    
    private String absolutePath(String path)
    {
        if (!path.startsWith(File.separator))
        {
            File absoluteFile = new File(rootPath, path);
            return absoluteFile.getAbsolutePath();
        }
        return path;
    }
    
    public static File getFile(ContentReference reference)
    {
        return getFile(reference, true);
    }
    
    public static File getFile(ContentReference reference, boolean mustExist)
    {
        File file = new File(reference.getPath());
        if (mustExist)
        {
            if (!file.exists())
            {
                throw new AlfrescoRuntimeException("File " + file + " does not exist");
            }
            else if (file.isDirectory())
            {
                throw new AlfrescoRuntimeException("File " + file + " is a directory");
            }
        }
        return file;
    }
    
    public static File createFile(ContentReference reference)
    {
        File file = new File(reference.getPath());
        return file;
    }
}
