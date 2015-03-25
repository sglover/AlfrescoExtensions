/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.IOException;
import java.util.List;

import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.service.namespace.QName;
import org.alfresco.services.solr.AlfrescoModel;
import org.alfresco.services.solr.AlfrescoModelDiff;
import org.json.JSONException;

/**
 * 
 * @author sglover
 *
 */
public interface ModelGetter
{
    AlfrescoModel getModel(QName modelName) throws AuthenticationException, IOException, JSONException;
    List<AlfrescoModelDiff> getModelsDiff(List<AlfrescoModel> currentModels) throws AuthenticationException, IOException, JSONException;
}
