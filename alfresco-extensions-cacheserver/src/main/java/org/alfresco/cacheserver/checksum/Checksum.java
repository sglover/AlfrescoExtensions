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
public class Checksum
{
	private int blockIndex;
	private int hash;
	private int adler32;
	private String md5;

	public Checksum(int blockIndex, int hash, int adler32, String md5)
    {
        super();
        this.blockIndex = blockIndex;
        this.adler32 = adler32;
        this.md5 = md5;
        this.hash = hash;
    }

	
	public int getBlockIndex()
	{
		return blockIndex;
	}


	public int getHash()
	{
		return hash;
	}

	public void setHash(int hash)
	{
		this.hash = hash;
	}

	public void setAdler32(int adler32)
	{
		this.adler32 = adler32;
	}

	public void setMd5(String md5)
	{
		this.md5 = md5;
	}

	public int getAdler32()
	{
		return adler32;
	}
	public String getMd5()
	{
		return md5;
	}


	@Override
    public String toString()
    {
	    return "Checksum [blockIndex=" + blockIndex + ", hash=" + hash
	            + ", adler32=" + adler32 + ", md5=" + md5 + "]";
    }


}
