/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.entities.spark;

import org.apache.spark.Accumulable;
import org.apache.spark.api.java.function.VoidFunction;

/**
 * 
 * @author sglover
 *
 */
public class LinesAccumulableFunction implements VoidFunction<String>
{
    private static final long serialVersionUID = -6018327238356710468L;

    private Accumulable<String, String> linesAccumulator;

    public LinesAccumulableFunction(Accumulable<String, String> linesAccumulator)
    {
        this.linesAccumulator = linesAccumulator;
    }

    public void call(String s) throws Exception
    {
        linesAccumulator.add(s);
    }
}
