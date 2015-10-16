/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

/**
 * 
 * @author sglover
 *
 */
public class Range
{
	private long start;
	private long end;

	public static Range start(long start)
	{
		Range range = new Range();
		range.start = start;
		return range;
	}
	public Range end(long end)
	{
		this.end = end;
		return this;
	}
	public long getStart()
	{
		return start;
	}
	public long getEnd()
	{
		return end;
	}

	@Override
    public int hashCode()
    {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + (int) (end ^ (end >>> 32));
	    result = prime * result + (int) (start ^ (start >>> 32));
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
	    Range other = (Range) obj;
	    if (end != other.end)
		    return false;
	    if (start != other.start)
		    return false;
	    return true;
    }
	@Override
	public String toString()
	{
	    return "Range [start=" + start + ", end=" + end + "]";
	}
}
