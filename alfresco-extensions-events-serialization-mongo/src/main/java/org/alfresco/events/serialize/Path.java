/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a node path.
 * 
 * @author steveglover
 *
 */
public class Path
{

	protected List<String> path;

	public Path(String path)
	{
		super();
		this.path = getPath(path);
	}

	public Path(List<String> path)
	{
		super();
		if(path == null)
		{
			throw new IllegalArgumentException("Path cannot be null");
		}
		this.path = path;
	}
	
	public Path(Path path)
	{
	    // make a copy
		if(path == null)
		{
			throw new IllegalArgumentException("Path cannot be null");
		}
	    List<String> arrayPath = path.getArrayPath();
	    this.path = new ArrayList<>(arrayPath);
	}
	
	public Path subfolder()
	{
	    Path subFolder = new Path(path.subList(1, path.size()));
	    return subFolder;
	}

	public void insert(int idx, List<String> newElements)
	{
		if(idx >= path.size())
		{
			throw new IllegalArgumentException("idx too large");
		}
		for(int i = newElements.size() - 1; i >= 0; i--)
		{
			String newElement = newElements.get(i);
			path.add(idx, newElement);
		}
	}

	public void change(int idx, String newElement)
	{
		if(idx > path.size())
		{
			throw new IllegalArgumentException("idx too large");
		}
		path.set(idx, newElement);
	}

	public void append(String newElement)
	{
		path.add(newElement);
	}

	public String get(int idx)
	{
		if(idx > path.size())
		{
			throw new IllegalArgumentException("idx too large");
		}
		return path.get(idx);
	}

	public int size()
	{
		return path.size();
	}

    protected List<String> getPath(String pathString)
    {
    	List<String> path = Collections.emptyList();

    	if(pathString != null)
    	{
    		path = Arrays.asList(pathString.substring(1).split("/"));
    	}

    	return path;
    }

	protected String joinPath(List<String> path)
	{
	    int numPathElements = path.size();
		StringBuilder sb = new StringBuilder("/");
		for(int i = 0; i < numPathElements; i++)
		{
		    String pathElement = path.get(i);
		    sb.append(pathElement);
		    if(i < numPathElements - 1)
		    {
		        sb.append("/");
		    }
		}
		return sb.toString();
	}

	public List<String> getArrayPath()
	{
		return path;
	}
	
	public String getPath()
	{
		return joinPath(path);
	}

	@Override
	public String toString()
	{
		return "Path [path=" + path + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		Path other = (Path) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}
