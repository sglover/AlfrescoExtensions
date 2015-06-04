/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events.node;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeCheckOutCancelledEvent;
import org.alfresco.events.node.types.NodeCheckedInEvent;
import org.alfresco.events.node.types.NodeCheckedOutEvent;
import org.alfresco.events.node.types.NodeCommentedEvent;
import org.alfresco.events.node.types.NodeContentGetEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeEvent;
import org.alfresco.events.node.types.NodeFavouritedEvent;
import org.alfresco.events.node.types.NodeLikedEvent;
import org.alfresco.events.node.types.NodeMovedEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;
import org.alfresco.events.node.types.NodeRenamedEvent;
import org.alfresco.events.node.types.NodeTaggedEvent;
import org.alfresco.events.node.types.NodeUnFavouritedEvent;
import org.alfresco.events.node.types.NodeUnLikedEvent;
import org.alfresco.events.node.types.NodeUnTaggedEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
import org.alfresco.events.node.types.Property;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.Client;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event service implementation. Generates events and sends them to an event queue.
 * 
 * TODO: transaction rollback handling, deletion of nodes (currently tied to beforeDeleteNode).
 * 
 * @author steveglover
 */
public class EventsServiceImpl extends AbstractEventsService implements EventsService
{
    private static Log logger = LogFactory.getLog(EventsServiceImpl.class);

	private NodeRenamedEvent nodeRenamedEvent(NodeInfo nodeInfo, String oldName, String newName)
	{
	    NodeRenamedEvent event = new NodeRenamedEvent();
        populate(event, nodeInfo);

        List<String> newPaths = nodeInfo.getPaths();

        nodeInfo.updateName(oldName);
        List<String> paths = nodeInfo.getPaths();

        event.setNewName(newName);
        event.setToPaths(newPaths);
        event.setPaths(paths);

		return event;
	}

	@Override
	public void nodeMoved(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
	{
		NodeRef nodeRef = newChildAssocRef.getChildRef();
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeMovedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
			NodeRef oldParentNodeRef = oldChildAssocRef.getParentRef();
			NodeRef newParentNodeRef = newChildAssocRef.getParentRef();

			// Work out the old and new paths. Note that the FileFolderService sets the node name to a temporary name during the move,
			// so we can't rely on the name. Use the association name instead.
			String oldName = oldChildAssocRef.getQName().getLocalName();
			String tmpNewName = newChildAssocRef.getQName().getLocalName();
			String newName = null;
			if(oldName != null && tmpNewName != null && !oldName.equals(tmpNewName))
			{
				newName = tmpNewName;
			}
			String oldParentNodeName = (String)nodeService.getProperty(oldParentNodeRef, ContentModel.PROP_NAME);
			String newParentNodeName = (String)nodeService.getProperty(newParentNodeRef, ContentModel.PROP_NAME);
			List<Path> newParentPaths = nodeService.getPaths(newParentNodeRef, false);
			List<String> newPaths = getPaths(newParentPaths, Arrays.asList(newParentNodeName, tmpNewName));

            // renames are handled by an onUpdateProperties callback, we just deal with real moves here.
            if(!oldParentNodeRef.equals(newParentNodeRef))
            {
                List<List<String>> toParentNodeIds = getNodeIds(newParentPaths);
    			List<Path> oldParentPaths = nodeService.getPaths(oldParentNodeRef, false);
    			List<String> previousPaths = getPaths(oldParentPaths, Arrays.asList(oldParentNodeName, oldName));
    			List<List<String>> previousParentNodeIds = getNodeIds(oldParentPaths);

    			NodeMovedEvent event = new NodeMovedEvent();
                populate(event, nodeInfo);
                event.setName(oldName);
                event.setNewName(newName);
                event.setPaths(previousPaths);
                event.setParentNodeIds(previousParentNodeIds);
                event.setToParentNodeIds(toParentNodeIds);
                event.setToPaths(newPaths);
    			sendEvent(event);
            }
		}
	}

