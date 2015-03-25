/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;


/**
 * 
 * @author sglover
 *
 */
public class WrappingDictionaryDAO
{
//    implements DictionaryDAO, NamespaceDAO
//{
//    private static final Log logger = LogFactory.getLog(WrappingDictionaryDAO.class);
//
//	private DictionaryDAO dictionaryDAODelegate;
//	private NamespaceDAO namespaceDAODelegate;
//    private ModelTracker modelTracker;
//
//	public WrappingDictionaryDAO(DictionaryDAO dictionaryDAO, NamespaceDAO namespaceDAO, 
//	        NamespaceService namespaceService, SOLRAPIClient solrAPIClient)
//	{
//		this.dictionaryDAODelegate = dictionaryDAO;
//		this.namespaceDAODelegate = namespaceDAO;
//
//		modelTracker = new ModelTracker(dictionaryDAODelegate, namespaceDAODelegate, namespaceService,
//		        solrAPIClient);
////		try
////		{
////			modelTracker.trackModels();
////		}
////		catch(Exception e)
////		{
////			logger.warn("Exception tracking models ", e);
////		}
//	}
//
//	private void check()
//	{
//		try
//		{
//			modelTracker.trackModels();
//		}
//		catch(Exception e)
//		{
//			throw new RuntimeException(e);
//		}
//	}
//
//	@Override
//    public DataTypeDefinition getDataType(QName name)
//    {
//		DataTypeDefinition def = dictionaryDAODelegate.getDataType(name);
//		if(def == null)
//		{
//			check();
//			def = dictionaryDAODelegate.getDataType(name);
//		}
//		return def;
//    }
//
//	@Override
//    public DataTypeDefinition getDataType(Class javaClass)
//    {
//		DataTypeDefinition def = dictionaryDAODelegate.getDataType(javaClass);
//		if(def == null)
//		{
//			check();
//			def = dictionaryDAODelegate.getDataType(javaClass);
//		}
//		return def;
//    }
//
//	@Override
//    public TypeDefinition getType(QName name)
//    {
//	    TypeDefinition typeDef = dictionaryDAODelegate.getType(name);
//		if(typeDef == null)
//		{
//			check();
//			typeDef = dictionaryDAODelegate.getType(name);
//		}
//		return typeDef;
//    }
//
//	@Override
//    public AspectDefinition getAspect(QName name)
//    {
//		AspectDefinition aspectDef = dictionaryDAODelegate.getAspect(name);
//		if(aspectDef == null)
//		{
//			check();
//			aspectDef = dictionaryDAODelegate.getAspect(name);
//		}
//		return aspectDef;
//    }
//
//	@Override
//    public ClassDefinition getClass(QName name)
//    {
//	    ClassDefinition classDef = dictionaryDAODelegate.getClass(name);
//		if(classDef == null)
//		{
//			check();
//			classDef = dictionaryDAODelegate.getClass(name);
//		}
//		return classDef;
//    }
//
//	@Override
//    public PropertyDefinition getProperty(QName name)
//    {
//		PropertyDefinition propDef = dictionaryDAODelegate.getProperty(name);
//		if(propDef == null)
//		{
//			check();
//			propDef = dictionaryDAODelegate.getProperty(name);
//		}
//		return propDef;
//    }
//
//	@Override
//    public ConstraintDefinition getConstraint(QName name)
//    {
//		ConstraintDefinition constraintDef = dictionaryDAODelegate.getConstraint(name);
//		if(constraintDef == null)
//		{
//			check();
//			constraintDef = dictionaryDAODelegate.getConstraint(name);
//		}
//		return constraintDef;
//    }
//
//	@Override
//    public AssociationDefinition getAssociation(QName name)
//    {
//		AssociationDefinition assocDef = dictionaryDAODelegate.getAssociation(name);
//		if(assocDef == null)
//		{
//			check();
//			assocDef = dictionaryDAODelegate.getAssociation(name);
//		}
//		return assocDef;
//    }
//
//	@Override
//    public List<DictionaryListener> getDictionaryListeners()
//    {
//	    return dictionaryDAODelegate.getDictionaryListeners();
//    }
//
//	@Override
//    public DictionaryRegistry getDictionaryRegistry(String tenantDomain)
//    {
//	    return dictionaryDAODelegate.getDictionaryRegistry(tenantDomain);
//    }
//
//	@Override
//    public boolean isContextRefreshed()
//    {
//	    return dictionaryDAODelegate.isContextRefreshed();
//    }
//
//	@Override
//    public Collection<QName> getModels(boolean includeInherited)
//    {
//		Collection<QName> models = dictionaryDAODelegate.getModels(includeInherited);
//		if(models == null)
//		{
//			check();
//			models = getModels(includeInherited);
//		}
//		return models;
//    }
//
//	@Override
//    public Collection<QName> getTypes(boolean includeInherited)
//    {
//		Collection<QName> types = dictionaryDAODelegate.getTypes(includeInherited);
//		if(types == null)
//		{
//			check();
//			types = dictionaryDAODelegate.getTypes(includeInherited);
//		}
//		return types;
//    }
//
//	@Override
//    public Collection<QName> getAssociations(boolean includeInherited)
//    {
//		Collection<QName> assocs = dictionaryDAODelegate.getAssociations(includeInherited);
//		if(assocs == null)
//		{
//			check();
//			assocs = dictionaryDAODelegate.getAssociations(includeInherited);
//		}
//		return assocs;
//    }
//
//	@Override
//    public Collection<QName> getAspects(boolean includeInherited)
//    {
//		Collection<QName> aspects = dictionaryDAODelegate.getAspects(includeInherited);
//		if(aspects == null)
//		{
//			check();
//			aspects = dictionaryDAODelegate.getAspects(includeInherited);
//		}
//		return aspects;
//    }
//
//	@Override
//    public ModelDefinition getModel(QName name)
//    {
//		ModelDefinition modelDef = dictionaryDAODelegate.getModel(name);
//		if(modelDef == null)
//		{
//			check();
//			modelDef = dictionaryDAODelegate.getModel(name);
//		}
//		return modelDef;
//    }
//
//	@Override
//    public Collection<DataTypeDefinition> getDataTypes(QName model)
//    {
//		Collection<DataTypeDefinition> dataTypes = dictionaryDAODelegate.getDataTypes(model);
//		if(dataTypes == null)
//		{
//			check();
//			dataTypes = dictionaryDAODelegate.getDataTypes(model);
//		}
//		return dataTypes;
//    }
//
//	@Override
//    public Collection<TypeDefinition> getTypes(QName model)
//    {
//		Collection<TypeDefinition> types = dictionaryDAODelegate.getTypes(model);
//	    if(types == null)
//		{
//			check();
//			types = dictionaryDAODelegate.getTypes(model);
//		}
//	    return types;
//    }
//
//	@Override
//    public Collection<QName> getSubTypes(QName superType, boolean follow)
//    {
//		Collection<QName> subTypes = dictionaryDAODelegate.getSubTypes(superType, follow);
//	    if(subTypes == null)
//		{
//			check();
//			subTypes = dictionaryDAODelegate.getSubTypes(superType, follow);
//		}
//	    return subTypes;
//    }
//
//	@Override
//    public Collection<AspectDefinition> getAspects(QName model)
//    {
//		Collection<AspectDefinition> aspects = dictionaryDAODelegate.getAspects(model);
//	    if(aspects == null)
//		{
//			check();
//			aspects = dictionaryDAODelegate.getAspects(model);
//		}
//	    return aspects;
//    }
//
//	@Override
//    public Collection<AssociationDefinition> getAssociations(QName model)
//    {
//		Collection<AssociationDefinition> assocs = dictionaryDAODelegate.getAssociations(model);
//	    if(assocs == null)
//		{
//			check();
//			assocs = dictionaryDAODelegate.getAssociations(model);
//		}
//	    return assocs;
//    }
//
//	@Override
//    public Collection<QName> getSubAspects(QName superAspect, boolean follow)
//    {
//		Collection<QName> subAspects = dictionaryDAODelegate.getSubAspects(superAspect, follow);
//	    if(subAspects == null)
//		{
//			check();
//			subAspects = dictionaryDAODelegate.getSubAspects(superAspect, follow);
//		}
//	    return subAspects;
//    }
//
//	@Override
//    public Collection<PropertyDefinition> getProperties(QName model)
//    {
//		Collection<PropertyDefinition> properties = dictionaryDAODelegate.getProperties(model);
//	    if(properties == null)
//		{
//			check();
//			properties = dictionaryDAODelegate.getProperties(model);
//		}
//	    return properties;
//    }
//
//	@Override
//    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects)
//    {
//		TypeDefinition typeDef = dictionaryDAODelegate.getAnonymousType(type, aspects);
//	    if(typeDef == null)
//		{
//			check();
//			typeDef = dictionaryDAODelegate.getAnonymousType(type, aspects);
//		}
//	    return typeDef;
//    }
//
//	@Override
//    public QName putModel(M2Model model)
//    {
//	    return dictionaryDAODelegate.putModel(model);
//    }
//
//	@Override
//    public QName putModelIgnoringConstraints(M2Model model)
//    {
//	    return dictionaryDAODelegate.putModelIgnoringConstraints(model);
//    }
//
//	@Override
//    public void removeModel(QName model)
//    {
//		dictionaryDAODelegate.removeModel(model);
//    }
//
//	@Override
//    public Collection<PropertyDefinition> getProperties(QName modelName,
//            QName dataType)
//    {
//		Collection<PropertyDefinition> properties = dictionaryDAODelegate.getProperties(modelName, dataType);
//	    if(properties == null)
//		{
//			check();
//			properties = dictionaryDAODelegate.getProperties(modelName, dataType);
//		}
//	    return properties;
//    }
//
//	@Override
//    public Collection<PropertyDefinition> getPropertiesOfDataType(QName dataType)
//    {
//		Collection<PropertyDefinition> properties = dictionaryDAODelegate.getPropertiesOfDataType(dataType);
//	    if(properties == null)
//		{
//			check();
//			properties = dictionaryDAODelegate.getPropertiesOfDataType(dataType);
//		}
//	    return properties;
//    }
//
//	@Override
//    public Collection<NamespaceDefinition> getNamespaces(QName modelName)
//    {
//		Collection<NamespaceDefinition> namespaces = dictionaryDAODelegate.getNamespaces(modelName);
//	    if(namespaces == null)
//		{
//			check();
//			namespaces = dictionaryDAODelegate.getNamespaces(modelName);
//		}
//	    return namespaces;
//    }
//
//	@Override
//    public Collection<ConstraintDefinition> getConstraints(QName model)
//    {
//		Collection<ConstraintDefinition> constraints = dictionaryDAODelegate.getConstraints(model);
//	    if(constraints == null)
//		{
//			check();
//			constraints = dictionaryDAODelegate.getConstraints(model);
//		}
//	    return constraints;
//    }
//
//	@Override
//    public Collection<ConstraintDefinition> getConstraints(QName model,
//            boolean referenceableDefsOnly)
//    {
//		Collection<ConstraintDefinition> constraints = dictionaryDAODelegate.getConstraints(model, referenceableDefsOnly);
//	    if(constraints == null)
//		{
//			check();
//			constraints = dictionaryDAODelegate.getConstraints(model, referenceableDefsOnly);
//		}
//	    return constraints;
//    }
//
//	@Override
//    public List<M2ModelDiff> diffModel(M2Model model)
//    {
//	    return dictionaryDAODelegate.diffModel(model);
//    }
//
//	@Override
//    public List<M2ModelDiff> diffModelIgnoringConstraints(M2Model model)
//    {
//	    return dictionaryDAODelegate.diffModelIgnoringConstraints(model);
//    }
//
//	@Override
//    public void registerListener(DictionaryListener dictionaryListener)
//    {
//		dictionaryDAODelegate.registerListener(dictionaryListener);
//    }
//
//	@Override
//    public void reset()
//    {
//		dictionaryDAODelegate.reset();
//    }
//
//	@Override
//    public void init()
//    {
//		dictionaryDAODelegate.init();
//    }
//
//	@Override
//    public void destroy()
//    {
//		dictionaryDAODelegate.destroy();
//    }
//
//	@Override
//    public boolean isModelInherited(QName name)
//    {
//		// TODO should probably detect missing and try to get it by calling check()
//	    return dictionaryDAODelegate.isModelInherited(name);
//    }
//
//	@Override
//    public String getDefaultAnalyserResourceBundleName()
//    {
//	    return dictionaryDAODelegate.getDefaultAnalyserResourceBundleName();
//    }
//
//	@Override
//    public ClassLoader getResourceClassLoader()
//    {
//	    return dictionaryDAODelegate.getResourceClassLoader();
//    }
//
//	@Override
//    public void setResourceClassLoader(ClassLoader resourceClassLoader)
//    {
//		dictionaryDAODelegate.setResourceClassLoader(resourceClassLoader);
//    }
//
////	@Override
////    public CompiledModel getCompiledModel(QName modelName)
////    {
////	    CompiledModel compiledModel = dictionaryDAODelegate.getCompiledModel(modelName);
////	    if(compiledModel == null)
////		{
////			check();
////			compiledModel = dictionaryDAODelegate.getCompiledModel(modelName);
////		}
////	    return compiledModel;
////    }
//
//	@Override
//    public String getNamespaceURI(String prefix) throws NamespaceException
//    {
//		String namespaceURI = namespaceDAODelegate.getNamespaceURI(prefix);
//	    if(namespaceURI == null)
//		{
//			check();
//			namespaceURI = namespaceDAODelegate.getNamespaceURI(prefix);
//		}
//	    return namespaceURI;
//    }
//
//	@Override
//    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
//    {
//		Collection<String> prefixes = namespaceDAODelegate.getPrefixes(namespaceURI);
//	    if(prefixes == null)
//		{
//			check();
//			prefixes = namespaceDAODelegate.getPrefixes(namespaceURI);
//		}
//	    return prefixes;
//    }
//
//	@Override
//    public Collection<String> getPrefixes()
//    {
//		Collection<String> prefixes = namespaceDAODelegate.getPrefixes();
//	    if(prefixes == null)
//		{
//			check();
//			prefixes = namespaceDAODelegate.getPrefixes();
//		}
//	    return prefixes;
//    }
//
//	@Override
//    public Collection<String> getURIs()
//    {
//		Collection<String> uris = namespaceDAODelegate.getURIs();
//	    if(uris == null)
//		{
//			check();
//			uris = namespaceDAODelegate.getURIs();
//		}
//	    return uris;
//    }
//
//	@Override
//    public void addURI(String uri)
//    {
//		namespaceDAODelegate.addURI(uri);
//    }
//
//	@Override
//    public void removeURI(String uri)
//    {
//		namespaceDAODelegate.removeURI(uri);
//    }
//
//	@Override
//    public void addPrefix(String prefix, String uri)
//    {
//		namespaceDAODelegate.addPrefix(prefix, uri);
//    }
//
//	@Override
//    public void removePrefix(String prefix)
//    {
//		namespaceDAODelegate.removePrefix(prefix);
//    }
}
