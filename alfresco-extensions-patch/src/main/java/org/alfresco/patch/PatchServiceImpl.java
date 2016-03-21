/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.patch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.alfresco.checksum.ChecksumService;
import org.alfresco.checksum.NodeChecksums;
import org.alfresco.checksum.Patch;
import org.alfresco.checksum.PatchDocument;
import org.alfresco.contentstore.AbstractContentStore;
import org.alfresco.contentstore.dao.ContentDAO;
import org.alfresco.contentstore.dao.NodeInfo;
import org.apache.commons.lang3.StringUtils;

import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;

/**
 * 
 * @author sglover
 *
 */
public class PatchServiceImpl implements PatchService
{
    private ChecksumService checksumService;
    private ContentStore contentStore;

    public PatchServiceImpl(ChecksumService checksumService, ContentStore contentStore)
    {
        super();
        this.checksumService = checksumService;
        this.contentStore = contentStore;
    }

    @Override
    public PatchDocument getPatch(String nodeId, long nodeVersion)
            throws IOException
    {
        if(contentStore.exists(nodeId, nodeVersion - 1, true))
        {
            // previous version
            NodeChecksums nodeChecksums = checksumService.getChecksums(nodeId,
                    nodeVersion - 1);
            if (nodeChecksums != null)
            {
                // parameters version
                NodeInfo nodeInfo1 = contentDAO.getByNodeId(nodeId,
                        nodeVersion, true);
                String contentPath1 = nodeInfo1.getContentPath();
                FileChannel inChannel = contentStore.getChannel(contentPath1);
                ByteBuffer buffer = ByteBuffer.allocate(1024 * 100);
                inChannel.read(buffer);
                buffer.flip();

                PatchDocument patchDocument = checksumService
                        .createPatchDocument(nodeChecksums, buffer);
                return patchDocument;
            }
            else
            {
                throw new RuntimeException(
                        "No patches available, no checksums for node " + nodeId
                                + ", nodeVersion " + (nodeVersion - 1));
            }
        }
        else
        {
            throw new RuntimeException(
                    "No patches available, only a single version of the node");
        }
    }

    @Override
    public MultiPart getMultiPart(PatchDocument patchDocument)
    {
        MultiPart resource = new MultiPart();
        resource.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        {
            FormDataBodyPart bodyPart = new FormDataBodyPart("p_block_size",
                    String.valueOf(patchDocument.getBlockSize()));
            resource.bodyPart(bodyPart);
        }
        {
            FormDataBodyPart bodyPart = new FormDataBodyPart("p_match_count",
                    String.valueOf(patchDocument.getMatchCount()));
            resource.bodyPart(bodyPart);
        }
        {
            List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
            String matchedBlocksStr = StringUtils.join(matchedBlocks, ",");
            FormDataBodyPart bodyPart = new FormDataBodyPart(
                    "p_matched_blocks", matchedBlocksStr);
            resource.bodyPart(bodyPart);
        }

        for (Patch patch : patchDocument.getPatches())
        {
            int lastMatchIndex = patch.getLastMatchIndex();
            int size = patch.getSize();
            InputStream is = patch.getStream();

            FormDataMultiPart multiPart = new FormDataMultiPart();
            resource.bodyPart(multiPart);

            {
                StreamDataBodyPart bodyPart = new StreamDataBodyPart(
                        "p_stream", is);
                multiPart.bodyPart(bodyPart);
            }

            {
                FormDataBodyPart bodyPart = new FormDataBodyPart("p_size",
                        String.valueOf(size));
                multiPart.bodyPart(bodyPart);
            }

            {
                FormDataBodyPart bodyPart = new FormDataBodyPart(
                        "p_last_match_idx", String.valueOf(lastMatchIndex));
                multiPart.bodyPart(bodyPart);
            }
        }

        return resource;
    }

    @Override
    public FormDataMultiPart getPatchEntity(PatchDocument patchDocument)
            throws IOException
    {
        FormDataMultiPart f = new FormDataMultiPart();

        // MultipartEntityBuilder multipartBuilder =
        // MultipartEntityBuilder.create();
        // multipartBuilder
        // .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        int matchCount = patchDocument.getMatchCount();
        int blockSize = patchDocument.getBlockSize();
        List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
        String matchedBlocksStr = StringUtils.join(matchedBlocks, ",");
        f.field("p_block_size", String.valueOf(blockSize));
        f.field("p_match_count", String.valueOf(matchCount));
        f.field("p_matched_blocks", matchedBlocksStr);

        for (Patch patch : patchDocument.getPatches())
        {
            int lastMatchIndex = patch.getLastMatchIndex();
            int size = patch.getSize();
            InputStream is = patch.getStream();
            // FormBodyPart bodyPart = FormBodyPartBuilder
            // .create("p_stream", new InputStreamBody(is,
            // ContentType.APPLICATION_OCTET_STREAM, ""))
            // .build();
            StreamDataBodyPart bp = new StreamDataBodyPart("p_stream", is);
            f.bodyPart(bp);
            f.field("p_size", String.valueOf(size));
            f.field("p_last_match_idx", String.valueOf(lastMatchIndex));
            // field("p_matched_blocks", matchedBlocksStr);
            // multipartBuilder
            // .addPart(bodyPart)
            // .addTextBody("p_size", String.valueOf(size))
            // .addTextBody("p_last_match_idx", String.valueOf(lastMatchIndex));
        }

        return f;
    }

