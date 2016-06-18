package org.alfresco.contentstore;

import java.util.Iterator;

/**
 * 
 * @author sglover
 *
 */
public class FixedSizeBlockMap implements BlockMap
{
    private long[] blockMap;

    public FixedSizeBlockMap(int numBlocks)
    {
        this.blockMap = new long[numBlocks];
    }

    public long getGlobalBlockNum(int blockNum)
    {
        return blockMap[blockNum];
    }

    public void addBlockMapping(int contentBlock, long blockNum)
    {
        blockMap[contentBlock] = blockNum;
    }

    @Override
    public Iterator<Long> iterator()
    {
        return new BlockMapIterator();
    }

    private class BlockMapIterator implements Iterator<Long>
    {
        private int i = 0;

        @Override
        public boolean hasNext()
        {
            return i < blockMap.length;
        }

        @Override
        public Long next()
        {
            return blockMap[i++];
        }
        
    }
}
