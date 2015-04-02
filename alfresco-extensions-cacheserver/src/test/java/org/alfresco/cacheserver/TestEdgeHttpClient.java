/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.io.InputStream;

import org.alfresco.cacheserver.http.CacheHttpClient;
import org.alfresco.cacheserver.http.HttpCallback;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class TestEdgeHttpClient
{
	@Test
	public void test1() throws Exception
	{
		CacheHttpClient client = new CacheHttpClient();
		HttpCallback callback = new HttpCallback()
		{
			
			@Override
			public void execute(InputStream in)
			{
				System.out.println("ok");
			}
		};
		client.getNodeById("localhost", 9199, "admin", "admin", "927753f6-229e-4f93-9d12-f70cb1f1fba4", "1.17", callback);
	}
}
