/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author sglover
 *
 * @param <K>
 * @param <V>
 */
public class MapBuilder<K, V>
{
	private Map<K, V> map;

	private MapBuilder()
	{
		this.map = new HashMap<>();
	}

	public static <K, V> MapBuilder<K, V> start()
	{
		return new MapBuilder<K, V>();
	}
	
	public MapBuilder<K, V> add(K key, V value)
	{
		map.put(key, value);
		return this;
	}
	
	public Map<K, V> get()
	{
		return map;
	}
}
