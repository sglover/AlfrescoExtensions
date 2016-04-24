/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

/**
 * 
 * @author sglover
 *
 */
public class InvalidNodeException extends RuntimeException
{
    private static final long serialVersionUID = 5914975526875322107L;

    private String nodeId;
    private long nodeInternalVersion;

    public InvalidNodeException(String nodeId, long nodeInternalVersion)
    {
        this.nodeId = nodeId;
        this.nodeInternalVersion = nodeInternalVersion;
    }

    public long getNodeInternalVersion()
    {
        return nodeInternalVersion;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    @Override
    public String toString()
    {
        return "InvalidNodeException [nodeId=" + nodeId
                + ", nodeInternalVersion=" + nodeInternalVersion + "]";
    }
}
