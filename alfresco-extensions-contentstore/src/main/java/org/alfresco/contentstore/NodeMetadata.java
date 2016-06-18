package org.alfresco.contentstore;

import org.sglover.alfrescoextensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public class NodeMetadata
{
    private Node node;
    private BlockMap blockMap;

    public NodeMetadata(Node node)
    {
        super();
        this.node = node;
        this.blockMap = new DynamicBlockMap();
    }

    public NodeMetadata(Node node, int numBlocks)
    {
        super();
        this.node = node;
        this.blockMap = new FixedSizeBlockMap(numBlocks);
    }

    public NodeMetadata(Node node, FixedSizeBlockMap blockMap)
    {
        super();
        this.node = node;
        this.blockMap = blockMap;
    }

    public Node getNode()
    {
        return node;
    }

    public BlockMap getBlockmap()
    {
        return blockMap;
    }

}