    // @Override
    // public HttpEntity getPatchEntity(PatchDocument patchDocument) throws
    // IOException
    // {
    // FormDataMultiPart f = new FormDataMultiPart();
    // f.field(name, entity, mediaType)
    //
    // MultipartEntityBuilder multipartBuilder =
    // MultipartEntityBuilder.create();
    // multipartBuilder
    // .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    //
    // int matchCount = patchDocument.getMatchCount();
    // int blockSize = patchDocument.getBlockSize();
    // List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
    // String matchedBlocksStr = StringUtils.join(matchedBlocks, ",");
    // multipartBuilder.addTextBody("p_block_size", String.valueOf(blockSize));
    // multipartBuilder.addTextBody("p_match_count",
    // String.valueOf(matchCount));
    // multipartBuilder.addTextBody("p_matched_blocks", matchedBlocksStr);
    //
    // for(Patch patch : patchDocument.getPatches())
    // {
    // int lastMatchIndex = patch.getLastMatchIndex();
    // int size = patch.getSize();
    // InputStream is = patch.getStream();
    // FormBodyPart bodyPart = FormBodyPartBuilder
    // .create("p_stream", new InputStreamBody(is,
    // ContentType.APPLICATION_OCTET_STREAM, ""))
    // .build();
    // multipartBuilder
    // .addPart(bodyPart)
    // .addTextBody("p_size", String.valueOf(size))
    // .addTextBody("p_last_match_idx", String.valueOf(lastMatchIndex));
    // }
    //
    // HttpEntity entity = multipartBuilder.build();
    // return entity;
    // }

    @Override
    public MultiPart getPatchEntity(String nodeId, long nodeVersion)
            throws IOException
    {
        PatchDocument patchDocument = getPatch(nodeId, nodeVersion);
        return getPatchEntity(patchDocument);
    }

    @SuppressWarnings("resource")
    @Override
    public PatchDocument getPatch(MultiPart resource) throws IOException
    {
        Integer blockSize = null;
        Integer matchCount = null;

        List<Integer> matchedBlocks = null;
        List<Patch> patches = new LinkedList<>();

        // This will iterate the individual parts of the multipart response
        for (BodyPart bodyPart : resource.getBodyParts())
        {
            if (bodyPart instanceof FormDataMultiPart)
            {
                System.out.printf("Multipart Body Part [Mime Type: %s]\n",
                        bodyPart.getMediaType());

                InputStream is = null;
                Integer size = null;
                Integer lastMatchIndex = null;

                FormDataMultiPart mp = (FormDataMultiPart) bodyPart;
                for (BodyPart bodyPart1 : mp.getBodyParts())
                {
                    ContentDisposition contentDisposition = bodyPart1
                            .getContentDisposition();
                    if (contentDisposition instanceof FormDataContentDisposition)
                    {
                        FormDataContentDisposition cd = (FormDataContentDisposition) contentDisposition;
                        String name = cd.getName();

                        if (name.equals("p_size"))
                        {
                            size = Integer.parseInt((String) bodyPart1
                                    .getEntity());
                        }
                        else if (name.equals("p_last_match_idx"))
                        {
                            lastMatchIndex = Integer
                                    .parseInt((String) bodyPart1.getEntity());
                        }
                        else if (name.equals("p_stream"))
                        {
                            is = (InputStream) bodyPart1.getEntity();
                        }
                    }
                }

                ByteBuffer bb = ByteBuffer.allocate(1024 * 20); // TODO
                ReadableByteChannel channel = Channels.newChannel(is);
                channel.read(bb);
                bb.flip();
                byte[] buffer = new byte[bb.limit()];
                bb.get(buffer);
                Patch patch = new Patch(lastMatchIndex, size, buffer);
                patches.add(patch);
            }
            else
            {
                System.out.printf(
                        "Embedded Body Part [Mime Type: %s, Length: %s]\n",
                        bodyPart.getMediaType(), bodyPart
                                .getContentDisposition().getSize());

                ContentDisposition contentDisposition = bodyPart
                        .getContentDisposition();
                if (contentDisposition instanceof FormDataContentDisposition)
                {
                    FormDataContentDisposition cd = (FormDataContentDisposition) contentDisposition;
                    String name = cd.getName();

                    if (name.equals("p_block_size"))
                    {
                        blockSize = Integer.parseInt((String) bodyPart
                                .getEntity());
                    }
                    else if (name.equals("p_match_count"))
                    {
                        matchCount = Integer.parseInt((String) bodyPart
                                .getEntity());
                    }
                    else if (name.equals("p_matched_blocks"))
                    {
                        String matchedBlocksStr = (String) bodyPart.getEntity();
                        List<String> l = Arrays.asList(matchedBlocksStr
                                .split(","));
                        matchedBlocks = l.stream()
                                .filter(s -> s != null && !s.equals(""))
                                .map(s -> Integer.parseInt(s))
                                .collect(Collectors.toList());
                    }
                }
            }
        }

        PatchDocument patchDocument = new PatchDocument(blockSize,
                matchedBlocks, patches);
        return patchDocument;
    }
}
