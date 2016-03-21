/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.checksum;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 
 * @author sglover
 *
 */
public class Patch
{
    private int lastMatchIndex;
    private int size;
    private byte[] buffer;
    private InputStream is;

	public Patch(int lastMatchIndex, int size, byte[] buffer)
    {
	    super();
	    this.lastMatchIndex = lastMatchIndex;
	    this.size = size;
	    this.buffer = buffer;
    }

	public Patch(int lastMatchIndex, int size, InputStream is) throws IOException
    {
	    super();
	    this.lastMatchIndex = lastMatchIndex;
	    this.size = size;
	    this.is = is;
    }

	public int getLastMatchIndex()
	{
		return lastMatchIndex;
	}

	public int getSize()
	{
		return size;
	}

	public InputStream getStream()
	{
		if(is != null)
		{
			return is;
		}
		else
		{
			return new ByteArrayInputStream(buffer);
		}
	}

	public byte[] getBuffer()
    {
        return buffer;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(buffer);
        result = prime * result + lastMatchIndex;
        result = prime * result + size;
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
        Patch other = (Patch) obj;
        if (!Arrays.equals(buffer, other.buffer))
            return false;
        if (lastMatchIndex != other.lastMatchIndex)
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    private String printBuffer()
    {
        StringBuilder sb = new StringBuilder("[");
        if(buffer.length > 0)
        {
            sb.append(buffer[0]);
        }
        if(buffer.length > 1)
        {
            sb.append(",");
            sb.append(buffer[1]);
        }
        if(buffer.length > 2)
        {
            sb.append("...");
            sb.append(buffer[buffer.length - 2]);
        }
        if(buffer.length > 3)
        {
            sb.append(",");
            sb.append(buffer[buffer.length - 1]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString()
    {
	    return "Patch [lastMatchIndex=" + lastMatchIndex + ", size=" + size
	            + ", buffer=" + printBuffer() + ", is=" + is + "]";
    }
}
