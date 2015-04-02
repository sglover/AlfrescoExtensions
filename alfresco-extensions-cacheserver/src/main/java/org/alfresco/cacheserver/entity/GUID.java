/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.entity;

import java.util.UUID;

/**
 * 
 * @author sglover
 *
 */
public final class GUID
{
   /**
    * Private Constructor for GUID.
    */
   private GUID()
   {
   }

   /**
    * Generates and returns a new GUID as a string
    *
    * @return String GUID
    */
   public static String generate()
   {
       return UUID.randomUUID().toString();
   }
}