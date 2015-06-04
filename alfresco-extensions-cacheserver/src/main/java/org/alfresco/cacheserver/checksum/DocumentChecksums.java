/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.checksum;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author sglover
 *
 */
public class DocumentChecksums
{
	private String contentUrl;
	private int blockSize;
	private Map<Integer, List<Checksum>> checksums;
	private long numBlocks;

	public DocumentChecksums()
	{
		
	}

	public DocumentChecksums(String contentUrl, int blockSize, long numBlocks)
    {
        super();
        this.contentUrl = contentUrl;
        this.blockSize = blockSize;
        this.numBlocks = numBlocks;
        this.checksums = new HashMap<>();;
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
		if(checksums == null)
		{
			checksums = new LinkedList<>();
			this.checksums.put(checksum.getHash(), checksums);
		}
		checksums.add(checksum);
	}

	public Map<Integer, List<Checksum>> getChecksums()
	{
		return checksums;
	}

	@Override
    public String toString()
    {
	    return "DocumentChecksums [contentUrl=" + contentUrl + ", blockSize=" + blockSize + ", checksums="
	            + checksums + ", numBlocks=" + numBlocks + "]";
    }

}
