/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.checksum;

import java.util.List;

/**
 * 
 * @author sglover
 *
 */
public class PatchDocument
{
	private int blockSize;
    private List<Integer> matchedBlocks;
    private List<Patch> patches;
    private int byteSize;

	public PatchDocument(int blockSize, List<Integer> matchedBlocks, List<Patch> patches)
    {
	    super();
	    this.blockSize = blockSize;
	    this.matchedBlocks = matchedBlocks;
	    this.patches = patches;
	    byteSize = 8 + matchedBlocks.size() * 4;
	    for(Patch patch : patches)
	    {
	        int bufLen = patch.getBuffer().length;
	        byteSize += 12 + bufLen;
	    }
    }

	public int getByteSize()
    {
        return byteSize;
    }


    public int getBlockSize()
	{
		return blockSize;
	}
	public int getMatchCount()
	{
		return matchedBlocks.size();
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
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + blockSize;
        result = prime * result
                + ((matchedBlocks == null) ? 0 : matchedBlocks.hashCode());
        result = prime * result + ((patches == null) ? 0 : patches.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PatchDocument other = (PatchDocument) obj;
        if (blockSize != other.blockSize)
            return false;
        if (matchedBlocks.size() != other.matchedBlocks.size())
            return false;
        if (matchedBlocks == null)
        {
            if (other.matchedBlocks != null)
                return false;
        }
        else if (!matchedBlocks.equals(other.matchedBlocks))
            return false;
        if (patches == null)
        {
            if (other.patches != null)
                return false;
        }
        else if (!patches.equals(other.patches))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
	    return "PatchDocument [blockSize=" + blockSize + ", matchCount=" + matchedBlocks.size()
	            + ", matchedBlocks=" + matchedBlocks + ", patches=" + patches
	            + "]";
    }
}
