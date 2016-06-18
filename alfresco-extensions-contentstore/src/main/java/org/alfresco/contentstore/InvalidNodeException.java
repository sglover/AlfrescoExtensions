/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

/**
 * 
 * @author sglover
 *
 */
public class InvalidNodeException extends RuntimeException
{
    private static final long serialVersionUID = 1458837576525558222L;

    public InvalidNodeException(String nodeId, long nodeVersion)
    {
    }
}
