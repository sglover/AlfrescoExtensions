/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.solr;

/**
 * 
 * @author sglover
 *
 */
public enum SolrApiContentStatus
{
    NOT_MODIFIED, OK, NO_TRANSFORM, NO_CONTENT, UNKNOWN, TRANSFORM_FAILED, GENERAL_FAILURE;
    
    public static SolrApiContentStatus getStatus(String statusStr)
    {
        if(statusStr.equals("ok"))
        {
            return OK;
        }
        else if(statusStr.equals("transformFailed"))
        {
            return TRANSFORM_FAILED;
        }
        else if(statusStr.equals("noTransform"))
        {
            return NO_TRANSFORM;
        }
        else if(statusStr.equals("noContent"))
        {
            return NO_CONTENT;
        }
        else
        {
            return UNKNOWN;
        }
    }
}
