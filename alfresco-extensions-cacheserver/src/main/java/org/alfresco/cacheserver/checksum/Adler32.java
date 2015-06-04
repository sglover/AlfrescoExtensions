/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.checksum;

import java.nio.ByteBuffer;

/**
 * 
 * @author sglover
 *
 */
public class Adler32
{
	private int a;
	private int b;
	private int checksum;
	private int hash;

	public Adler32(int a, int b, int checksum)
    {
        super();
        this.a = a;
        this.b = b;
        this.checksum = checksum;
        this.hash = hash16(checksum);
    }

	public int getA()
	{
		return a;
	}


	public int getB()
	{
		return b;
	}


	public int getChecksum()
	{
		return checksum;
	}


	public int getHash()
	{
		return hash;
	}


	public int hash16(int num)
	{
		return num % 65536;
	}

	public void rollingChecksum(int offset, int end, ByteBuffer data)
	{
//		byte[] data = new byte[buffer.capacity()];
//		buffer.get(data, 0, buffer.capacity());

		byte temp = data.get(offset - 1); //this is the first byte used in the previous iteration
		this.a = (this.a - temp + data.get(end)) % 65536;
		this.b = (this.b - ((end - offset + 1) * temp) + a) % 65536;
		this.checksum = (b << 16) | a;
	}

	@Override
    public String toString()
    {
        return "AdlerInfo [a=" + a + ", b=" + b + ", checksum=" + checksum
                + "]";
    }

}