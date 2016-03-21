/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.checksum.PatchDocument;
import org.alfresco.extensions.common.MimeType;
import org.alfresco.extensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public class MockContentStore implements ContentStore
{
    private Map<Node, Content> testContentByNodeId = new HashMap<>();
    private Map<String, Content> testContentByNodePath = new HashMap<>();
    private Map<Long, Content> testContentByNodeInternalId = new HashMap<>();

    private interface Content
    {
        ReadableByteChannel getChannel();
        Long getSize();
        InputStream getInputStream() throws IOException;
        Reader getReader() throws IOException;
    }

    private static class StringContent implements Content
    {
        private String str;
        private Long size;

        public StringContent(String str, Long size)
        {
            super();
            this.str = str;
            this.size = size;
        }

        public ReadableByteChannel getChannel()
        {
            ByteArrayInputStream in = new ByteArrayInputStream(
                    str.getBytes());
            ReadableByteChannel channel = Channels.newChannel(in);
            return channel;
        }

        public InputStream getInputStream() throws IOException
        {
            ByteArrayInputStream in = new ByteArrayInputStream(
                    str.getBytes());
            return in;
        }

        public Reader getReader() throws IOException
        {
            Reader reader = new BufferedReader(new StringReader(str));
            return reader;
        }

        public Long getSize()
        {
            return size;
        }
    }

    private static class FileContent implements Content
    {
        private File file;
        private Long size;
        private ReadableByteChannel channel;

        @SuppressWarnings("resource")
        public FileContent(File file, Long size) throws FileNotFoundException
        {
            super();
            this.file = file;
            this.channel = new FileInputStream(file).getChannel();
            this.size = size;
        }

        public ReadableByteChannel getChannel()
        {
            return channel;
        }

        public Long getSize()
        {
            return size;
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            return new FileInputStream(file);
        }

        @Override
        public Reader getReader() throws IOException
        {
            return new FileReader(file);
        }
    }

    public void addTestContent(long nodeInternalId, String nodeId, long nodeVersion,
            String path, String content, String mimeType)
    {
        long size = content.getBytes().length;
        Node node = Node.build().nodeId(nodeId).nodeInternalId(nodeInternalId).nodeVersion(nodeVersion);
        Content c = new StringContent(content, size);
        testContentByNodeId.put(node, c);
        testContentByNodeInternalId.put(nodeInternalId, c);
        testContentByNodePath.put(path, c);
    }

    public void addTestContent(long nodeInternalId, String nodeId, long nodeVersion,
            String path, File file, String mimeType) throws FileNotFoundException
    {
        long size = file.length();
        Node node = Node.build().nodeId(nodeId).nodeInternalId(nodeInternalId).nodeVersion(nodeVersion);
        Content content = new FileContent(file, size);
        testContentByNodeId.put(node, content);
        testContentByNodeInternalId.put(nodeInternalId, content);
        testContentByNodePath.put(path, content);
    }

    @Override
    public boolean exists(String nodeId, long nodeVersion)
    {
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion);
        return testContentByNodeId.get(node) != null;
    }

    @Override
    public boolean exists(String nodeId, long nodeVersion, MimeType mimeType)
    {
        //MimeType.INSTANCES.getByMimetype(mimetype)
        Node node = Node.build().nodeId(nodeId).nodeVersion(nodeVersion).mimeType(mimeType);
        return testContentByNodeId.get(node) != null;
    }

    private class MockContentReader extends AbstractContentReader
    {
        public MockContentReader(Node node)
        {
            super(node);
        }

        @Override
        public ContentStore getStore()
        {
            return MockContentStore.this;
        }

        @Override
        public ReadableByteChannel getChannel() throws IOException
        {
            Content content = testContentByNodeId.get(node);
            return content.getChannel();
        }

        @Override
        public InputStream getStream() throws IOException
        {
            Content content = testContentByNodeId.get(node);
            return content.getInputStream();
        }

        @Override
        public Reader getReader() throws IOException
        {
            Content content = testContentByNodeId.get(node);
            return content.getReader();
        }

        @Override
        public MimeType getMimeType()
        {
            return node.getMimeType();
        }

        @Override
        public Long getSize()
        {
            Content content = testContentByNodeId.get(node);
            return content.getSize();
        }
    }

    @Override
    public ContentWriter getWriter(Node node, MimeType mimeType)
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentReader getReader(Node node) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContentReader getReader(Node node, MimeType mimeType)
            throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node applyPatch(String nodeId, long nodeVersion,
            PatchDocument patchDocument) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReadableByteChannel getChannel(Node node) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReadableByteChannel getChannel(Node node, MimeType mimeType)
            throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
