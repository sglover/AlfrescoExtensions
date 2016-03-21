/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * 
 * @author sglover
 *
 */
public interface ContentWriter extends ContentAccessor
{
    OutputStream getOutputStream() throws IOException;
    Writer getWriter() throws IOException;
    void writeStream(InputStream stream) throws IOException;
}
