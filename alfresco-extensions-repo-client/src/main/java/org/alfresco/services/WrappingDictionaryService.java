/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.httpclient.AlfrescoHttpClient;
import org.alfresco.repo.dictionary.CompiledModelsCache;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;

/**
 * 
 * @author sglover
 *
 */
public class WrappingDictionaryService implements DictionaryService, NamespaceService
{
    private ModelGetter modelGetter;
    private ModelTracker modelTracker;

    private DictionaryDAO dictionaryDAO;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;

    public WrappingDictionaryService(AlfrescoHttpClient repoClient) 
    {
        TenantService tenantService = new SingleTServiceImpl();

        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);

        CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
        compiledModelsCache.setDictionaryDAO(dictionaryDAO);
        compiledModelsCache.setTenantService(tenantService);
        compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                4,
                1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
                );
        compiledModelsCache.setThreadPoolExecutor(threadPoolExecutor);

        dictionaryDAO.setDictionaryRegistryCache(compiledModelsCache);
        dictionaryDAO.setResourceClassLoader(getClass().getClassLoader());
        dictionaryDAO.init();

        DictionaryComponent dictionaryService = new DictionaryComponent();
        dictionaryService.setDictionaryDAO(dictionaryDAO);
        this.dictionaryService = dictionaryService;

        DictionaryNamespaceComponent namespaceService = new DictionaryNamespaceComponent();
        namespaceService.setNamespaceDAO(dictionaryDAO);
        this.namespaceService = namespaceService;

        this.modelGetter = new ModelGetterImpl(repoClient, namespaceService);

        this.modelTracker = new ModelTracker(dictionaryDAO, namespaceService, modelGetter);
    }

    private void check()
    {
        try
        {
            modelTracker.trackModels();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getMessage(String messageKey)
    {
        String message = dictionaryService.getMessage(messageKey);
        if(message == null)
        {
            check();
            message = dictionaryService.getMessage(messageKey);
        }
        return message;
    }

    @Override
    public String getMessage(String messageKey, Locale locale)
    {
        String message = dictionaryService.getMessage(messageKey, locale);
        if(message == null)
        {
            check();
            message = dictionaryService.getMessage(messageKey, locale);
        }
        return message;
    }

    @Override
    public String getMessage(String messageKey, Object... params)
    {
        String message = dictionaryService.getMessage(messageKey, params);
        if(message == null)
        {
            check();
            message = dictionaryService.getMessage(messageKey, params);
        }
        return message;
    }

    @Override
    public String getMessage(String messageKey, Locale locale, Object... params)
    {
        String message = dictionaryService.getMessage(messageKey, locale, params);
        if(message == null)
        {
            check();
            message = dictionaryService.getMessage(messageKey, locale, params);
        }
        return message;
    }

    @Override
    public Collection<QName> getAllModels()
    {
        Collection<QName> models = dictionaryService.getAllModels();
        if(models == null)
        {
            check();
            models = dictionaryService.getAllModels();
        }
        return models;
    }

    @Override
    public ModelDefinition getModel(QName model)
    {
        ModelDefinition modelDefinition = dictionaryDAO.getModel(model);
        if(modelDefinition == null)
        {
            check();
            modelDefinition = dictionaryDAO.getModel(model);
        }
        return modelDefinition;
    }

    @Override
    public Collection<QName> getAllDataTypes()
    {
        Collection<QName> dataTypes = dictionaryService.getAllDataTypes();
        if(dataTypes == null)
        {
            check();
            dataTypes = dictionaryService.getAllDataTypes();
        }
        return dataTypes;
    }

    @Override
    public Collection<QName> getDataTypes(QName model)
    {
        Collection<QName> dataTypes = dictionaryService.getDataTypes(model);
        if(dataTypes == null)
        {
            check();
            dataTypes = dictionaryService.getDataTypes(model);
        }
        return dataTypes;
    }

    @Override
    public DataTypeDefinition getDataType(QName name)
    {
        DataTypeDefinition def = dictionaryDAO.getDataType(name);
        if(def == null)
        {
            check();
            def = dictionaryDAO.getDataType(name);
        }
        return def;
    }

    @Override
    public DataTypeDefinition getDataType(Class<?> javaClass)
    {
        DataTypeDefinition dataType = dictionaryService.getDataType(javaClass);
        if(dataType == null)
        {
            check();
            dataType = dictionaryService.getDataType(javaClass);
        }
        return dataType;
    }

    @Override
    public Collection<QName> getAllTypes()
    {
        Collection<QName> types = dictionaryService.getAllTypes();
        if(types == null)
        {
            check();
            types = dictionaryService.getAllTypes();
        }
        return types;
    }

    @Override
    public Collection<QName> getAllTypes(boolean includeInherited) {
        Collection<QName> types = dictionaryService.getAllTypes();
        if(types == null)
        {
            check();
            types = dictionaryService.getAllTypes();
        }
        return types;
    }

    @Override
    public Collection<QName> getSubTypes(QName type, boolean follow) {
        Collection<QName> types = dictionaryService.getAllTypes();
        if(types == null)
        {
            check();
            types = dictionaryService.getAllTypes();
        }
        return types;
    }

    @Override
    public Collection<QName> getTypes(QName model) {
        Collection<QName> types = dictionaryService.getAllTypes();
        if(types == null)
        {
            check();
            types = dictionaryService.getAllTypes();
        }
        return types;
    }

    @Override
    public TypeDefinition getType(QName name)
    {
        TypeDefinition type = dictionaryService.getType(name);
        if(type == null)
        {
            check();
            type = dictionaryService.getType(name);
        }
        return type;
    }

    @Override
    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects)
    {
        TypeDefinition typeDef = dictionaryService.getAnonymousType(type, aspects);
        if(typeDef == null)
        {
            check();
            typeDef = dictionaryService.getAnonymousType(type, aspects);
        }
        return typeDef;
    }

    @Override
    public TypeDefinition getAnonymousType(QName name)
    {
        TypeDefinition typeDef = dictionaryService.getAnonymousType(name);
        if(typeDef == null)
        {
            check();
            typeDef = dictionaryService.getAnonymousType(name);
        }
        return typeDef;
    }

    @Override
    public Collection<QName> getAllAspects()
    {
        Collection<QName> aspects = dictionaryService.getAllAspects();
        if(aspects == null)
        {
            check();
            aspects = dictionaryService.getAllAspects();
        }
        return aspects;
    }

    @Override
    public Collection<QName> getAllAspects(boolean includeInherited)
    {
        Collection<QName> aspects = dictionaryService.getAllAspects(includeInherited);
        if(aspects == null)
        {
            check();
            aspects = dictionaryService.getAllAspects(includeInherited);
        }
        return aspects;
    }

    @Override
    public Collection<QName> getSubAspects(QName aspect, boolean follow)
    {
        Collection<QName> aspects = dictionaryService.getSubAspects(aspect, follow);
        if(aspects == null)
        {
            check();
            aspects = dictionaryService.getSubAspects(aspect, follow);
        }
        return aspects;
    }

    @Override
    public Collection<QName> getAspects(QName model)
    {
        Collection<QName> aspects = dictionaryService.getAspects(model);
        if(aspects == null)
        {
            check();
            aspects = dictionaryService.getAspects(model);
        }
        return aspects;
    }

    @Override
    public Collection<QName> getAssociations(QName model)
    {
        Collection<QName> assocs = dictionaryService.getAssociations(model);
        if(assocs == null)
        {
            check();
            assocs = dictionaryService.getAssociations(model);
        }
        return assocs;
    }

    @Override
    public AspectDefinition getAspect(QName name)
    {
        AspectDefinition aspect = dictionaryService.getAspect(name);
        if(aspect == null)
        {
            check();
            aspect = dictionaryService.getAspect(name);
        }
        return aspect;
    }

    @Override
    public ClassDefinition getClass(QName name)
    {
        ClassDefinition clazz = dictionaryService.getClass(name);
        if(clazz == null)
        {
            check();
            clazz = dictionaryService.getClass(name);
        }
        return clazz;
    }

    @Override
    public boolean isSubClass(QName className, QName ofClassName)
    {
        boolean isSubClass = dictionaryService.isSubClass(className, ofClassName);
        if(!isSubClass)
        {
            check();
            isSubClass = dictionaryService.isSubClass(className, ofClassName);
        }
        return isSubClass;
    }

    @Override
    public PropertyDefinition getProperty(QName className, QName propertyName)
    {
        PropertyDefinition propertyDefinition = dictionaryService.getProperty(className, propertyName);
        if(propertyDefinition == null)
        {
            check();
            propertyDefinition = dictionaryService.getProperty(className, propertyName);
        }
        return propertyDefinition;
    }

    @Override
    public Map<QName, PropertyDefinition> getPropertyDefs(QName className)
    {
        Map<QName, PropertyDefinition> propertyDefs = dictionaryService.getPropertyDefs(className);
        if(propertyDefs == null)
        {
            check();
            propertyDefs = dictionaryService.getPropertyDefs(className);
        }
        return propertyDefs;
    }

    @Override
    public PropertyDefinition getProperty(QName propertyName)
    {
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyName);
        if(propertyDef == null)
        {
            check();
            propertyDef = dictionaryService.getProperty(propertyName);
        }
        return propertyDef;
    }

    @Override
    public Collection<QName> getAllProperties(QName dataType)
    {
        Collection<QName> propertyDefs = dictionaryService.getAllProperties(dataType);
        if(propertyDefs == null)
        {
            check();
            propertyDefs = dictionaryService.getAllProperties(dataType);
        }
        return propertyDefs;
    }

    @Override
    public Collection<QName> getProperties(QName model, QName dataType)
    {
        Collection<QName> propertyDefs = dictionaryService.getProperties(model, dataType);
        if(propertyDefs == null)
        {
            check();
            propertyDefs = dictionaryService.getProperties(model, dataType);
        }
        return propertyDefs;
    }

    @Override
    public Collection<QName> getProperties(QName model)
    {
        Collection<QName> propertyDefs = dictionaryService.getProperties(model);
        if(propertyDefs == null)
        {
            check();
            propertyDefs = dictionaryService.getProperties(model);
        }
        return propertyDefs;
    }

    @Override
    public AssociationDefinition getAssociation(QName associationName) 
    {
        AssociationDefinition assocDef = dictionaryService.getAssociation(associationName);
        if(assocDef == null)
        {
            check();
            assocDef = dictionaryService.getAssociation(associationName);
        }
        return assocDef;
    }

    @Override
    public Collection<QName> getAllAssociations()
    {
        Collection<QName> assocs = dictionaryService.getAllAssociations();
        if(assocs == null)
        {
            check();
            assocs = dictionaryService.getAllAssociations();
        }
        return assocs;
    }

    @Override
    public Collection<QName> getAllAssociations(boolean includeInherited)
    {
        Collection<QName> assocs = dictionaryService.getAllAssociations(includeInherited);
        if(assocs == null)
        {
            check();
            assocs = dictionaryService.getAllAssociations(includeInherited);
        }
        return assocs;
    }

    @Override
    public ConstraintDefinition getConstraint(QName constraintQName)
    {
        ConstraintDefinition constraint = dictionaryService.getConstraint(constraintQName);
        if(constraint == null)
        {
            check();
            constraint = dictionaryService.getConstraint(constraintQName);
        }
        return constraint;
    }

    @Override
    public Collection<ConstraintDefinition> getConstraints(QName model)
    {
        Collection<ConstraintDefinition> constraints = dictionaryService.getConstraints(model);
        if(constraints == null)
        {
            check();
            constraints = dictionaryService.getConstraints(model);
        }
        return constraints;
    }

    @Override
    public Collection<ConstraintDefinition> getConstraints(QName model, boolean referenceableDefsOnly)
    {
        Collection<ConstraintDefinition> constraints = dictionaryService.getConstraints(model, referenceableDefsOnly);
        if(constraints == null)
        {
            check();
            constraints = dictionaryService.getConstraints(model, referenceableDefsOnly);
        }
        return constraints;
    }

    @Override
    public String getNamespaceURI(String prefix) throws NamespaceException
    {
        String namespaceURI = namespaceService.getNamespaceURI(prefix);
        if(namespaceURI == null)
        {
            check();
            namespaceURI = namespaceService.getNamespaceURI(prefix);
        }
        return namespaceURI;
    }

    @Override
    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
        Collection<String> prefixes = namespaceService.getPrefixes(namespaceURI);
        if(prefixes == null)
        {
            check();
            prefixes = namespaceService.getPrefixes(namespaceURI);
        }
        return prefixes;
    }

    @Override
    public Collection<String> getPrefixes() 
    {
        Collection<String> prefixes = namespaceService.getPrefixes();
        if(prefixes == null)
        {
            check();
            prefixes = namespaceService.getPrefixes();
        }
        return prefixes;
    }

    @Override
    public Collection<String> getURIs()
    {
        Collection<String> uris = namespaceService.getURIs();
        if(uris == null)
        {
            check();
            uris = namespaceService.getURIs();
        }
        return uris;
    }

    @Override
    public void registerNamespace(String prefix, String uri)
    {
        namespaceService.registerNamespace(prefix, uri);
    }

    @Override
    public void unregisterNamespace(String prefix)
    {
        namespaceService.unregisterNamespace(prefix);
    }
}
