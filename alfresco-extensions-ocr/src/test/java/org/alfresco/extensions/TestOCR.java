/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions;

import java.net.URL;

import org.alfresco.extensions.ocr.OCR;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class TestOCR
{
    @Test
    public void test1() throws Exception
    {
        OCR ocr = new OCR();
        URL url = getClass().getClassLoader().getResource("test.png");
        String filename = url.getFile();
        String str = ocr.convert(filename);
        System.out.println(str);
    }
}
