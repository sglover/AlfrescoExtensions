/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author sglover
 *
 */
public class SlowInputStream extends InputStream
{
    private InputStream delegate;
    private int delay;
    private int blockSize;
    private long counter = 0;

    public SlowInputStream(InputStream delegate, int blockSize, int delay)
    {
        this.delegate = delegate;
        this.delay = delay;
        this.blockSize = blockSize;
    }

    @Override
    public int read() throws IOException
    {
        if(counter++ % blockSize == 0)
        {
            try {
                Thread.sleep(delay);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return delegate.read();
    }

}
