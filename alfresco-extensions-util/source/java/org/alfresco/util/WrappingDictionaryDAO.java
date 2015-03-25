/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.util;

import java.util.Collection;
import java.util.List;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.repo.dictionary.DictionaryRegistry;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2ModelDiff;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.alfresco.solr.client.SOLRAPIClient;

public class WrappingDictionaryDAO implements DictionaryDAO, NamespaceDAO
{
	private DictionaryDAO dictionaryDAODelegate;
	private NamespaceDAO namespaceDAODelegate;
    private ModelTracker modelTracker;

	public WrappingDictionaryDAO(DictionaryDAO dictionaryDAO, NamespaceDAO namespaceDAO, SOLRAPIClient solrAPIClient)
	{
		modelTracker = new ModelTracker(dictionaryDAODelegate, namespaceDAODelegate, solrAPIClient);

		this.dictionaryDAODelegate = dictionaryDAO;
		this.namespaceDAODelegate = namespaceDAO;
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
    public DataTypeDefinition getDataType(QName name)
    {
		DataTypeDefinition def = dictionaryDAODelegate.getDataType(name);
		if(def == null)
		{
			check();
		}
		def = dictionaryDAODelegate.getDataType(name);
		return def;
    }

	@Override
    public DataTypeDefinition getDataType(Class javaClass)
    {
	    return dictionaryDAODelegate.getDataType(javaClass);
    }

	@Override
    public TypeDefinition getType(QName name)
    {
	    return dictionaryDAODelegate.getType(name);
    }

	@Override
    public AspectDefinition getAspect(QName name)
    {
	    return dictionaryDAODelegate.getAspect(name);
    }

	@Override
    public ClassDefinition getClass(QName name)
    {
	    return dictionaryDAODelegate.getClass(name);
    }

	@Override
    public PropertyDefinition getProperty(QName name)
    {
	    return dictionaryDAODelegate.getProperty(name);
    }

	@Override
    public ConstraintDefinition getConstraint(QName name)
    {
	    return dictionaryDAODelegate.getConstraint(name);
    }

	@Override
    public AssociationDefinition getAssociation(QName name)
    {
	    return dictionaryDAODelegate.getAssociation(name);
    }

	@Override
    public List<DictionaryListener> getDictionaryListeners()
    {
	    return dictionaryDAODelegate.getDictionaryListeners();
    }

	@Override
    public DictionaryRegistry getDictionaryRegistry(String tenantDomain)
    {
	    return dictionaryDAODelegate.getDictionaryRegistry(tenantDomain);
    }

	@Override
    public boolean isContextRefreshed()
    {
	    return dictionaryDAODelegate.isContextRefreshed();
    }

	@Override
    public Collection<QName> getModels(boolean includeInherited)
    {
	    return dictionaryDAODelegate.getModels(includeInherited);
    }

	@Override
    public Collection<QName> getTypes(boolean includeInherited)
    {
	    return dictionaryDAODelegate.getTypes(includeInherited);
    }

	@Override
    public Collection<QName> getAssociations(boolean includeInherited)
    {
	    return dictionaryDAODelegate.getAssociations(includeInherited);
    }

	@Override
    public Collection<QName> getAspects(boolean includeInherited)
    {
	    return dictionaryDAODelegate.getAspects(includeInherited);
    }

	@Override
    public ModelDefinition getModel(QName name)
    {
	    return dictionaryDAODelegate.getModel(name);
    }

	@Override
    public Collection<DataTypeDefinition> getDataTypes(QName model)
    {
	    return dictionaryDAODelegate.getDataTypes(model);
    }

	@Override
    public Collection<TypeDefinition> getTypes(QName model)
    {
	    return dictionaryDAODelegate.getTypes(model);
    }

	@Override
    public Collection<QName> getSubTypes(QName superType, boolean follow)
    {
	    return dictionaryDAODelegate.getSubTypes(superType, follow);
    }

	@Override
    public Collection<AspectDefinition> getAspects(QName model)
    {
	    return dictionaryDAODelegate.getAspects(model);
    }

	@Override
    public Collection<AssociationDefinition> getAssociations(QName model)
    {
	    return dictionaryDAODelegate.getAssociations(model);
    }

	@Override
    public Collection<QName> getSubAspects(QName superAspect, boolean follow)
    {
	    return dictionaryDAODelegate.getSubAspects(superAspect, follow);
    }

	@Override
    public Collection<PropertyDefinition> getProperties(QName model)
    {
	    return dictionaryDAODelegate.getProperties(model);
    }

	@Override
    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects)
    {
	    return dictionaryDAODelegate.getAnonymousType(type, aspects);
    }

