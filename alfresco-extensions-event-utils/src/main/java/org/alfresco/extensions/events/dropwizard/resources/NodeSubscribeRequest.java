/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.events.dropwizard.resources;

/**
 * 
 * @author sglover
 *
 */
public class NodeSubscribeRequest
{
	private String subscriberId;
	private String subscriptionId;
	private String path;

	public String getPath()
	{
		return path;
	}
	public void setPath(String path)
	{
		this.path = path;
	}
	public String getSubscriberId()
	{
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId)
	{
		this.subscriberId = subscriberId;
	}
	public String getSubscriptionId()
	{
		return subscriptionId;
	}
	public void setSubscriptionId(String subscriptionId)
	{
		this.subscriptionId = subscriptionId;
	}

	
}
