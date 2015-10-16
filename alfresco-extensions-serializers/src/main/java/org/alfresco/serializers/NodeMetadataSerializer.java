/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.serializers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.mongodb.BasicDBObjectBuilder;

/**
 * 
 * @author sglover
 *
 */
public interface NodeMetadataSerializer
{
	void buildNodeMetadata(BasicDBObjectBuilder builder,
            String nodeId, String changeTxnId, String nodeType,
            Map<String, Serializable> props, Set<String> aspects);
    void buildNodeMetadata(BasicDBObjectBuilder builder, NodeVersionKey nodeVersionKey, String changeTxnId,
            Long txnId, String nodeType, Map<String, Serializable> props, Set<String> aspectQNames);
    void buildNodeMetadata(XContentBuilder builder,
            NodeVersionKey nodeVersionKey, String changeTxnId, Long txnId, String nodeType,
            Map<String, Serializable> props, Set<String> aspects) throws IOException;
}
