/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.extensions.ocr;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;

/**
 * 
 * @author sglover
 *
 */
public class OCR
{
    public String convert(String filename) throws Exception
    {
        TessBaseAPI api = new TessBaseAPI();
        BytePointer outText = null;
        PIX image = null;

        try
        {
            // Initialize tesseract-ocr with English, without specifying tessdata path
            if (api.Init(".", "ENG") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
    
            // Open input image with leptonica library
            image = pixRead(filename);
            api.SetImage(image);
            // Get OCR result
            outText = api.GetUTF8Text();
            String string = outText.getString();
            return string;
        }
        finally
        {
            // Destroy used object and release memory
            api.End();

            if(outText != null)
            {
                outText.deallocate();
            }

            if(image != null)
            {
                pixDestroy(image);
            }
        }
    }
}
