/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.content;


/**
 * Configuration for content store
 */
public class ContentConfig
{
    private Store store;
    private String root;
    private String s3access;
    private String s3secret;
    private String endpoint;
    private String bucket;

    
    public ContentConfig()
    {
    }
    
    public ContentConfig(String store, String root, String s3accessKey, String s3secretKey, String endpoint, String bucket)
    {
        if (store != null)
        {
            setStore(store);
        }
        this.root = root;
        this.s3access = s3accessKey;
        this.s3secret = s3secretKey;
        this.endpoint = endpoint;
        this.bucket = bucket;
    }
    
    public void setStore(String store)
    {
        this.store = Store.valueOf(store.toUpperCase());
        if (this.store == null)
        {
            throw new IllegalArgumentException("Unknown store " + store);
        }
    }

    public Store getStore()
    {
        return store;
    }
    
    //
    // General Config
    //
    
    public String getRoot()
    {
        return root;
    }
    
    //
    // S3 Config
    //
    
    public String getS3access()
    {
        return s3access;
    }

    public String getS3secret()
    {
        return s3secret;
    }

    public String getEndpoint()
    {
        return endpoint;
    }
    
    public String getBucket()
    {
        return bucket;
    }
    
    /**
     * Merge content config into this content config (overriding values in this content config)
     * 
     * @param config  content config to merge
     * @return  new merged content config
     */
    public ContentConfig merge(ContentConfig config)
    {
        if (config == null)
        {
            return this;
        }
        ContentConfig content = new ContentConfig();
        content.store = (config.store == null) ? this.store : config.store;
        content.root = (config.root == null) ? this.root : config.root;
        content.s3access = (config.s3access == null) ? this.s3access : config.s3access;
        content.s3secret = (config.s3secret == null) ? this.s3secret : config.s3secret;
        content.endpoint = (config.endpoint == null) ? this.endpoint : config.endpoint;
        content.bucket = (config.bucket == null) ? this.bucket : config.bucket;
        return content;
    }
    
    @Override
    public String toString()
    {
        if (Store.FILE == store)
        {
            return "ContentConfig[store=" + store + ",root=" + root + "]";
        }
        else if (Store.S3 == store)
        {
            return "ContentConfig[store=" + store + ",endpoint=" + endpoint + ",bucket=" + bucket + ",root=" + root + ",accessKey=" + 
                    (s3access != null ? "<hidden>" : "<notset>") + ",secretKey=" + (s3secret != null ? "<hidden>" : "<notset>") + "]";
        }
        return "ContentConfig[store=<notset>]";
    }
}
