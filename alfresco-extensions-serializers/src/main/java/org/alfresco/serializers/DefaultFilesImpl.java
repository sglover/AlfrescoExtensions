/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class DefaultFilesImpl implements Files
{
	public DefaultFilesImpl()
	{
	}

	@Override
    public DBObject createFile(long nodeId, long nodeVersion, String propertyName, byte[] bytes)
    {
		throw new UnsupportedOperationException();
    }
}
