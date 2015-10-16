/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.checksum;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author sglover
 *
 */
public class NodeChecksums implements Serializable
{
    private static final long serialVersionUID = -2008780747092612209L;

    private String nodeId;
    private Long nodeInternalId;
    private Long nodeVersion;
    private String versionLabel;
    private String contentUrl;
    private int blockSize;
    private Map<Integer, List<Checksum>> checksums;
    private long numBlocks;

    public NodeChecksums()
    {

    }

    public NodeChecksums(String nodeId, Long nodeInternalId, Long nodeVersion,
            String versionLabel, String contentUrl, int blockSize,
            long numBlocks)
    {
        this(nodeId, nodeInternalId, nodeVersion, versionLabel, contentUrl, blockSize, numBlocks, null);
    }

    public NodeChecksums(String nodeId, Long nodeInternalId, Long nodeVersion,
            String versionLabel, String contentUrl, int blockSize,
            long numBlocks, List<Checksum> checksums)
    {
        super();
        this.nodeId = nodeId;
        this.nodeInternalId = nodeInternalId;
        this.nodeVersion = nodeVersion;
        this.versionLabel = versionLabel;
        this.contentUrl = contentUrl;
        this.blockSize = blockSize;
        this.numBlocks = numBlocks;
        this.checksums = new HashMap<>();
        if(checksums != null && !checksums.isEmpty())
        {
            for(Checksum checksum : checksums)
            {
                addChecksum(checksum);
            }
        }
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public Long getNodeVersion()
    {
        return nodeVersion;
    }

    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getNodeInternalId()
    {
        return nodeInternalId;
    }

    public void setNodeInternalId(Long nodeInternalId)
    {
        this.nodeInternalId = nodeInternalId;
    }

    public String getVersionLabel()
    {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
    }

    public long getNumBlocks()
    {
        return numBlocks;
    }

    public void setNumBlocks(long numBlocks)
    {
        this.numBlocks = numBlocks;
    }

    public int getBlockSize()
    {
        return blockSize;
    }

    public void setBlockSize(int blockSize)
    {
        this.blockSize = blockSize;
    }

    public String getContentUrl()
    {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl)
    {
        this.contentUrl = contentUrl;
    }

    public void setChecksums(Map<Integer, List<Checksum>> checksums)
    {
        this.checksums = checksums;
    }

    public List<Checksum> getChecksums(int hash)
    {
        return checksums.get(hash);
    }

    public void addChecksum(Checksum checksum)
    {
        List<Checksum> checksums = this.checksums.get(checksum.getHash());
        if (checksums == null)
        {
            checksums = new LinkedList<>();
            this.checksums.put(checksum.getHash(), checksums);
        }
        checksums.add(checksum);
    }

    public void addChecksums(Collection<Checksum> checksums)
    {
        for(Checksum checksum : checksums)
        {
            addChecksum(checksum);
        }
    }

    public Map<Integer, List<Checksum>> getChecksums()
    {
        return checksums;
    }

    @Override
    public String toString()
    {
        return "NodeChecksums [nodeId=" + nodeId + ", nodeInternalId="
                + nodeInternalId + ", nodeVersion=" + nodeVersion
                + ", versionLabel=" + versionLabel + ", contentUrl="
                + contentUrl + ", blockSize=" + blockSize + ", checksums="
                + checksums + ", numBlocks=" + numBlocks + "]";
    }

}
