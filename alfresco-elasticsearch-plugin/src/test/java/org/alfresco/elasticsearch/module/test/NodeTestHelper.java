/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.elasticsearch.module.test;

import org.elasticsearch.common.logging.log4j.LogConfigurator;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.IOException;

public class NodeTestHelper {

    public static Node createNode(String clusterName) throws IOException {
        ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();

        settingsBuilder.put("gateway.type", "none");
        settingsBuilder.put("cluster.name", clusterName);
        settingsBuilder.put("index.number_of_shards", 1);
        settingsBuilder.put("index.number_of_replicas", 1);
        settingsBuilder.put("opennlp.models.name.file", "src/test/resources/models/en-ner-person.bin");
        settingsBuilder.put("opennlp.models.date.file", "src/test/resources/models/en-ner-date.bin");
        settingsBuilder.put("opennlp.models.location.file", "src/test/resources/models/en-ner-location.bin");

        LogConfigurator.configure(settingsBuilder.build());

        return NodeBuilder.nodeBuilder().settings(settingsBuilder.build()).node();
    }
}
