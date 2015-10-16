/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.domain.node;

/**
 * 
 * @author sglover
 *
 * @param <T>
 */
public class RetryingTransactionHelper<T>
{
	private int maxNumRetries = 5;

	private T doInTransationImpl(int i, RetryingTransactionCallback<T> callback)
	{
		try
		{
			return doInTransationImpl(i, callback);
		}
		catch(ConcurrentModificationException e)
		{
			if(i < maxNumRetries)
			{
				return doInTransationImpl(i + 1, callback);
			}
			else
			{
				throw e;
			}
		}
	}

	public T doInTransation(RetryingTransactionCallback<T> callback)
	{
		return doInTransationImpl(0, callback);
	}
}