	@Override
    public QName putModel(M2Model model)
    {
	    return dictionaryDAODelegate.putModel(model);
    }

	@Override
    public QName putModelIgnoringConstraints(M2Model model)
    {
	    return dictionaryDAODelegate.putModelIgnoringConstraints(model);
    }

	@Override
    public void removeModel(QName model)
    {
		dictionaryDAODelegate.removeModel(model);
    }

	@Override
    public Collection<PropertyDefinition> getProperties(QName modelName,
            QName dataType)
    {
	    return dictionaryDAODelegate.getProperties(modelName, dataType);
    }

	@Override
    public Collection<PropertyDefinition> getPropertiesOfDataType(QName dataType)
    {
	    return dictionaryDAODelegate.getPropertiesOfDataType(dataType);
    }

	@Override
    public Collection<NamespaceDefinition> getNamespaces(QName modelName)
    {
	    return dictionaryDAODelegate.getNamespaces(modelName);
    }

	@Override
    public Collection<ConstraintDefinition> getConstraints(QName model)
    {
	    return dictionaryDAODelegate.getConstraints(model);
    }

	@Override
    public Collection<ConstraintDefinition> getConstraints(QName model,
            boolean referenceableDefsOnly)
    {
	    return dictionaryDAODelegate.getConstraints(model, referenceableDefsOnly);
    }

	@Override
    public List<M2ModelDiff> diffModel(M2Model model)
    {
	    return dictionaryDAODelegate.diffModel(model);
    }

	@Override
    public List<M2ModelDiff> diffModelIgnoringConstraints(M2Model model)
    {
	    return dictionaryDAODelegate.diffModelIgnoringConstraints(model);
    }

	@Override
    public void registerListener(DictionaryListener dictionaryListener)
    {
		dictionaryDAODelegate.registerListener(dictionaryListener);
    }

	@Override
    public void reset()
    {
		dictionaryDAODelegate.reset();
    }

	@Override
    public void init()
    {
		dictionaryDAODelegate.init();
    }

	@Override
    public void destroy()
    {
		dictionaryDAODelegate.destroy();
    }

	@Override
    public boolean isModelInherited(QName name)
    {
	    return dictionaryDAODelegate.isModelInherited(name);
    }

	@Override
    public String getDefaultAnalyserResourceBundleName()
    {
	    return dictionaryDAODelegate.getDefaultAnalyserResourceBundleName();
    }

	@Override
    public ClassLoader getResourceClassLoader()
    {
	    return dictionaryDAODelegate.getResourceClassLoader();
    }

	@Override
    public void setResourceClassLoader(ClassLoader resourceClassLoader)
    {
		dictionaryDAODelegate.setResourceClassLoader(resourceClassLoader);
    }

	@Override
    public CompiledModel getCompiledModel(QName modelName)
    {
	    return dictionaryDAODelegate.getCompiledModel(modelName);
    }

	@Override
    public String getNamespaceURI(String prefix) throws NamespaceException
    {
	    return namespaceDAODelegate.getNamespaceURI(prefix);
    }

	@Override
    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
		return namespaceDAODelegate.getPrefixes(namespaceURI);
    }

	@Override
    public Collection<String> getPrefixes()
    {
		return namespaceDAODelegate.getPrefixes();
    }

	@Override
    public Collection<String> getURIs()
    {
		return namespaceDAODelegate.getURIs();
    }

	@Override
    public void addURI(String uri)
    {
		namespaceDAODelegate.addURI(uri);
    }

	@Override
    public void removeURI(String uri)
    {
		namespaceDAODelegate.removeURI(uri);
    }

	@Override
    public void addPrefix(String prefix, String uri)
    {
		namespaceDAODelegate.addPrefix(prefix, uri);
    }

	@Override
    public void removePrefix(String prefix)
    {
		namespaceDAODelegate.removePrefix(prefix);
    }
}
