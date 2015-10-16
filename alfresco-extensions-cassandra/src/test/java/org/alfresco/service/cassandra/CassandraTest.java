/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.service.cassandra;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class CassandraTest
{
	private CassandraDAO cassandraDAO;

	@Before
	public void init()
	{
		this.cassandraDAO = new CassandraDAO();
		this.cassandraDAO.setDropKeyspaceOnInit(true);
		this.cassandraDAO.init();
	}

	@Test
	public void test1() throws Exception
	{
		cassandraDAO.add("1", 10, "/Company Home/Sites/Site1/documentLibrary/file1.txt");
		cassandraDAO.add("1", 10, "/Company Home/Sites/Site1/documentLibrary/file2.txt");
		cassandraDAO.add("1", 10, "/Company Home/Sites/Site1/documentLibrary/file3.txt");
		cassandraDAO.add("1", 10, "/Company Home/Sites/Site1/documentLibrary/file4.txt");

		{
			List<String> events = cassandraDAO.getEvents("1", 10, "/Company Home/Sites/Site1");
			assertEquals(0, events.size());
		}

		{
			List<String> events = cassandraDAO.getEvents("1", 11, "/Company Home/Sites/Site1");
			assertEquals("1, /Company Home/Sites/Site1/documentLibrary/file1.txt", events.get(0));
			assertEquals("1, /Company Home/Sites/Site1/documentLibrary/file2.txt", events.get(1));
			assertEquals("1, /Company Home/Sites/Site1/documentLibrary/file3.txt", events.get(2));
			assertEquals("1, /Company Home/Sites/Site1/documentLibrary/file4.txt", events.get(3));
		}

		{
			List<String> events = cassandraDAO.getEvents("1", 11, "/Company Home/Sites/Site2");
			assertEquals(0, events.size());
		}
	}
}
