/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.solr;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.httpclient.Response;
import org.apache.commons.httpclient.HttpStatus;

// TODO register a stream close listener that release the response when the response has been read
/**
 * 
 * @author sglover
 *
 */
public class GetTextContentResponse extends SOLRResponse
{
    private InputStream content;
    private SolrApiContentStatus status;
    private String transformException;
    private String transformStatusStr;
    private Long transformDuration;

    public GetTextContentResponse(Response response) throws IOException
    {
        super(response);

        this.content = response.getContentAsStream();
        this.transformStatusStr = response.getHeader("X-Alfresco-transformStatus");
        this.transformException = response.getHeader("X-Alfresco-transformException");
        String tmp = response.getHeader("X-Alfresco-transformDuration");
        this.transformDuration = (tmp != null && !tmp.equals("") ? Long.valueOf(tmp) : null);
        setStatus();
    }

    public InputStream getContent()
    {
        return content;
    }

    public SolrApiContentStatus getStatus()
    {
        return status;
    }
    
    private void setStatus()
    {
        int status = response.getStatus();
        if(status == HttpStatus.SC_NOT_MODIFIED)
        {
            this.status = SolrApiContentStatus.NOT_MODIFIED;
        }
        else if(status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
        {
            this.status = SolrApiContentStatus.GENERAL_FAILURE;
        }
        else if(status == HttpStatus.SC_OK)
        {
            this.status = SolrApiContentStatus.OK;
        }
        else if(status == HttpStatus.SC_NO_CONTENT)
        {
            if(transformStatusStr == null)
            {
                this.status = SolrApiContentStatus.UNKNOWN;
            }
            else
            {
                if(transformStatusStr.equals("noTransform"))
                {
                    this.status = SolrApiContentStatus.NO_TRANSFORM;
                }
                else if(transformStatusStr.equals("transformFailed"))
                {
                    this.status = SolrApiContentStatus.TRANSFORM_FAILED;
                }
                else
                {
                    this.status = SolrApiContentStatus.UNKNOWN;
                }
            }
        }
    }

    public String getTransformException()
    {
        return transformException;
    }
    
    public void release()
    {
        response.release();
    }
    
    public Long getTransformDuration()
    {
        return transformDuration;
    }
}