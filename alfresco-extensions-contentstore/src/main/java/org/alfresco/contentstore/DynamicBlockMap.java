package org.alfresco.contentstore;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author sglover
 *
 */
public class DynamicBlockMap implements BlockMap
{
    private Map<Integer, Long> blockMap;

    public DynamicBlockMap()
    {
        this.blockMap = new ConcurrentHashMap<>();
    }

    public long getGlobalBlockNum(int blockNum)
    {
        return blockMap.get(blockNum);
    }

    public void addBlockMapping(int contentBlock, long blockNum)
    {
        blockMap.put(contentBlock, blockNum);
    }

    @Override
    public Iterator<Long> iterator()
    {
        return blockMap.values().iterator();
    }
}
