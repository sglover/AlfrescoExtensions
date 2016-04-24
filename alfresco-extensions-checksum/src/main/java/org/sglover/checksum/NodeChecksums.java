/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.checksum;

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
    private int blockSize;
    private Map<Integer, List<Checksum>> checksumsByHash;
    private Map<Integer, Checksum> checksumsByBlock;
    private long numBlocks;

    public NodeChecksums()
    {
        this.checksumsByHash = new HashMap<>();
        this.checksumsByBlock = new HashMap<>();
    }

    public NodeChecksums(String nodeId, Long nodeInternalId, Long nodeVersion,
            String versionLabel, int blockSize)
    {
        this(nodeId, nodeInternalId, nodeVersion, versionLabel, blockSize, 0, null);
    }

    public NodeChecksums(String nodeId, Long nodeInternalId, Long nodeVersion,
            String versionLabel, int blockSize, long numBlocks)
    {
        this(nodeId, nodeInternalId, nodeVersion, versionLabel, blockSize, numBlocks, null);
    }

    public NodeChecksums(String nodeId, Long nodeInternalId, Long nodeVersion,
            String versionLabel, int blockSize, long numBlocks, List<Checksum> checksums)
    {
        this();
        this.nodeId = nodeId;
        this.nodeInternalId = nodeInternalId;
        this.nodeVersion = nodeVersion;
        this.versionLabel = versionLabel;
        this.blockSize = blockSize;
        this.numBlocks = numBlocks;
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

    public void setChecksums(Map<Integer, List<Checksum>> checksums)
    {
        this.checksumsByHash = checksums;
    }

    public List<Checksum> getChecksums(int hash)
    {
        return checksumsByHash.get(hash);
    }

    public void addChecksum(Checksum checksum)
    {
        List<Checksum> checksums = this.checksumsByHash.get(checksum.getHash());
        if (checksums == null)
        {
            checksums = new LinkedList<>();
            this.checksumsByHash.put(checksum.getHash(), checksums);
        }
        this.checksumsByBlock.put(checksum.getBlockIndex(), checksum);
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
        return checksumsByHash;
    }

    public Map<Integer, Checksum> getChecksumsByBlock()
    {
        return checksumsByBlock;
    }

    @Override
    public String toString()
    {
        return "NodeChecksums [nodeId=" + nodeId + ", nodeInternalId="
                + nodeInternalId + ", nodeVersion=" + nodeVersion
                + ", versionLabel=" + versionLabel + ", blockSize=" + blockSize + ", checksums="
                + checksumsByHash + ", numBlocks=" + numBlocks + "]";
    }

}
