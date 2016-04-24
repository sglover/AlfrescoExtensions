/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.checksum;

import java.util.List;

import org.sglover.alfrescoextensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public interface PatchDocument
{
    Node getNode();
    void addPatch(Patch patch);
    void addMatchedBlock(int matchedBlock);
    void setBlockSize(int blockSize);
    int getBlockSize();
    int getMatchCount();
    List<Integer> getMatchedBlocks();
    List<Patch> getPatches();
    void commit();
}
