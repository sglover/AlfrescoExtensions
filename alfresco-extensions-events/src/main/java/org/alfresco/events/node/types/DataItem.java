/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.events.node.types;

/**
 * An event that implements this interface indicates it has some unstructured
 * data that it wants to make available to Event Listeners.
 * 
 * The primary use case is to record this data for use by an ETL job.
 * 
 * @author Gethin James
 */
public interface DataItem {

	String getDataAsJson();
}
