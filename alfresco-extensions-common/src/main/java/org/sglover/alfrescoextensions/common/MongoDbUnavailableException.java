/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

/**
 * Exception thrown when MongoDB is unavailable
 * 
 * @author rgauss
 */
public class MongoDbUnavailableException extends Exception
{
    private static final long serialVersionUID = -3224782139266711513L;
    
    public MongoDbUnavailableException()
    {
        super();
    }

    public MongoDbUnavailableException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MongoDbUnavailableException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MongoDbUnavailableException(String message)
    {
        super(message);
    }

    public MongoDbUnavailableException(Throwable cause)
    {
        super(cause);
    }

}
