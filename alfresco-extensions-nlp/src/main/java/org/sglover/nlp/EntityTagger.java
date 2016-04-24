/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.nlp;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author sglover
 *
 */
public interface EntityTagger
{
	Entities getEntities(String text) throws IOException;
	Entities getEntities(URL url) throws IOException;
	Entities getEntities(File file) throws IOException;

	void getEntities(String text, EntityTaggerCallback callback);
	void getEntities(URL url, EntityTaggerCallback callback);
	void getEntities(File file, EntityTaggerCallback callback);
}
