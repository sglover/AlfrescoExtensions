package org.alfresco.events

import java.io.IOException

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind._

/**
  * Created by sglover on 29/11/2015.
  */
class JsonClassKeyDeserializer extends KeyDeserializer
{
  @throws(classOf[IOException])
  @throws(classOf[JsonProcessingException])
  def deserializeKeyToClass(key:String): AnyRef = {
    if (!key.startsWith("class "))
    {
      throw new IllegalArgumentException("Invalid key format");
    }
    val classname:String = key.replaceFirst("class ", "");
    try
    {
      classOf[JsonClassKeyDeserializer].getClassLoader().loadClass(classname);
    }
    catch {
      case e: ClassNotFoundException => {
        throw new IllegalArgumentException(e)
      }
    }
  }

  @throws(classOf[IOException])
  @throws(classOf[JsonProcessingException])
  override def deserializeKey(key:String, ctxt:DeserializationContext): AnyRef = {
      deserializeKeyToClass(key)
  }
}
