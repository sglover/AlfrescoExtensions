/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author sglover
 */
public interface Hasher
{
    String md5(ByteBuffer bytes, int start, int end) throws NoSuchAlgorithmException;
}
