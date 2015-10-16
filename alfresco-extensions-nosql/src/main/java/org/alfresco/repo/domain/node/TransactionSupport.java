/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.domain.node;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.util.GUID;

/**
 * 
 * @author sglover
 *
 */
public class TransactionSupport
{
	private static ThreadLocal<String> txnId = new ThreadLocal<String>()
	{
	};
	
	private static List<TransactionListener> listeners = new LinkedList<>();

	public static String getTxnId()
	{
		String ret = txnId.get();
		if(ret == null)
		{
			ret = GUID.generate();
			txnId.set(ret);
		}
		return ret;
	}
	
	public static void addListener(TransactionListener listener)
	{
		listeners.add(listener);
	}

	public static void begin()
	{
		String id = GUID.generate();
		txnId.set(id);
	}

	public static void commit()
	{
		for(TransactionListener listener : listeners)
		{
			listener.onCommit(getTxnId());
		}
	}

	public static void rollback()
	{
		for(TransactionListener listener : listeners)
		{
			listener.onRollback(getTxnId());
		}
	}
}
