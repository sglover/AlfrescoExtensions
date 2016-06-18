/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.nlp;

/**
 * 
 * @author sglover
 *
 */
public class EntityLocation
{
    private long beginOffset;
    private long endOffset;
    private double probability;
    private String context;

    public EntityLocation()
    {

    }

    public EntityLocation(long beginOffset, long endOffset, double probability, String context)
    {
        super();
        this.beginOffset = beginOffset;
        this.endOffset = endOffset;
        this.probability = probability;
        this.context = context;
    }

    public long getBeginOffset()
    {
        return beginOffset;
    }

    public long getEndOffset()
    {
        return endOffset;
    }

    public double getProbability()
    {
        return probability;
    }

    public String getContext()
    {
        return context;
    }

    @Override
    public String toString()
    {
        return "EntityLocation [beginOffset=" + beginOffset + ", endOffset=" + endOffset
                + ", probability=" + probability + ", context=" + context + "]";
    }

}
