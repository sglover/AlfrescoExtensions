/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.nlp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class ClasspathModelLoader implements ModelLoader
{
    private static final Log logger = LogFactory.getLog(ClasspathModelLoader.class);

    public InputStream load(String modelFilePath) throws IOException
    {
    	InputStream in = getClass().getClassLoader().getResourceAsStream(modelFilePath);
    	return in;
    }
}