	@Override
	public void nodeRenamed(NodeRef nodeRef, String oldName, String newName)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeRenamedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
			NodeRenamedEvent nodeRenamedEvent = nodeRenamedEvent(nodeInfo, oldName, newName);
			sendEvent(nodeRenamedEvent);
		}
	}

	@Override
	public void nodeTagged(final NodeRef nodeRef, final String tag)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeTaggedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeTaggedEvent event = new NodeTaggedEvent();
            populate(event, nodeInfo);
            
            event.setTag(tag);

	    	sendEvent(event);
		}
	}
	
	@Override
	public void nodeTagRemoved(final NodeRef nodeRef, final String tag)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUnTaggedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeUnTaggedEvent event = new NodeUnTaggedEvent();
            populate(event, nodeInfo);
            
            event.setTag(tag);

	    	sendEvent(event);
		}
	}
	
	@Override
	public void nodeLiked(final NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeLikedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeLikedEvent();
            populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}
	
	@Override
	public void nodeUnLiked(final NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUnLikedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeUnLikedEvent();
            populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}

	private void populate(NodeEvent nodeEvent, NodeInfo nodeInfo)
	{
        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        String networkId = TenantUtil.getCurrentDomain();
        String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
        List<String> nodePaths = nodeInfo.getPaths();
        List<List<String>> parentNodeIds = nodeInfo.getParentNodeIds();
        Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());
        Map<String, Serializable> properties = nodeInfo.getProperties();
        Set<String> aspects = nodeInfo.getAspectsAsStrings();
        long aclId = nodeInfo.getAclId();

	    nodeEvent.setSeqNumber(nextSequenceNumber());
	    nodeEvent.setName(nodeInfo.getName());
	    nodeEvent.setTxnId(nodeInfo.getTxnId());
	    nodeEvent.setAclId(aclId);
	    long timestamp = System.currentTimeMillis();
	    nodeEvent.setTimestamp(timestamp);
	    nodeEvent.setNetworkId(networkId);
	    nodeEvent.setSiteId(nodeInfo.getSiteId());
	    nodeEvent.setNodeId(nodeInfo.getNodeId());
	    nodeEvent.setNodeInternalId(nodeInfo.getNodeInternalId());
	    nodeEvent.setVersionLabel(nodeInfo.getVersionLabel());
	    nodeEvent.setNodeVersion(nodeInfo.getNodeVersion());
	    nodeEvent.setNodeType(nodeType);
	    nodeEvent.setPaths(nodePaths);
	    nodeEvent.setParentNodeIds(parentNodeIds);
	    nodeEvent.setUsername(username);
	    nodeEvent.setNodeModificationTime(nodeInfo.getModificationTimestamp());
	    nodeEvent.setClient(alfrescoClient);
	    nodeEvent.setNodeProperties(properties);
	    nodeEvent.setAspects(aspects);
	}

	@Override
	public void nodeFavourited(NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeFavouritedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeFavouritedEvent();
	    	populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}
	
	@Override
	public void nodeUnFavourited(NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUnFavouritedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeUnFavouritedEvent();
	    	populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}

	@Override
	public void nodeCreated(final NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeAddedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeAddedEvent();
	    	populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}
	
    @Override
	public void nodeDeleted(final NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeRemovedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeRemovedEvent();
	    	populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}

	@Override
	public void nodeCommented(final NodeRef nodeRef, final String comment)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeCommentedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeCommentedEvent event = new NodeCommentedEvent();
	    	populate(event, nodeInfo);
	    	
	    	event.setComment(comment);

	    	sendEvent(event);
		}
	}

	@Override
	public void nodeUpdated(final NodeRef nodeRef, final Map<String, Property> propertiesAdded,
			final Set<String> propertiesRemoved, final Map<String, Property> propertiesChanged,
			final Set<String> aspectsAdded, final Set<String> aspectsRemoved)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUpdatedEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeUpdatedEvent event = new NodeUpdatedEvent();
	    	populate(event, nodeInfo);

	    	event.setAspectsAdded(aspectsAdded);
	        event.setAspectsAdded(aspectsRemoved);
	        
	        event.setPropertiesAdded(propertiesAdded);
	        event.setPropertiesChanged(propertiesChanged);
	        event.setPropertiesRemoved(propertiesRemoved);

    		sendEvent(event);
		}
	}

	@Override
	public void contentGet(NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeContentGetEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeContentGetEvent event = new NodeContentGetEvent();
            populate(event, nodeInfo);

	    	sendEvent(event);
		}
	}

	@Override
	public void contentWrite(NodeRef nodeRef, QName propertyQName, ContentData value)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeContentPutEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
		    NodeContentPutEvent event = new NodeContentPutEvent();
            populate(event, nodeInfo);

            long size = (value != null ? value.getSize() : 0);
            String mimeType = value.getMimetype();
            String encoding = value.getEncoding();
            event.setSize(size);
            event.setMimeType(mimeType);
            event.setEncoding(encoding);

	    	sendEvent(event);
		}
	}

	public void nodeCheckedOut(NodeRef workingCopyNodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(workingCopyNodeRef, NodeCheckedOutEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeCheckedOutEvent event = new NodeCheckedOutEvent();
            populate(event, nodeInfo);

            String workingCopyNodeId = nodeInfo.getNodeId();
            event.setCheckedOutNodeId(workingCopyNodeId);

	    	sendEvent(event);
		}
	}

	public void nodeCheckOutCancelled(NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeCheckOutCancelledEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeCheckOutCancelledEvent();
            populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}

	public void nodeCheckedIn(NodeRef nodeRef)
	{
		NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeCheckedInEvent.EVENT_TYPE);
		if(nodeInfo.checkNodeInfo())
		{
	    	NodeEvent event = new NodeCheckedInEvent();
            populate(event, nodeInfo);
	    	sendEvent(event);
		}
	}
}
