/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.httpclient.AuthenticationException;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2ModelDiff;
import org.alfresco.repo.dictionary.M2Namespace;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition.XMLBindingType;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.services.solr.AlfrescoModel;
import org.alfresco.services.solr.AlfrescoModelDiff;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

/**
 * 
 * @author sglover
 *
 */
public class ModelTracker
{
    private static Log logger = LogFactory.getLog(ModelTracker.class);

    private DictionaryDAO dictionaryDAO;
    private NamespaceService namespaceService;
    private ModelGetter modelGetter;

    private HashMap<String, Set<String>> modelErrors = new HashMap<String, Set<String>>();

    public ModelTracker(DictionaryDAO dictionaryDAO, NamespaceService namespaceService, ModelGetter modelGetter)
    {
        this.dictionaryDAO = dictionaryDAO;
        this.namespaceService = namespaceService;
        this.modelGetter = modelGetter;
    }

//    protected AlfrescoHttpClient getRepoClient(SecureCommsType commsType)
//    {
//        // TODO i18n
//        KeyStoreParameters keyStoreParameters = new KeyStoreParameters("SSL Key Store", sslKeyStoreType, sslKeyStoreProvider, sslKeyStorePasswordFileLocation, sslKeyStoreLocation);
//        KeyStoreParameters trustStoreParameters = new KeyStoreParameters("SSL Trust Store", sslTrustStoreType, sslTrustStoreProvider, sslTrustStorePasswordFileLocation, sslTrustStoreLocation);
//        SSLEncryptionParameters sslEncryptionParameters = new SSLEncryptionParameters(keyStoreParameters, trustStoreParameters);
//
//        KeyResourceLoaderImpl keyResourceLoader = new KeyResourceLoaderImpl();
//
//        HttpClientFactory httpClientFactory = new HttpClientFactory(commsType,
//                sslEncryptionParameters, keyResourceLoader, null, null, alfrescoHost,
//                alfrescoPort, alfrescoPortSSL, maxTotalConnections, maxHostConnections, socketTimeout);
//        // TODO need to make port configurable depending on secure comms, or just make redirects
//        // work
//        AlfrescoHttpClient repoClient = httpClientFactory.getRepoClient(alfrescoHost, alfrescoPortSSL);
//        repoClient.setBaseUrl(baseUrl);
//        return repoClient;
//    }
//
//    private SOLRAPIClient getSOLRAPIClient()
//    {
//    	AlfrescoHttpClient httpClient = getRepoClient(SecureCommsType.NONE);
//        SOLRAPIClient solrAPIClient = new SOLRAPIClient(httpClient, dictionaryService, namespaceDAO);
//        return solrAPIClient;
//    }

    /**
     * @param alfrescoModelDir
     * @param modelName
     */
    private void removeMatchingModels(File alfrescoModelDir, QName modelName)
    {
        final String prefix = modelName.toPrefixString(namespaceService).replace(":", ".") + ".";
        final String postFix = ".xml";

        File[] toDelete = alfrescoModelDir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                if (pathname.isDirectory())
                {
                    return false;
                }
                String name = pathname.getName();
                if (false == name.endsWith(postFix))
                {
                    return false;
                }
                if (false == name.startsWith(prefix))
                {
                    return false;
                }
                // check is number between
                String checksum = name.substring(prefix.length(), name.length() - postFix.length());
                try
                {
                    Long.parseLong(checksum);
                    return true;
                }
                catch (NumberFormatException nfe)
                {
                    return false;
                }
            }
        });

        if (toDelete != null)
        {
            for (File file : toDelete)
            {
                file.delete();
            }
        }
    }

    private void loadModel(Map<String, M2Model> modelMap, HashSet<String> loadedModels, M2Model model)
    {
        String modelName = model.getName();
        if (loadedModels.contains(modelName) == false)
        {
            for (M2Namespace importNamespace : model.getImports())
            {
                M2Model importedModel = modelMap.get(importNamespace.getUri());
                if (importedModel != null)
                {

                    // Ensure that the imported model is loaded first
                    loadModel(modelMap, loadedModels, importedModel);
                }
            }

            if(putModel(model))
            {
                loadedModels.add(modelName);
            }
            logger.info("Loading model " + model.getName());
        }
    }

    private String getModelFileName(ModelDefinition model)
    {
        String modelName = model.getName().toPrefixString(namespaceService);
        return modelName.replace(":", ".") + "." + model.getChecksum(XMLBindingType.DEFAULT) + ".xml";
    }

