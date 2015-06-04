/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.checksum;

/**
 * 
 * @author sglover
 *
 */
public class Patch
{
    private int lastMatchIndex;
    private byte[] buffer;

	public Patch(int lastMatchIndex, byte[] buffer)
    {
	    super();
	    this.lastMatchIndex = lastMatchIndex;
	    this.buffer = buffer;
    }

//	public void add(byte b)
//	{
//		buffer.put(b);
//	}

	public int getLastMatchIndex()
	{
		return lastMatchIndex;
	}

	public byte[] getBuffer()
	{
		return buffer;
	}

	@Override
    public String toString()
    {
		String s = new String(buffer);
	    return "Patch [lastMatchIndex=" + lastMatchIndex; //+ ", buffer=" + s
//	            + "]";
    }
}
