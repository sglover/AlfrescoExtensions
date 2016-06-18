package org.alfresco.contentstore;

/**
 * 
 * @author sglover
 *
 */
public interface BlockMap extends Iterable<Long>
{
    long getGlobalBlockNum(int blockNum);
    void addBlockMapping(int contentBlock, long blockNum);
}