//    private String getModelFileName(M2Model model)
//    {
//        return model.getName().replace(":", ".") + "." + model.getChecksum(XMLBindingType.DEFAULT) + ".xml";
//    }

    private boolean putModel(M2Model model)
    {
        Set<String> errors = validateModel(model);
        if(errors.size() == 0)
        {
            modelErrors.remove(model.getName());
            dictionaryDAO.putModelIgnoringConstraints(model);
            return true;
        }
        else
        {
            if(!modelErrors.containsKey(model.getName()))
            {
                modelErrors.put(model.getName(), errors);
                logger.warn(errors.iterator().next());
            }
            return false;
        }
       
    }

    public void trackModels() throws AuthenticationException, IOException, JSONException
    {
        // track models
        // reflect changes changes and update on disk copy
    	
    	try
    	{
	        List<AlfrescoModelDiff> modelDiffs = modelGetter.getModelsDiff(Collections.<AlfrescoModel>emptyList());
	        HashMap<String, M2Model> modelMap = new HashMap<String, M2Model>();
	
	        for (AlfrescoModelDiff modelDiff : modelDiffs)
	        {
	            switch (modelDiff.getType())
	            {
	            case CHANGED:
	                AlfrescoModel changedModel = modelGetter.getModel(modelDiff.getModelName());
	                for (M2Namespace namespace : changedModel.getModel().getNamespaces())
	                {
	                    modelMap.put(namespace.getUri(), changedModel.getModel());
	                }
	                break;
	            case NEW:
	                AlfrescoModel newModel = modelGetter.getModel(modelDiff.getModelName());
	                for (M2Namespace namespace : newModel.getModel().getNamespaces())
	                {
	                    modelMap.put(namespace.getUri(), newModel.getModel());
	                }
	                break;
	            case REMOVED:
	                // At the moment we do not unload models - I can see no side effects .... 
	                // However search is used to check for references to indexed properties or types
	                // This will be partially broken anyway due to eventual consistency
	                // A model should only be unloaded if there are no data dependencies
	                // Should have been on the de-lucene list.
	                break;
	            }
	        }
	
	        HashSet<String> loadedModels = new HashSet<String>();
	        for (M2Model model : modelMap.values())
	        {
	            loadModel(modelMap, loadedModels, model);
	        }
	
	//        if(loadedModels.size() > 0)
	//        {
	//            this.infoSrv.afterInitModels();
	//        }
	
	        File alfrescoModelDir = new File("alfrescoModels");
	        if (!alfrescoModelDir.exists())
	        {
	            alfrescoModelDir.mkdir();
	        }
	        for (AlfrescoModelDiff modelDiff : modelDiffs)
	        {
	            switch (modelDiff.getType())
	            {
	            case CHANGED:
	                removeMatchingModels(alfrescoModelDir, modelDiff.getModelName());
	                ModelDefinition changedModel = dictionaryDAO.getModel(modelDiff.getModelName());

//	                M2Model changedModel = dictionaryDAO.getCompiledModel(modelDiff.getModelName()).getM2Model();
	                File changedFile = new File(alfrescoModelDir, getModelFileName(changedModel));
	                FileOutputStream cos = new FileOutputStream(changedFile);
//	                changedModel.toXML(cos);
	                changedModel.toXML(null, cos);
	                cos.flush();
	                cos.close();
	                break;
	            case NEW:
//	                M2Model newModel = dictionaryDAO.getCompiledModel(modelDiff.getModelName()).getM2Model();
	                ModelDefinition newModel = dictionaryDAO.getModel(modelDiff.getModelName());
	                // add on file
	                File newFile = new File(alfrescoModelDir, getModelFileName(newModel));
	                FileOutputStream nos = new FileOutputStream(newFile);
//	                newModel.toXML(nos);
	                newModel.toXML(null, nos);
	                nos.flush();
	                nos.close();
	                break;
	            case REMOVED:
	                removeMatchingModels(alfrescoModelDir, modelDiff.getModelName());
	                break;
	            }
	        }
    	}
    	catch(IOException e)
    	{
    		logger.warn("", e);
    	}

//        trackerStats.addModelTime(end-start);

//        if(true == runPostModelLoadInit)
//        {
//            for(Object key : props.keySet())
//            {
//                String stringKey = (String)key;
//                if(stringKey.startsWith("alfresco.index.store"))
//                {
//                    StoreRef store = new StoreRef(props.getProperty(stringKey));
//                    indexedStores.add(store);
//                }
//                if(stringKey.startsWith("alfresco.ignore.store"))
//                {
//                    StoreRef store = new StoreRef(props.getProperty(stringKey));
//                    ignoredStores.add(store);
//                }
//                if(stringKey.startsWith("alfresco.index.tenant"))
//                {
//                    indexedTenants.add(props.getProperty(stringKey));
//                }
//                if(stringKey.startsWith("alfresco.ignore.tenant"))
//                {
//                    ignoredTenants.add(props.getProperty(stringKey));
//                }
//                if(stringKey.startsWith("alfresco.index.datatype"))
//                {
//                    QName qname = expandQName(props.getProperty(stringKey));
//                    indexedDataTypes.add(qname);
//                }
//                if(stringKey.startsWith("alfresco.ignore.datatype"))
//                {
//                    QName qname = expandQName(props.getProperty(stringKey));
//                    ignoredDataTypes.add(qname);
//                }
//                if(stringKey.startsWith("alfresco.index.type"))
//                {
//                    QName qname = expandQName(props.getProperty(stringKey));
//                    indexedTypes.add(qname);
//                }
//                if(stringKey.startsWith("alfresco.ignore.type"))
//                {
//                    QName qname = expandQName(props.getProperty(stringKey));
//                    ignoredTypes.add(qname);
//                }
//                if(stringKey.startsWith("alfresco.index.aspect"))
//                {
//                    QName qname = expandQName(props.getProperty(stringKey));
//                    indexedAspects.add(qname);
//                }
//                if(stringKey.startsWith("alfresco.ignore.aspect"))
//                {
//                    QName qname = expandQName(props.getProperty(stringKey));
//                    ignoredAspects.add(qname);
//                }
//                if(stringKey.startsWith("alfresco.index.field"))
//                {
//                    String name = expandName(props.getProperty(stringKey));
//                    indexedFields.add(name);
//                }
//                if(stringKey.startsWith("alfresco.ignore.field"))
//                {
//                    String name = expandName(props.getProperty(stringKey));
//                    ignoredFields.add(name);
//                }
//            }
//            runPostModelLoadInit = false;
//        }

    }

    private Set<String> validateModel(M2Model model)
    {
        HashSet<String> errors = new HashSet<String>();
        try 
        { 
//            dictionaryDAO.getCompiledModel(QName.createQName(model.getName(), namespaceDAO)); 
            dictionaryDAO.getModel(QName.createQName(model.getName(), namespaceService));
        } 
        catch (DictionaryException e) 
        {
            // No model to diff
            return errors;
        }
        catch(NamespaceException e)
        {
            // namespace unknown - no model 
            return errors;
        }
        
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModelIgnoringConstraints(model);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            if (modelDiff.getDiffType().equals(M2ModelDiff.DIFF_UPDATED))
            {
                errors.add("Model not updated: "+model.getName()  + "   Failed to validate model update - found non-incrementally updated " + modelDiff.getElementType() + " '" + modelDiff.getElementName() + "'");
            }
        }
        return errors;
    }
}
