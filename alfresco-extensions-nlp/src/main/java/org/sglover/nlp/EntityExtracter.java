/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.nlp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.alfrescoextensions.common.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 
 * @author sglover
 *
 */
@Component
public class EntityExtracter
{
    private static Log logger = LogFactory.getLog(EntityExtracter.class
            .getName());

    @Autowired()
    @Qualifier(value="coreNLPEntityTagger")
    private EntityTagger entityTagger;

    private Set<String> includeNodeTypes = new HashSet<>();

    public EntityExtracter()
    {
    }

    public EntityExtracter(EntityTagger entityTagger)
    {
        this.entityTagger = entityTagger;
    }

    @PostConstruct
    public void init()
    {
        this.includeNodeTypes.add("cm:content");
    }

    public void getEntities(Node node, ReadableByteChannel channel, EntityTaggerCallback callback)
    {
        logger.debug("Node " + node + " extracting entities");

        ExtractEntitiesRunnable extractEntities = new ExtractEntitiesRunnable(node, channel, callback);
        extractEntities.run();
    }

    private class ExtractEntitiesRunnable extends ExtractEntities implements
            Runnable
    {
        private EntityTaggerCallback callback;

        public ExtractEntitiesRunnable(Node node, ReadableByteChannel channel, EntityTaggerCallback callback)
        {
            super(node, channel);
            this.callback = callback;
        }

        @Override
        public void run()
        {
            executeAsync(callback);
        }
    }

    private String getContent(ReadableByteChannel channel) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        ByteBuffer bb = ByteBuffer.allocate(2048);
        int c = -1;
        do
        {
            c = channel.read(bb);
            bb.flip();
            bb.clear();
            sb.append(new String(bb.array(), "UTF-8"));
        }
        while(c != -1);

        String content = sb.toString();
        return content;
    }

    private class ExtractEntities
    {
        private Node node;
        private ReadableByteChannel channel;

        public ExtractEntities(Node node, ReadableByteChannel channel)
        {
            this.node = node;
            this.channel = channel;
        }

        public Entities execute() throws IOException
        {
            Entities entities = null;

//            ReadableByteChannel channel = contentStore.getChannel(node);
            if (channel != null)
            {
                String content = getContent(channel);

                logger.debug("OpenNlp node = " + node);

                entities = entityTagger.getEntities(content);
            }
            else
            {
                logger.warn("Unable to get text content for node " + node);
            }

            return entities;
        }

        public void executeAsync(EntityTaggerCallback callback)
        {
            try
            {
//                ReadableByteChannel channel = contentStore.getChannel(node);
                if (channel != null)
                {
                    String content = getContent(channel);

                    logger.debug("OpenNlp node = " + node);

                    entityTagger.getEntities(content, callback);
                }
                else
                {
                    logger.warn("Unable to get text content for node " + node);
                }
            }
            catch (IOException e)
            {
                logger.warn("", e);
            }
        }
    }
}
