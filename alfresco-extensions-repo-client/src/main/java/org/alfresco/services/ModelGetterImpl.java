/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.httpclient.GetRequest;
import org.alfresco.httpclient.PostRequest;
import org.alfresco.httpclient.Response;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.services.solr.AlfrescoModel;
import org.alfresco.services.solr.AlfrescoModelDiff;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 
 * @author sglover
 *
 */
public class ModelGetterImpl implements ModelGetter
{
    private static final String GET_MODEL = "api/solr/model";
    private static final String GET_MODELS_DIFF = "api/solr/modelsdiff";

    private static final String CHECKSUM_HEADER = "XAlfresco-modelChecksum";

    private static final Log logger = LogFactory.getLog(ModelGetterImpl.class);

    private NamespaceService namespaceService;
    private AlfrescoHttpClient repoClient;

    public ModelGetterImpl(AlfrescoHttpClient repoClient, NamespaceService namespaceService)
    {
        this.repoClient = repoClient;
        this.namespaceService = namespaceService;
    }

    public AlfrescoModel getModel(QName modelName) throws AuthenticationException, IOException, JSONException
    {
        // If the model is new to the SOLR side the prefix will be unknown so we can not generate prefixes for the request!
        // Always use the full QName with explicit URI
        StringBuilder url = new StringBuilder(GET_MODEL);

        URLCodec encoder = new URLCodec();
        // must send the long name as we may not have the prefix registered
        url.append("?modelQName=").append(encoder.encode(modelName.toString(), "UTF-8"));
        
        GetRequest req = new GetRequest(url.toString());

        Response response = null;
        try
        {
            response = repoClient.sendRequest(req);
            if(response.getStatus() != HttpStatus.SC_OK)
            {
                throw new AlfrescoRuntimeException("GetModel return status is " + response.getStatus());
            }

            return new AlfrescoModel(M2Model.createModel(response.getContentAsStream()),
                    Long.valueOf(response.getHeader(CHECKSUM_HEADER)));
        }
        finally
        {
            if(response != null)
            {
                response.release();
            }
        }
    }
    
    public List<AlfrescoModelDiff> getModelsDiff(List<AlfrescoModel> currentModels) throws AuthenticationException, IOException, JSONException
    {
        StringBuilder url = new StringBuilder(GET_MODELS_DIFF);

        JSONObject body = new JSONObject();
        JSONArray jsonModels = new JSONArray();
        for(AlfrescoModel model : currentModels)
        {
            JSONObject jsonModel = new JSONObject();
            QName modelQName = QName.createQName( model.getModel().getName(), namespaceService);
//            QName modelQName = model.getModelDef().getName();
            jsonModel.put("name", modelQName.toString());
            jsonModel.put("checksum", model.getChecksum());
            jsonModels.put(jsonModel);
        }
        body.put("models", jsonModels);

        PostRequest req = new PostRequest(url.toString(), body.toString(), "application/json");
        Response response = null;
        JSONObject json = null;
        try
        {
            response = repoClient.sendRequest(req);
            if(response.getStatus() != HttpStatus.SC_OK)
            {
                throw new AlfrescoRuntimeException("GetModelsDiff return status is " + response.getStatus());
            }
    
            Reader reader = new BufferedReader(new InputStreamReader(response.getContentAsStream(), "UTF-8"));
            json = new JSONObject(new JSONTokener(reader));
        }
        finally
        {
            if(response != null)
            {
                response.release();
            }
        }
        
        if(logger.isDebugEnabled())
        {
            logger.debug(json.toString());
        }
        JSONArray jsonDiffs = json.getJSONArray("diffs");
        if(jsonDiffs == null)
        {
            throw new AlfrescoRuntimeException("GetModelsDiff badly formatted response");
        }

        List<AlfrescoModelDiff> diffs = new ArrayList<AlfrescoModelDiff>(jsonDiffs.length());
        for(int i = 0; i < jsonDiffs.length(); i++)
        {
            JSONObject jsonDiff = jsonDiffs.getJSONObject(i);
            diffs.add(new AlfrescoModelDiff(
                    QName.createQName(jsonDiff.getString("name")),
                    AlfrescoModelDiff.TYPE.valueOf(jsonDiff.getString("type")),
                    (jsonDiff.isNull("oldChecksum") ? null : jsonDiff.getLong("oldChecksum")),
                    (jsonDiff.isNull("newChecksum") ? null : jsonDiff.getLong("newChecksum"))));
        }

        return diffs;
    }
}
