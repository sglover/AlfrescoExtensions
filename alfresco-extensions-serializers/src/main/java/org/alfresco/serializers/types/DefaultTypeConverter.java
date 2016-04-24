/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
//import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.EntityRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;

/**
 * Support for generic conversion between types.
 * 
 * Additional conversions may be added. Basic inter-operability supported.
 * 
 * Direct conversion and two stage conversions via Number are supported. We do
 * not support conversion by any route at the moment
 * 
 * TODO: Add support for Path 
 * 
 * TODO: Add support for lucene
 * 
 * TODO: Add suport to check of a type is convertable
 * 
 * TODO: Support for dynamically managing conversions
 * 
 * @author andyh
 * 
 */
public class DefaultTypeConverter extends TypeConverter
{
    private static Log logger = LogFactory.getLog(DefaultTypeConverter.class);

    /**
     * Default Type Converter
     */
    public static TypeConverter INSTANCE = new DefaultTypeConverter();

    @SuppressWarnings("rawtypes")
    private DefaultTypeConverter()
    {
        //
        // From string
        //
        addConverter(String.class, Class.class, new TypeConverter.Converter<String, Class>()
                {
                    public Class convert(String source)
                    {
                        try
                        {
                            return Class.forName(source);
                        }
                        catch (ClassNotFoundException e)
                        {
                            throw new TypeConversionException("Failed to convert string to class: " + source, e);
                        }
                    }
                });
        addConverter(String.class, Boolean.class, new TypeConverter.Converter<String, Boolean>()
        {
            public Boolean convert(String source)
            {
                return Boolean.valueOf(source);
            }
        });
        addConverter(String.class, Character.class, new TypeConverter.Converter<String, Character>()
        {
            public Character convert(String source)
            {
                if ((source == null) || (source.length() == 0))
                {
                    return null;
                }
                return Character.valueOf(source.charAt(0));
            }
        });
        addConverter(String.class, Number.class, new TypeConverter.Converter<String, Number>()
        {
            public Number convert(String source)
            {
                try
                {
                    return DecimalFormat.getNumberInstance().parse(source);
                }
                catch (ParseException e)
                {
                    throw new TypeConversionException("Failed to parse number " + source, e);
                }
            }
        });
        addConverter(String.class, Byte.class, new TypeConverter.Converter<String, Byte>()
        {
            public Byte convert(String source)
            {
                return Byte.valueOf(source);
            }
        });
        addConverter(String.class, Short.class, new TypeConverter.Converter<String, Short>()
        {
            public Short convert(String source)
            {
                return Short.valueOf(source);
            }
        });
        addConverter(String.class, Integer.class, new TypeConverter.Converter<String, Integer>()
        {
            public Integer convert(String source)
            {
                return Integer.valueOf(source);
            }
        });
        addConverter(String.class, Long.class, new TypeConverter.Converter<String, Long>()
        {
            public Long convert(String source)
            {
                return Long.valueOf(source);
            }
        });
        addConverter(String.class, Float.class, new TypeConverter.Converter<String, Float>()
        {
            public Float convert(String source)
            {
                return Float.valueOf(source);
            }
        });
        addConverter(String.class, Double.class, new TypeConverter.Converter<String, Double>()
        {
            public Double convert(String source)
            {
                return Double.valueOf(source);
            }
        });
        addConverter(String.class, BigInteger.class, new TypeConverter.Converter<String, BigInteger>()
        {
            public BigInteger convert(String source)
            {
                return new BigInteger(source);
            }
        });
        addConverter(String.class, BigDecimal.class, new TypeConverter.Converter<String, BigDecimal>()
        {
            public BigDecimal convert(String source)
            {
                return new BigDecimal(source);
            }
        });
        addConverter(BasicDBObject.class, BigDecimal.class, new TypeConverter.Converter<BasicDBObject, BigDecimal>()
        {
            public BigDecimal convert(BasicDBObject source)
            {
            	String type = (String)source.get("t");
            	if(type.equals("FIXED_POINT"))
            	{
	            	String number = (String)source.get("n");
	            	Integer precision = (Integer)source.get("p");
	            	MathContext ctx = new MathContext(precision);
	                return new BigDecimal(number, ctx);
            	}
            	else
            	{
                    throw new IllegalArgumentException("Invalid source object for conversion "
                            + source 
                            + ", expected a Fixed Decimal object");
            	}
            }
        });
        addConverter(String.class, Date.class, new TypeConverter.Converter<String, Date>()
        {
            public Date convert(String source)
            {
                try
                {
                    Date date = ISO8601DateFormat.parse(source);
                    return date;
                }
                catch (PlatformRuntimeException e)
                {
                    throw new TypeConversionException("Failed to convert date " + source + " to string", e);
                }
                catch (AlfrescoRuntimeException e)
                {
                    throw new TypeConversionException("Failed to convert date " + source + " to string", e);
                }
            }
        });
        addConverter(String.class, Duration.class, new TypeConverter.Converter<String, Duration>()
        {
            public Duration convert(String source)
            {
                return new Duration(source);
            }
        });
        addConverter(String.class, QName.class, new TypeConverter.Converter<String, QName>()
        {
            public QName convert(String source)
            {
                return QName.createQName(source);
            }
        });
        addConverter(BasicDBObject.class, QName.class, new TypeConverter.Converter<BasicDBObject, QName>()
        {
            public QName convert(BasicDBObject source)
            {
                String type = (String)source.get("t");
                if(type.equals("QNAME"))
                {
                    String qname = (String)source.get("v");
                    return QName.createQName(qname);
                }
                else
                {
                    throw new IllegalArgumentException();
                }
            }
        });
        addConverter(String.class, ContentData.class, new TypeConverter.Converter<String, ContentData>()
        {
            public ContentData convert(String source)
            {
                return ContentData.createContentProperty(source);
            }
        });
        addConverter(String.class, NodeRef.class, new TypeConverter.Converter<String, NodeRef>()
        {
            public NodeRef convert(String source)
            {
                return new NodeRef(source);
            }
        });
        addConverter(BasicDBObject.class, NodeRef.class, new TypeConverter.Converter<BasicDBObject, NodeRef>()
        {
            public NodeRef convert(BasicDBObject source)
            {
                String type = (String)source.get("t");
                if(!type.equals("NODEREF"))
                {
                    throw new IllegalArgumentException("Invalid source object for conversion "
                            + source 
                            + ", expected a NodeRef object");
                }
                String protocol = (String)source.get("p");
                String storeId = (String)source.get("s");
                String id = (String)source.get("id");
                NodeRef nodeRef = new NodeRef(new StoreRef(protocol, storeId), id);
                return nodeRef;
            }
        });
        addConverter(String.class, StoreRef.class, new TypeConverter.Converter<String, StoreRef>()
        {
            public StoreRef convert(String source)
            {
                return new StoreRef(source);
            }
        });
        addConverter(BasicDBObject.class, StoreRef.class, new TypeConverter.Converter<BasicDBObject, StoreRef>()
        {
            public StoreRef convert(BasicDBObject source)
            {
                String type = (String)source.get("t");
                if(!type.equals("STOREREF"))
                {
                    throw new IllegalArgumentException("Invalid source object for conversion "
                            + source 
                            + ", expected a StoreRef object");
                }
                String protocol = (String)source.get("p");
                String storeId = (String)source.get("s");
                return new StoreRef(protocol, storeId);
            }
        });
        addConverter(String.class, ChildAssociationRef.class, new TypeConverter.Converter<String, ChildAssociationRef>()
        {
            public ChildAssociationRef convert(String source)
            {
                return new ChildAssociationRef(source);
            }
        });
        addConverter(String.class, AssociationRef.class, new TypeConverter.Converter<String, AssociationRef>()
        {
            public AssociationRef convert(String source)
            {
                return new AssociationRef(source);
            }
        });
        addConverter(String.class, InputStream.class, new TypeConverter.Converter<String, InputStream>()
        {
            public InputStream convert(String source)
            {
                try
                {
                    return new ByteArrayInputStream(source.getBytes("UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new TypeConversionException("Encoding not supported", e);
                }
            }
        });
        addConverter(String.class, MLText.class, new TypeConverter.Converter<String, MLText>()
        {
            public MLText convert(String source)
            {
                return new MLText(source);
            }
        });
        addConverter(BasicDBObject.class, MLText.class, new TypeConverter.Converter<BasicDBObject, MLText>()
        {
            public MLText convert(BasicDBObject source)
            {
                String type = (String)source.get("t");
                if(!type.equals("MLTEXT"))
                {
                    throw new IllegalArgumentException("Invalid source object for conversion "
                            + source 
                            + ", expected a NodeRef object");
                }

                MLText mlText = new MLText();
                for(String languageTag : source.keySet())
                {
                    String text = (String)source.get(languageTag);
                    Locale locale = Locale.forLanguageTag(languageTag);
                    mlText.put(locale, text);
                }

                return mlText;
            }
        });
//        addConverter(BasicDBObject.class, ContentDataWithId.class, new TypeConverter.Converter<BasicDBObject, ContentDataWithId>()
//        {
//            public ContentDataWithId convert(BasicDBObject source)
//            {
//                String type = (String)source.get("t");
//                if(!type.equals("CONTENT_DATA_ID"))
//                {
//                    throw new IllegalArgumentException("Invalid source object for conversion "
//                            + source 
//                            + ", expected a ContentDataWithId object");
//                }
//                String contentUrl = (String)source.get("u");
//                String mimeType = (String)source.get("m");
//                Long size = (Long)source.get("s");
//                String encoding = (String)source.get("e");
//                String languageTag = (String)source.get("l");
//                Long id = (Long)source.get("id");
//                Locale locale = Locale.forLanguageTag(languageTag);
//
//                ContentData contentData = new ContentData(contentUrl, mimeType, size, encoding, locale);
//                ContentDataWithId contentDataWithId = new ContentDataWithId(contentData, id);
//                return contentDataWithId;
//            }
//        });
        addConverter(BasicDBObject.class, ContentData.class, new TypeConverter.Converter<BasicDBObject, ContentData>()
        {
            public ContentData convert(BasicDBObject source)
            {
                ContentData contentData = null;

                String type = (String)source.get("t");
                if(type.equals("CONTENT"))
                {
                    String contentUrl = (String)source.get("u");
                    String mimeType = (String)source.get("m");
                    Long size = (Long)source.get("s");
                    String encoding = (String)source.get("e");
                    String languageTag = (String)source.get("l");
                    Locale locale = Locale.forLanguageTag(languageTag);
                    contentData = new ContentData(contentUrl, mimeType, size, encoding, locale);
                }
                else if(type.equals("CONTENT_DATA_ID"))
                {
                    String contentUrl = (String)source.get("u");
                    String mimeType = (String)source.get("m");
                    Long size = (Long)source.get("s");
                    String encoding = (String)source.get("e");
                    String languageTag = (String)source.get("l");
                    Locale locale = Locale.forLanguageTag(languageTag);
                    contentData = new ContentData(contentUrl, mimeType, size, encoding, locale);
                }
                else
                {
                    throw new IllegalArgumentException("Invalid source object for conversion "
                            + source 
                            + ", expected a ContentData object");
                }

                return contentData;
            }
        });
        addConverter(BasicDBObject.class, String.class, new TypeConverter.Converter<BasicDBObject, String>()
        {
            public String convert(BasicDBObject source)
            {
                // TODO distinguish between different BasicDBObject representations e.g. for MLText, ...

                Set<String> languageTags = source.keySet();
                if(languageTags.size() == 0)
                {
                    throw new IllegalArgumentException("Persisted MLText is invalid " + source);
                }
                else if(languageTags.size() > 1)
                {
                    // TODO
                    logger.warn("Persisted MLText has more than 1 locale " + source);
                }

                String languageTag = languageTags.iterator().next();
                String text = source.getString(languageTag);
                return text;
            }
        });
        addConverter(String.class, Locale.class, new TypeConverter.Converter<String, Locale>()
        {
            public Locale convert(String source)
            {
                return I18NUtil.parseLocale(source);
            }
        });
        addConverter(String.class, Period.class, new TypeConverter.Converter<String, Period>()
        {
            public Period convert(String source)
            {
                return new Period(source);
            }
        });
        addConverter(String.class, VersionNumber.class, new TypeConverter.Converter<String, VersionNumber>()
        {
            public VersionNumber convert(String source)
            {
                return new VersionNumber(source);
            }
        });

        //
        // From Locale
        //
        addConverter(Locale.class, String.class, new TypeConverter.Converter<Locale, String>()
        {
            public String convert(Locale source)
            {
                String localeStr = source.toString();
                if (localeStr.length() < 6)
                {
                    localeStr += "_";
                }
                return localeStr;
            }
        });

        
        //
        // From VersionNumber
        //
        addConverter(VersionNumber.class, String.class, new TypeConverter.Converter<VersionNumber, String>()
        {
            public String convert(VersionNumber source)
            {
                return source.toString();
            }
        });

        
        //
        // From MLText
        //
        addConverter(MLText.class, String.class, new TypeConverter.Converter<MLText, String>()
        {
            public String convert(MLText source)
            {
                return source.getDefaultValue();
            }
        });

        addConverter(MLText.class, BasicDBObject.class, new TypeConverter.Converter<MLText, BasicDBObject>()
        {
            public BasicDBObject convert(MLText source)
            {
                BasicDBObject dbObject = new BasicDBObject("t", "MLTEXT");
                for(Map.Entry<Locale, String> entry : source.entrySet())
                {
                    dbObject.put(entry.getKey().toLanguageTag(), entry.getValue());
                }
                return dbObject;
            }
        });

//        addConverter(ContentDataWithId.class, BasicDBObject.class, new TypeConverter.Converter<ContentDataWithId, BasicDBObject>()
//        {
//            public BasicDBObject convert(ContentDataWithId source)
//            {
//                BasicDBObject dbObject = new BasicDBObject("t", "CONTENT_DATA_ID");
//
//                String contentUrl = source.getContentUrl();
//                Long id = source.getId();
//                String languageTag = source.getLocale().toLanguageTag();
//                String encoding = source.getEncoding();
//                long size = source.getSize();
//                String mimeType = source.getMimetype();
//
//                dbObject.put("u", contentUrl);
//                dbObject.put("m", mimeType);
//                dbObject.put("s", size);
//                dbObject.put("e", encoding);
//                dbObject.put("l", languageTag);
//                dbObject.put("id", id);
//                return dbObject;
//            }
//        });

        addConverter(ContentData.class, BasicDBObject.class, new TypeConverter.Converter<ContentData, BasicDBObject>()
        {
            public BasicDBObject convert(ContentData source)
            {
                BasicDBObject dbObject = new BasicDBObject("t", "CONTENT");

                String contentUrl = source.getContentUrl();
                String languageTag = source.getLocale().toLanguageTag();
                String encoding = source.getEncoding();
                long size = source.getSize();
                String mimeType = source.getMimetype();

                dbObject.put("u", contentUrl);
                dbObject.put("m", mimeType);
                dbObject.put("s", size);
                dbObject.put("e", encoding);
                dbObject.put("l", languageTag);
                return dbObject;
            }
        });

        //
        // From enum
        //
        addConverter(Enum.class, String.class, new TypeConverter.Converter<Enum, String>()
        {
            public String convert(Enum source)
            {
                return source.toString();
            }
        });

        // From Period
        addConverter(Period.class, String.class, new TypeConverter.Converter<Period, String>()
        {
            public String convert(Period source)
            {
                return source.toString();
            }
        });
        
        // From Class
        addConverter(Class.class, String.class, new TypeConverter.Converter<Class, String>()
        {
            public String convert(Class source)
            {
                return source.getName();
            }
        });

        //
        // Number to Subtypes and Date
        //
        addConverter(Number.class, Boolean.class, new TypeConverter.Converter<Number, Boolean>()
        {
            public Boolean convert(Number source)
            {
                return new Boolean(source.longValue() > 0);
            }
        });
        addConverter(Number.class, Byte.class, new TypeConverter.Converter<Number, Byte>()
        {
            public Byte convert(Number source)
            {
                return Byte.valueOf(source.byteValue());
            }
        });
        addConverter(Number.class, Short.class, new TypeConverter.Converter<Number, Short>()
        {
            public Short convert(Number source)
            {
                return Short.valueOf(source.shortValue());
            }
        });
        addConverter(Number.class, Integer.class, new TypeConverter.Converter<Number, Integer>()
        {
            public Integer convert(Number source)
            {
                return Integer.valueOf(source.intValue());
            }
        });
        addConverter(Number.class, Long.class, new TypeConverter.Converter<Number, Long>()
        {
            public Long convert(Number source)
            {
                return Long.valueOf(source.longValue());
            }
        });
        addConverter(Number.class, Float.class, new TypeConverter.Converter<Number, Float>()
        {
            public Float convert(Number source)
            {
                return Float.valueOf(source.floatValue());
            }
        });
        addConverter(Number.class, Double.class, new TypeConverter.Converter<Number, Double>()
        {
            public Double convert(Number source)
            {
                return Double.valueOf(source.doubleValue());
            }
        });
        addConverter(Number.class, Date.class, new TypeConverter.Converter<Number, Date>()
        {
            public Date convert(Number source)
            {
                return new Date(source.longValue());
            }
        });
        addConverter(Number.class, String.class, new TypeConverter.Converter<Number, String>()
        {
            public String convert(Number source)
            {
                return source.toString();
            }
        });
        addConverter(Number.class, BigInteger.class, new TypeConverter.Converter<Number, BigInteger>()
        {
            public BigInteger convert(Number source)
            {
                if (source instanceof BigDecimal)
                {
                    return ((BigDecimal) source).toBigInteger();
                }
                else
                {
                    return BigInteger.valueOf(source.longValue());
                }
            }
        });
        addConverter(Number.class, BigDecimal.class, new TypeConverter.Converter<Number, BigDecimal>()
        {
            public BigDecimal convert(Number source)
            {
                if (source instanceof BigInteger)
                {
                    return new BigDecimal((BigInteger) source);
                }
                else if(source instanceof Double) 
                {
                    return BigDecimal.valueOf((Double) source);
                }
                else if(source instanceof Float) 
                {
                	Float val = (Float)source;
                	if(val.isInfinite())
            		{
                		// What else can we do here?  this is 3.4 E 38 so is fairly big
            			return new BigDecimal(Float.MAX_VALUE);   			
            		}
                    return BigDecimal.valueOf((Float) source);
                }
                else 
                {
                    return BigDecimal.valueOf(source.longValue());
                }
            }
        });
        addDynamicTwoStageConverter(Number.class, String.class, InputStream.class);
        
        //
        // Date, Timestamp ->
        //
        addConverter(Timestamp.class, Date.class, new TypeConverter.Converter<Timestamp, Date>()
        {
            public Date convert(Timestamp source)
            {
                return new Date(source.getTime());
            }
        });
        addConverter(Date.class, Number.class, new TypeConverter.Converter<Date, Number>()
        {
            public Number convert(Date source)
            {
                return Long.valueOf(source.getTime());
            }
        });
        addConverter(Date.class, String.class, new TypeConverter.Converter<Date, String>()
        {
            public String convert(Date source)
            {
                try
                {
                    return ISO8601DateFormat.format(source);
                }
                catch (PlatformRuntimeException e)
                {
                    throw new TypeConversionException("Failed to convert date " + source + " to string", e);
                }
            }
        });
        addConverter(Date.class, Calendar.class, new TypeConverter.Converter<Date, Calendar>()
        {
            public Calendar convert(Date source)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(source);
                return calendar;
            }
        });
        
        addConverter(Date.class, GregorianCalendar.class, new TypeConverter.Converter<Date, GregorianCalendar>()
        {
            public GregorianCalendar convert(Date source)
            {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(source);
                return calendar;
            }
        });
        addDynamicTwoStageConverter(Date.class, String.class, InputStream.class);

        //
        // Boolean ->
        //
        final Long LONG_FALSE = new Long(0L);
        final Long LONG_TRUE = new Long(1L);
        addConverter(Boolean.class, Long.class, new TypeConverter.Converter<Boolean, Long>()
                {
                    public Long convert(Boolean source)
                    {
                        return source.booleanValue() ? LONG_TRUE : LONG_FALSE;
                    }
                });
        addConverter(Boolean.class, String.class, new TypeConverter.Converter<Boolean, String>()
        {
            public String convert(Boolean source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Boolean.class, String.class, InputStream.class);

        //
        // Character ->
        //
        addConverter(Character.class, String.class, new TypeConverter.Converter<Character, String>()
        {
            public String convert(Character source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Character.class, String.class, InputStream.class);

        //
        // Duration ->
        //
        addConverter(Duration.class, String.class, new TypeConverter.Converter<Duration, String>()
        {
            public String convert(Duration source)
            {
                return source.toString();
            }

        });
        addDynamicTwoStageConverter(Duration.class, String.class, InputStream.class);

        //
        // Byte
        //
        addConverter(Byte.class, String.class, new TypeConverter.Converter<Byte, String>()
        {
            public String convert(Byte source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Byte.class, String.class, InputStream.class);
        
        //
        // Short
        //
        addConverter(Short.class, String.class, new TypeConverter.Converter<Short, String>()
        {
            public String convert(Short source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Short.class, String.class, InputStream.class);

        //
        // Integer
        //
        addConverter(Integer.class, String.class, new TypeConverter.Converter<Integer, String>()
        {
            public String convert(Integer source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Integer.class, String.class, InputStream.class);
        
        //
        // Long
        //
        addConverter(Long.class, String.class, new TypeConverter.Converter<Long, String>()
        {
            public String convert(Long source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Long.class, String.class, InputStream.class);

        //
        // Float
        //
        addConverter(Float.class, String.class, new TypeConverter.Converter<Float, String>()
        {
            public String convert(Float source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Float.class, String.class, InputStream.class);

        //
        // Double
        //
        addConverter(Double.class, String.class, new TypeConverter.Converter<Double, String>()
        {
            public String convert(Double source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Double.class, String.class, InputStream.class);

        //
        // BigInteger
        //
        addConverter(BigInteger.class, String.class, new TypeConverter.Converter<BigInteger, String>()
        {
            public String convert(BigInteger source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(BigInteger.class, String.class, InputStream.class);

        //
        // Calendar
        //
        addConverter(Calendar.class, Date.class, new TypeConverter.Converter<Calendar, Date>()
        {
            public Date convert(Calendar source)
            {
                return source.getTime();
            }
        });
        addConverter(Calendar.class, String.class, new TypeConverter.Converter<Calendar, String>()
        {
            public String convert(Calendar source)
            {
                try
                {
                    return ISO8601DateFormat.format(source.getTime());
                }
                catch (PlatformRuntimeException e)
                {
                    throw new TypeConversionException("Failed to convert date " + source + " to string", e);
                }
            }
        });
        
        //
        // BigDecimal
        //
        addConverter(BigDecimal.class, BasicDBObject.class, new TypeConverter.Converter<BigDecimal, BasicDBObject>()
        {
            public BasicDBObject convert(BigDecimal source)
            {
            	String number = source.toPlainString();
            	int precision = source.precision();
            	BasicDBObject dbObject = new BasicDBObject();
            	dbObject.put("t", "FIXED_POINT");
            	dbObject.put("n", number);
            	dbObject.put("p", precision);
                return dbObject;
            }
        });
        addConverter(BigDecimal.class, String.class, new TypeConverter.Converter<BigDecimal, String>()
        {
            public String convert(BigDecimal source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(BigDecimal.class, String.class, InputStream.class);

        //
        // QName
        //
        addConverter(QName.class, String.class, new TypeConverter.Converter<QName, String>()
        {
            public String convert(QName source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(QName.class, String.class, InputStream.class);

        //
        // EntityRef (NodeRef, ChildAssociationRef, NodeAssociationRef)
        //
        addConverter(EntityRef.class, String.class, new TypeConverter.Converter<EntityRef, String>()
        {
            public String convert(EntityRef source)
            {
                return source.toString();
            }
        });
        addConverter(EntityRef.class, BasicDBObject.class, new TypeConverter.Converter<EntityRef, BasicDBObject>()
        {
            public BasicDBObject convert(EntityRef source)
            {
                BasicDBObject ret = null;

                if(source instanceof NodeRef)
                {
                    NodeRef nodeRef = (NodeRef)source;

                    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("t", "NODEREF");
                    builder.add("p", nodeRef.getStoreRef().getProtocol());
                    builder.add("s", nodeRef.getStoreRef().getIdentifier());
                    builder.add("id", nodeRef.getId());
                    ret = (BasicDBObject)builder.get();
                }
                else if(source instanceof StoreRef)
                {
                    StoreRef storeRef = (StoreRef)source;

                    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("t", "STOREREF");
                    builder.add("p", storeRef.getProtocol());
                    builder.add("s", storeRef.getIdentifier());
                    ret = (BasicDBObject)builder.get();
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                return ret;
            }
        });
        addDynamicTwoStageConverter(EntityRef.class, String.class, InputStream.class);

        //
        // ContentData
        //
        addConverter(ContentData.class, String.class, new TypeConverter.Converter<ContentData, String>()
        {
            public String convert(ContentData source)
            {
                return source.getInfoUrl();
            }
        });
        addDynamicTwoStageConverter(ContentData.class, String.class, InputStream.class);
        
        //
        // Path
        //
        addConverter(Path.class, String.class, new TypeConverter.Converter<Path, String>()
        {
            public String convert(Path source)
            {
                return source.toString();
            }
        });
        addDynamicTwoStageConverter(Path.class, String.class, InputStream.class);
        
        //
        // Content Reader
        //
        addConverter(ContentReader.class, InputStream.class, new TypeConverter.Converter<ContentReader, InputStream>()
        {
            public InputStream convert(ContentReader source)
            {
                return source.getContentInputStream();
            }
        });
        addConverter(ContentReader.class, String.class, new TypeConverter.Converter<ContentReader, String>()
        {
            public String convert(ContentReader source)
            {
                // Getting the string from the ContentReader binary is meaningless
                return source.toString();
            }
        });

        //
        // Content Writer
        //
        addConverter(ContentWriter.class, String.class, new TypeConverter.Converter<ContentWriter, String>()
        {
            public String convert(ContentWriter source)
            {
                return source.toString();
            }
        });

        addConverter(Collection.class, BasicDBList.class, new TypeConverter.Converter<Collection, BasicDBList>()
        {
            public BasicDBList convert(Collection source)
            {
                BasicDBList ret = new BasicDBList();
                for(Object o : source)
                {
                    ret.add(o);
                }
                return ret;
            }
        });

        addConverter(BasicDBList.class, Collection.class, new TypeConverter.Converter<BasicDBList, Collection>()
        {
            @SuppressWarnings("unchecked")
            public Collection convert(BasicDBList source)
            {
                Collection ret = new LinkedList();
                for(Object o : source)
                {
                    ret.add(o);
                }
                return ret;
            }
        });

        //
        // Input Stream
        //
        addConverter(InputStream.class, String.class, new TypeConverter.Converter<InputStream, String>()
        {
            public String convert(InputStream source)
            {
                try
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = source.read(buffer)) > 0)
                    {
                        out.write(buffer, 0, read);
                    }
                    byte[] data = out.toByteArray();
                    return new String(data, "UTF-8");
                } 
                catch (UnsupportedEncodingException e)
                {
                    throw new TypeConversionException("Cannot convert input stream to String.", e);
                }
                catch (IOException e)
                {
                    throw new TypeConversionException("Conversion from stream to string failed", e);
                }
                finally
                {
                    if (source != null)
                    {
                        try
                        {
                            source.close();
                            }
                        catch(IOException e)
                        {
                            //NOOP
                        }
                    }
                }
            }
        });
        addDynamicTwoStageConverter(InputStream.class, String.class, Date.class);
        
        addDynamicTwoStageConverter(InputStream.class, String.class, Double.class);
        
        addDynamicTwoStageConverter(InputStream.class, String.class, Long.class);

        addDynamicTwoStageConverter(InputStream.class, String.class, Boolean.class);

        addDynamicTwoStageConverter(InputStream.class, String.class, QName.class);

        addDynamicTwoStageConverter(InputStream.class, String.class, Path.class);

        addDynamicTwoStageConverter(InputStream.class, String.class, NodeRef.class);
        
    }

}
