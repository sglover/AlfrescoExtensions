/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events.node;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeCheckOutCancelledEvent;
import org.alfresco.events.node.types.NodeCheckedInEvent;
import org.alfresco.events.node.types.NodeCheckedOutEvent;
import org.alfresco.events.node.types.NodeContentGetEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeMovedEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.serializers.PropertySerializer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author steveglover
 *
 */
public class EventGenerationBehaviours implements
		ContentServicePolicies.OnContentPropertyUpdatePolicy,
		ContentServicePolicies.OnContentReadPolicy,
		NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.BeforeDeleteNodePolicy,
		NodeServicePolicies.OnAddAspectPolicy,
		NodeServicePolicies.OnRemoveAspectPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy,
		NodeServicePolicies.OnMoveNodePolicy,
		CheckOutCheckInServicePolicies.BeforeCheckOut,
		CheckOutCheckInServicePolicies.OnCheckOut,
		CheckOutCheckInServicePolicies.OnCheckIn,
		CheckOutCheckInServicePolicies.OnCancelCheckOut
//		NodeServicePolicies.OnUpdateNodePolicy
{
	protected static Log logger = LogFactory.getLog(EventGenerationBehaviours.class);

	protected Set<String> includeEventTypes;
	protected PolicyComponent policyComponent;

	protected List<BehaviourDefinition<ClassBehaviourBinding>> behaviours = new LinkedList<>();

	protected EventsService eventsService;
	protected DictionaryService dictionaryService;
	protected NamespaceService namespaceService;
	protected PropertySerializer propertySerializer;

	protected void addBehaviour(BehaviourDefinition<ClassBehaviourBinding> binding)
	{
		behaviours.add(binding);

		logger.debug("Added policy binding " + binding);
	}
	
	protected void removeBehaviour(BehaviourDefinition<ClassBehaviourBinding> binding)
	{
		removeBehaviourImpl(binding);

		behaviours.remove(binding);
	}

	protected void removeBehaviourImpl(BehaviourDefinition<ClassBehaviourBinding> binding)
	{
		this.policyComponent.removeClassDefinition(binding);

		logger.debug("Removed policy binding " + binding);
	}

	public void cleanUp()
	{
		for(BehaviourDefinition<ClassBehaviourBinding> binding : behaviours)
		{
			removeBehaviourImpl(binding);
		}
	}

	public void setIncludeEventTypes(String includeEventTypesStr)
	{
		StringTokenizer st = new StringTokenizer(includeEventTypesStr, ",");
		this.includeEventTypes = new HashSet<String>();
		while(st.hasMoreTokens())
		{
			String eventType = st.nextToken().trim();
			this.includeEventTypes.add(eventType);
		}
	}

	public void setPolicyComponent(PolicyComponent policyComponent)
	{
		this.policyComponent = policyComponent;
	}
	
	protected boolean includeEventType(String eventType)
	{
		return includeEventTypes.contains(eventType);
	}

	public void setPropertySerializer(PropertySerializer propertySerializer)
	{
		this.propertySerializer = propertySerializer;
	}

	public void setDictionaryService(DictionaryService dictionaryService)
	{
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService)
	{
		this.namespaceService = namespaceService;
	}

	public void setEventsService(EventsService eventsService)
	{
		this.eventsService = eventsService;
	}

	public void init()
	{
		if(includeEventType(NodeContentPutEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME, 
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onContentPropertyUpdate"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeContentGetEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							ContentServicePolicies.OnContentReadPolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onContentRead"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeAddedEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							NodeServicePolicies.OnCreateNodePolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCreateNode"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeRemovedEvent.EVENT_TYPE))
		{
			// on before delete so that we have the relevant node details available
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "beforeDeleteNode"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeMovedEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							NodeServicePolicies.OnMoveNodePolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onMoveNode"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeCheckedOutEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.BeforeCheckOut.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "beforeCheckOut"));
			addBehaviour(binding);

			binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.OnCheckOut.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCheckOut"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeCheckedInEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.OnCheckIn.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCheckIn"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeCheckOutCancelledEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.OnCancelCheckOut.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCancelCheckOut"));
			addBehaviour(binding);
		}

		BehaviourDefinition<ClassBehaviourBinding> binding =
				this.policyComponent.bindClassBehaviour(
						NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
						ContentModel.TYPE_BASE,
						new JavaBehaviour(this, "onUpdateProperties"));
		addBehaviour(binding);
	}

	private Set<String> getRemoves(Map<QName, Serializable> before, Map<QName, Serializable> after)
	{
		Set<QName> tmp = new HashSet<QName>(before.keySet());
		tmp.removeAll(after.keySet());

		Set<String> ret = new HashSet<String>();
		for(QName propQName : tmp)
		{
			ret.add(propQName.toPrefixString(namespaceService));
		}

		return ret;
	}

	private Map<String, Object> getAdds(Map<QName, Serializable> before, Map<QName, Serializable> after)
	{
		Map<String, Object> ret = new HashMap<>();

		Set<QName> tmp = new HashSet<QName>(after.keySet());
		tmp.removeAll(before.keySet());

		Map<QName, Serializable> properties = new HashMap<>();
		for(QName propQName : tmp)
		{
			Serializable value = after.get(propQName);
			properties.put(propQName, value);
		}

		ret = propertySerializer.serialize(properties);
		return ret;
	}

	private Map<String, Object> getChanges(Map<QName, Serializable> before, Map<QName, Serializable> after)
	{
		Map<String, Object> ret = new HashMap<>();

		Set<QName> intersect = new HashSet<QName>(before.keySet());
		intersect.retainAll(after.keySet());

		Map<QName, Serializable> properties = new HashMap<>();
		for(QName propQName : intersect)
		{
			Serializable valueBefore = before.get(propQName);
			Serializable valueAfter = after.get(propQName);
			
			Serializable value = null;
			if(valueBefore == null && valueAfter == null)
			{
				continue;
			}
			else if(valueBefore == null && valueAfter != null)
			{
				value = valueAfter;
			}
			else if(valueBefore != null && valueAfter == null)
			{
				value = valueAfter;
			}
			else if(!valueBefore.equals(valueAfter))
			{
				value = valueAfter;
			}

			properties.put(propQName, value);
		}

		ret = propertySerializer.serialize(properties);
		return ret;
	}

	@Override
	public void onContentRead(NodeRef nodeRef) 
	{
		eventsService.contentGet(nodeRef);
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef)
	{
		NodeRef nodeRef = childAssocRef.getChildRef();
		eventsService.nodeCreated(nodeRef);
	}

	/*
	 * Checks whether a property has changed value (not including being null before)
	 */
	private boolean propertyChanged(Map<QName, Serializable> before, Map<QName, Serializable> after, QName propertyQName)
	{
		boolean isChanged = false;

		String valueBefore = (String)before.get(propertyQName);
		String valueAfter = (String)after.get(propertyQName);

		if(valueBefore != null && valueAfter != null)
		{
			isChanged = !valueBefore.equals(valueAfter);
		}

		return isChanged;
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
	{
		// handle renames
		if(propertyChanged(before, after, ContentModel.PROP_NAME))
		{
			String oldName = (String)before.get(ContentModel.PROP_NAME);
			String newName = (String)after.get(ContentModel.PROP_NAME);
			eventsService.nodeRenamed(nodeRef, oldName, newName);
		}

		if(includeEventType(NodeUpdatedEvent.EVENT_TYPE))
		{
			Map<String, Object> propertiesAdded = getAdds(before, after);
			Set<String> propertiesRemoved = getRemoves(before, after);
			Map<String, Object> propertiesChanged = getChanges(before, after);
			eventsService.nodeUpdated(nodeRef, propertiesAdded, propertiesRemoved, propertiesChanged, null, null);
		}
	}

	@Override
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
		eventsService.nodeUpdated(nodeRef, null, null, null, null, Collections.singleton(aspectTypeQName.toPrefixString()));
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
		eventsService.nodeUpdated(nodeRef, null, null, null, Collections.singleton(aspectTypeQName.toPrefixString()), null);
	}

	@Override
	public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
	{
		eventsService.nodeMoved(oldChildAssocRef, newChildAssocRef);
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef)
	{
		eventsService.nodeDeleted(nodeRef);
	}

	@Override
	public void onContentPropertyUpdate(NodeRef nodeRef, QName propertyQName, ContentData beforeValue, ContentData afterValue)
	{
		boolean hasContentBefore = ContentData.hasContent(beforeValue) && beforeValue.getSize() > 0;
        boolean hasContentAfter = ContentData.hasContent(afterValue) && afterValue.getSize() > 0;
        
        // There are some shortcuts here
        if (!hasContentBefore && !hasContentAfter)
        {
            // Really, nothing happened
            return;
        }
        else if (EqualsHelper.nullSafeEquals(beforeValue, afterValue))
        {
            // Still, nothing happening
        	return;
        }

        eventsService.contentWrite(nodeRef, propertyQName, afterValue);
	}

	@Override
	public void onCheckOut(NodeRef workingCopy)
	{
		eventsService.nodeCheckedOut(workingCopy);
	}

	@Override
	public void onCancelCheckOut(NodeRef nodeRef)
	{
		eventsService.nodeCheckOutCancelled(nodeRef);
	}

	@Override
	public void onCheckIn(NodeRef nodeRef)
	{
		eventsService.nodeCheckedIn(nodeRef);
	}

	@Override
    public void beforeCheckOut(
            NodeRef nodeRef,
            NodeRef destinationParentNodeRef,
            QName destinationAssocTypeQName, 
            QName destinationAssocQName)
	{
	}
}
