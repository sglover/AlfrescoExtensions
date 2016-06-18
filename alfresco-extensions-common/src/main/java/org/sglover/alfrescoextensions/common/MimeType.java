/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public enum MimeType
{
    TEST_SOURCE("test_source", "test_source"),
    TEST_TARGET("test_target", "test_target"),

    OCTET_STREAM("application/octet-stream", "application/octet-stream"),

    PDF("application/pdf", "pdf"),
    FLASH("application/x-shockwave-flash", "swf"),

    JSON("application/json", "json"),
    TEXT("text/plain", "txt"),
    TEXT_CSV("text/csv", "csv"),
    RTF("application/rtf", "rtf"),
    MARKDOWN("text/x-markdown", "md"),
    MEDIAWIKI("text/mediawiki", "mw"),
    RICHTEXT("text/richtext", "rtx"),
    RSS("application/rss+xml", "rss"),
    SGML("text/sgml", "sgml"),
    TSV("text/tab-separated-values", "tsv"),
    
    XML("text/xml", "xml"),
    XHTML("application/xhtml+xml", "xhtml"),
    HTML("text/html", "html"),
    CSS("text/css", "css"),
    
    ICS("text/calendar", "ics"),
    
    WORD("application/msword", "doc"),
    WORDPERFECT("application/wordperfect", "wpd"), 
    
    DOCM("application/vnd.ms-word.document.macroenabled.12", "docm"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    DOTM("application/vnd.ms-word.template.macroenabled.12", "dotm"),
    DOTX("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx"),
    POTM("application/vnd.ms-powerpoint.template.macroenabled.12", "potm"),
    POTX("application/vnd.openxmlformats-officedocument.presentationml.template", "potx"),
    MPP("application/vnd.ms-project", "mpp"),
    PPAM("application/vnd.ms-powerpoint.addin.macroenabled.12", "ppam"),
    PPSM("application/vnd.ms-powerpoint.slideshow.macroenabled.12", "ppsm"),
    PPSX("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx"),
    PPT("application/vnd.ms-powerpoint", "ppt"),
    PPTM("application/vnd.ms-powerpoint.presentation.macroenabled.12", "pptm"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
    SLDM("application/vnd.ms-powerpoint.slide.macroenabled.12", "sldm"),
    SLDX("application/vnd.openxmlformats-officedocument.presentationml.slide", "sldx"),
    XLAM("application/vnd.ms-excel.addin.macroenabled.12", "xlam"),
    XLS("application/vnd.ms-excel", "xls"),
    XLSB("application/vnd.ms-excel.sheet.binary.macroenabled.12", "xlsb"),
    XLSM("application/vnd.ms-excel.sheet.macroenabled.12", "xlsm"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    XLTM("application/vnd.ms-excel.template.macroenabled.12", "xltm"),
    XLTX("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx"),
    
    STC("application/vnd.sun.xml.calc.template", "stc"),
    STI("application/vnd.sun.xml.impress.template", "sti"),
    STW("application/vnd.sun.xml.writer.template", "stw"),
    SXC("application/vnd.sun.xml.calc", "sxc"),
    SXD("application/vnd.sun.xml.draw", "xsd"),
    SXI("application/vnd.sun.xml.impress", "sxi"),
    SXW("application/vnd.sun.xml.writer", "sxw"),
    
    ODB("application/vnd.oasis.opendocument.database", "odb"),
    ODC("application/vnd.oasis.opendocument.chart", "odc"),
    ODF("application/vnd.oasis.opendocument.formula", "odf"),
    ODG("application/vnd.oasis.opendocument.graphics", "odg"),
    ODI("application/vnd.oasis.opendocument.image", "odi"),
    ODM("application/vnd.oasis.opendocument.text-master", "odm"),
    ODP("application/vnd.oasis.opendocument.presentation", "odp"),
    ODS("application/vnd.oasis.opendocument.spreadsheet", "ods"),
    ODT("application/vnd.oasis.opendocument.text", "odt"),
    OTG("application/vnd.oasis.opendocument.graphics-template", "otg"),
    OTH("application/vnd.oasis.opendocument.text-web", "oth"),
    OTP("application/vnd.oasis.opendocument.presentation-template", "otp"),
    OTS("application/vnd.oasis.opendocument.spreadsheet-template", "ots"),
    OTT("application/vnd.oasis.opendocument.text-template", "ott"),

    VSD("application/vnd.visio", "vsd"),
    
    KEYNOTE("application/vnd.apple.keynote", "key"),
    NUMBERS("application/vnd.apple.numbers", "numbers"),
    PAGES("application/vnd.apple.pages", "pages"),
    
    PSD("image/vnd.adobe.photoshop", "psd"),
    
    BMP("image/bmp", "bmp"),
    GIF("image/gif", "gif"),
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    TIFF("image/tiff", "tiff"),

    OUTLOOK_MSG("application/vnd.ms-outlook", "msg"),
    EML("message/rfc822", "eml"),

    CPIO("application/x-cpio", "cpio"),
    TAR("application/x-tar", "tar"),
    ZIP("application/zip", "zip"),
    GZIP("application/x-gzip", "gzip"),
    
    JS("application/x-javascript", "js"),
    
    XPS("application/vnd.ms-xpsdocument", "xps");

    private static Set<MimeType> TEXT_MIMETYPES = new HashSet<>();
    static {
        TEXT_MIMETYPES.add(JSON);
        TEXT_MIMETYPES.add(TEXT);
        TEXT_MIMETYPES.add(TEXT_CSV);
        TEXT_MIMETYPES.add(SGML);
        TEXT_MIMETYPES.add(TSV);
        TEXT_MIMETYPES.add(XML);
        TEXT_MIMETYPES.add(XHTML);
        TEXT_MIMETYPES.add(HTML);
    }

    public boolean isText()
    {
        return TEXT_MIMETYPES.contains(this);
    }

    private String mimetype;
    private String ext;

    MimeType(String mimetype, String ext)
    {
        this.mimetype = mimetype;
        this.ext = ext;
    }
    
    public String getMimetype()
    {
        return mimetype;
    }
    
    public String getExt()
    {
        return ext;
    }
    
    @Override
    public String toString()
    {
        return mimetype + "(" + ext + ")";
    }
    
    public static class MimeTypesMap
    {
        private Map<String, MimeType> mimetypes = new HashMap<>();
        private Map<String, MimeType> exts = new HashMap<>();
        private Map<String, MimeType> names = new HashMap<>();
        
        public MimeTypesMap()
        {
            // setup label map
            EnumSet<MimeType> enumSet = EnumSet.allOf(MimeType.class);
            Iterator<MimeType> iter = enumSet.iterator();
            while(iter.hasNext())
            {
                MimeType m = iter.next();
                
                String mimetype = m.getMimetype().toLowerCase();
                if (mimetypes.containsKey(mimetype))
                {
                    throw new IllegalStateException("Mimetype " + mimetype + " from " + m.name() + " already defined");
                }
                mimetypes.put(mimetype,  m);
                
                String ext = m.getExt().toLowerCase();
                if (exts.containsKey(ext))
                {
                    throw new IllegalStateException("Extension " + ext + " from " + m.name() + " already defined");
                }
                exts.put(ext, m);
                
                names.put(m.name().toLowerCase(), m);
            }
        }
        
        
        public MimeType get(String label)
        {
            int parenIdx = label.indexOf("(");
            if (parenIdx != -1)
            {
                label = label.substring(0, parenIdx);
            }
            String lowerLabel = label.toLowerCase();
            MimeType m = getByMimetype(lowerLabel);
            if (m == null)
            {
                m = getByName(lowerLabel);
                if (m == null)
                {
                    m = getByExtension(lowerLabel);
                }
            }
            return m;
        }
        
        public MimeType getByMimetype(String mimetype)
        {
            return mimetypes.get(mimetype);
        }
        
        public MimeType getByExtension(String ext)
        {
            return exts.get(ext);
        }
        
        public MimeType getByName(String name)
        {
            return names.get(name);
        }
    }
    
    public static MimeTypesMap INSTANCES = new MimeTypesMap();
}
