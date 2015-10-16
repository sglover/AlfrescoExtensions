/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.minhash;

import java.util.Set;

/**
 * 
 * @author sglover
 *
 * @param <T>
 */
public interface MinHash<T>
{
	double similarity(Set<T> set1, Set<T> set2);
}
