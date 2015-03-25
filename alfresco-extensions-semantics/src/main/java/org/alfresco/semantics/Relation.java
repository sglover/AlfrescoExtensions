/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.semantics;

/**
 * 
 * @author sglover
 *
 */
public class Relation
{
	private String fromId;
	private String toId;
	private double weight;
	
	public Relation(String fromId, String toId, double weight)
    {
	    super();
	    this.fromId = fromId;
	    this.toId = toId;
	    this.weight = weight;
    }
	public String getFromId()
	{
		return fromId;
	}
	public String getToId()
	{
		return toId;
	}
	public double getWeight()
	{
		return weight;
	}
	
	
}
