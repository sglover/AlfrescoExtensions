/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.checksum;

import java.util.List;

/**
 * 
 * @author sglover
 *
 */
public class PatchDocument
{
	private int blockSize;
    private int matchCount;
    private List<Integer> matchedBlocks;
    private List<Patch> patches;
	public PatchDocument(int blockSize, int matchCount,
			List<Integer> matchedBlocks, List<Patch> patches)
    {
	    super();
	    this.blockSize = blockSize;
	    this.matchCount = matchCount;
	    this.matchedBlocks = matchedBlocks;
	    this.patches = patches;
    }
	public int getBlockSize()
	{
		return blockSize;
	}
	public int getMatchCount()
	{
		return matchCount;
	}
	public List<Integer> getMatchedBlocks()
	{
		return matchedBlocks;
	}
	public List<Patch> getPatches()
	{
		return patches;
	}
	@Override
    public String toString()
    {
	    return "PatchDocument [blockSize=" + blockSize + ", matchCount=" + matchCount
	            + ", matchedBlocks=" + matchedBlocks + ", patches=" + patches
	            + "]";
    }
}
