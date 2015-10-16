/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.content;

import java.io.Serializable;

/**
 * Content reference
 */
public class ContentReference implements Serializable
{
    private static final long serialVersionUID = -7668791663204999211L;
    
    public static String TOKEN_INDEX = "${index}";
    
    private static String FORMAT_SPECIFIER_INDEX = "%1$04d";
    
    private String path;
    private String pathTemplate;
    private boolean isResolved;
    private MimeType mimetype;
    private String encoding;
    private int index;

    /**
     * @param path
     * @param mimetype
     */
    public ContentReference(String path, MimeType mimetype)
    {
        this(path, mimetype, null);
    }

    /**
     * @param path
     * @param mimetype
     * @param index
     */
    public ContentReference(String path, MimeType mimetype, int index)
    {
        this(path, mimetype, null, index);
    }

    /**
     * @param path
     * @param mimetype
     * @param encoding
     */
    public ContentReference(String path, MimeType mimetype, String encoding)
    {
        this(path, mimetype, encoding, 0);
    }

    /**
     * @param path
     * @param mimetype
     * @param encoding
     */
    public ContentReference(String path, MimeType mimetype, String encoding, int index)
    {
        this.path = path;
        this.pathTemplate = createPathTemplate(path);
        this.isResolved = !containsFormatSpecifiers(pathTemplate);
        this.mimetype = mimetype;
        this.encoding = ((encoding == null) ? "UTF-8" : encoding);
        this.index = index;
    }

    private String createPathTemplate(String path)
    {
        if (path != null)
        {
            path = path.replace(TOKEN_INDEX, FORMAT_SPECIFIER_INDEX);
        }
        return path;
    }
    
    private boolean containsFormatSpecifiers(String path)
    {
        return path == null ? false : path.contains(FORMAT_SPECIFIER_INDEX);
    }
    
    /**
     * @return content url
     */
    public String getPath()
    {
        return path;
    }
    
    /**
     * @return  content mimetype
     */
    public MimeType getMimetype()
    {
        return mimetype;
    }
    
    /**
     * @return  content encoding
     */
    public String getEncoding()
    {
        return encoding;
    }
    
    /**
     * @return  content index
     */
    public int getIndex()
    {
        return index;
    }
    
    /**
     * @return  whether content reference path tokens have been resolved
     */
    public boolean isResolved()
    {
        return isResolved;
    }
    
    /**
     * @param values  1st = index
     * @return  resolved reference
     */
    public ContentReference resolve(Object... values)
    {
        String resolvedPath = String.format(pathTemplate, values);
        if (containsFormatSpecifiers(resolvedPath))
        {
            throw new IllegalArgumentException("Cannot resolve all tokens in path " + path);
        }
        int refIndex = (values.length > 0 && values[0].getClass().equals(Integer.class)) ? (int)values[0] : index;
        ContentReference resolvedRef = new ContentReference(resolvedPath, mimetype, encoding, refIndex);
        return resolvedRef;
    }
    
    @Override
    public String toString()
    {
        return "ContentReference[" + ((mimetype == null) ? "" : "<" + mimetype + ">") + path + ",idx=" + index + "]";
    }
}
