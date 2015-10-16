/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.domain.node;

/**
 * 
 * @author sglover
 *
 */
public interface NodeDAO
{
	NodeEntity newNode(long parentNodeId, long parentNodeVersion, long childNodeId);
	NodeEntity getByVersionLabel(long nodeId, String versionLabel);
	NodeEntity updateNode(long childNodeId, String childVersionLabel);

//    ChildAssocEntity newNode(
//            Long parentNodeId,
//            QName assocTypeQName,
//            QName assocQName,
//            StoreRef storeRef,
//            String uuid,
//            QName nodeTypeQName,
//            Locale nodeLocale,
//            String childNodeName,
//            Map<QName, Serializable> auditableProperties) throws InvalidTypeException;
}
