/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentStoreFactory
{
    private static Log logger = LogFactory.getLog(ContentStoreFactory.class);
    

    public static ContentStoreAdmin createContentStore(ContentConfig config)
    {
        ContentStoreAdmin contentStore = null;
        Store store = (config == null ? null : config.getStore());
        if (store == null)
        {
            store = Store.FILE;
        }
        
        if (store == Store.FILE)
        {
            if (logger.isDebugEnabled())
                logger.debug("Constructing file content store with " + (config == null ? "no config" : config));
            
            contentStore = new FileStore(config);
        }
//        else if (store == Store.S3)
//        {
//            if (logger.isDebugEnabled())
//                logger.debug("Constructing s3 content store with " + (config == null ? "no config" : config));
//            
//            contentStore = new S3Store(config);
//        }
        
        return contentStore;
    }
}
