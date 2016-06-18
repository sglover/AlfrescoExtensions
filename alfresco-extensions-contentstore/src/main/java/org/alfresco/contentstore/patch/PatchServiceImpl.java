/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.patch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.AbstractList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.alfresco.contentstore.protobuf.PatchDocumentProtos;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.alfrescoextensions.common.Hasher;
import org.sglover.alfrescoextensions.common.Node;
import org.sglover.checksum.Adler32;
import org.sglover.checksum.NodeChecksums;
import org.sglover.checksum.Patch;
import org.sglover.checksum.PatchDocument;
import org.sglover.checksum.PatchDocumentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;

/**
 * 
 * @author sglover
 *
 */
@Component
public class PatchServiceImpl implements PatchService
{
    private static Log logger = LogFactory.getLog(PatchServiceImpl.class);

    @Value("${content.blocksize}")
    private int blockSize;

    @Autowired
    private Hasher hasher;

    public PatchServiceImpl()
    {
    }

    public PatchServiceImpl(Hasher hasher, int blockSize)
    {
        super();
        this.hasher = hasher;
        this.blockSize = blockSize;
    }

    @PostConstruct
    public void init()
    {
    }

    @Override
    public void getPatch(PatchDocument patchDocument, NodeChecksums nodeChecksums, ReadableByteChannel inChannel)
            throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 100);
        inChannel.read(buffer);
        buffer.flip();

        updatePatchDocument(patchDocument, nodeChecksums, buffer);
    }

//    @Override
//    public MultiPart getMultiPart(PatchDocument patchDocument)
//    {
//        MultiPart resource = new MultiPart();
//        resource.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
//        {
//            FormDataBodyPart bodyPart = new FormDataBodyPart("p_block_size",
//                    String.valueOf(patchDocument.getBlockSize()));
//            resource.bodyPart(bodyPart);
//        }
//        {
//            FormDataBodyPart bodyPart = new FormDataBodyPart("p_match_count",
//                    String.valueOf(patchDocument.getMatchCount()));
//            resource.bodyPart(bodyPart);
//        }
//        {
//            List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
//            String matchedBlocksStr = StringUtils.join(matchedBlocks, ",");
//            FormDataBodyPart bodyPart = new FormDataBodyPart(
//                    "p_matched_blocks", matchedBlocksStr);
//            resource.bodyPart(bodyPart);
//        }
//
//        for (Patch patch : patchDocument.getPatches())
//        {
//            int lastMatchIndex = patch.getLastMatchIndex();
//            int size = patch.getSize();
//            InputStream is = patch.getStream();
//
//            FormDataMultiPart multiPart = new FormDataMultiPart();
//            resource.bodyPart(multiPart);
//
//            {
//                StreamDataBodyPart bodyPart = new StreamDataBodyPart(
//                        "p_stream", is);
//                multiPart.bodyPart(bodyPart);
//            }
//
//            {
//                FormDataBodyPart bodyPart = new FormDataBodyPart("p_size",
//                        String.valueOf(size));
//                multiPart.bodyPart(bodyPart);
//            }
//
//            {
//                FormDataBodyPart bodyPart = new FormDataBodyPart(
//                        "p_last_match_idx", String.valueOf(lastMatchIndex));
//                multiPart.bodyPart(bodyPart);
//            }
//        }
//
//        return resource;
//    }
//
//    @Override
//    public FormDataMultiPart getPatchEntity(PatchDocument patchDocument)
//            throws IOException
//    {
//        FormDataMultiPart f = new FormDataMultiPart();
//
//        // MultipartEntityBuilder multipartBuilder =
//        // MultipartEntityBuilder.create();
//        // multipartBuilder
//        // .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//        int matchCount = patchDocument.getMatchCount();
//        int blockSize = patchDocument.getBlockSize();
//        List<Integer> matchedBlocks = patchDocument.getMatchedBlocks();
//        String matchedBlocksStr = StringUtils.join(matchedBlocks, ",");
//        f.field("p_block_size", String.valueOf(blockSize));
//        f.field("p_match_count", String.valueOf(matchCount));
//        f.field("p_matched_blocks", matchedBlocksStr);
//
//        for (Patch patch : patchDocument.getPatches())
//        {
//            int lastMatchIndex = patch.getLastMatchIndex();
//            int size = patch.getSize();
//            InputStream is = patch.getStream();
//            // FormBodyPart bodyPart = FormBodyPartBuilder
//            // .create("p_stream", new InputStreamBody(is,
//            // ContentType.APPLICATION_OCTET_STREAM, ""))
//            // .build();
//            StreamDataBodyPart bp = new StreamDataBodyPart("p_stream", is);
//            f.bodyPart(bp);
//            f.field("p_size", String.valueOf(size));
//            f.field("p_last_match_idx", String.valueOf(lastMatchIndex));
//            // field("p_matched_blocks", matchedBlocksStr);
//            // multipartBuilder
//            // .addPart(bodyPart)
//            // .addTextBody("p_size", String.valueOf(size))
//            // .addTextBody("p_last_match_idx", String.valueOf(lastMatchIndex));
//        }
//
//        return f;
//    }

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

//    @Override
//    public MultiPart getPatchEntity(NodeChecksums nodeChecksums, ReadableByteChannel channel)
//            throws IOException
//    {
//        PatchDocument patchDocument = new PatchDocumentImpl();
//        getPatch(patchDocument, nodeChecksums, channel);
//        return getPatchEntity(patchDocument);
//    }

    private PatchDocumentProtos.PatchDocument buildProtocolBuffer(PatchDocument patchDocument)
    {
        PatchDocumentProtos.PatchDocument.Builder builder = PatchDocumentProtos.PatchDocument.newBuilder();
        builder.setBlockSize(patchDocument.getBlockSize());
        builder.setNodeId(patchDocument.getNode().getId());
        for(int matchedBlock : patchDocument.getMatchedBlocks())
        {
            builder.addMatchedBlocks(matchedBlock);
        }
        for(Patch patch : patchDocument.getPatches())
        {
            PatchDocumentProtos.PatchDocument.Patch.Builder patchBuilder = PatchDocumentProtos.PatchDocument.Patch.newBuilder();
            patchBuilder.setLastMatchIndex(patch.getLastMatchIndex());
            patchBuilder.setSize(patch.getSize());
            patchBuilder.setBuffer(ByteString.copyFrom(patch.getBuffer()));
            builder.addPatches(patchBuilder);
        }
        PatchDocumentProtos.PatchDocument patchDocumentProtoBuf = builder.build();
        return patchDocumentProtoBuf;
    }

    @Override
    public void writePatch(Node node, PatchDocument patchDocument, OutputStream out) throws IOException
    {
        PatchDocumentProtos.PatchDocument patchDocumentProtoBuf = buildProtocolBuffer(patchDocument);
        patchDocumentProtoBuf.writeTo(out);
    }

    private PatchDocument fromProtoBuf(PatchDocumentProtos.PatchDocument protoBuf)
    {
        List<Patch> patches = new AbstractList<Patch>()
        {
            @Override
            public Patch get(int index)
            {
                PatchDocumentProtos.PatchDocument.Patch patchProtoBuf = protoBuf.getPatchesList().get(index);
                Patch patch = new Patch(patchProtoBuf.getLastMatchIndex(), patchProtoBuf.getSize(),
                        patchProtoBuf.getBuffer().toByteArray());
                return patch;
            }

            @Override
            public int size()
            {
                return protoBuf.getPatchesList().size();
            }
        };

        String nodeId = protoBuf.getNodeId();
        Node node = Node.fromNodeId(nodeId);
        PatchDocument patchDocument = new PatchDocumentImpl(node, protoBuf.getBlockSize(), 
                protoBuf.getMatchedBlocksList(), patches);
        return patchDocument;
    }

    @Override
    public PatchDocument getPatch(InputStream in) throws IOException
    {
        PatchDocumentProtos.PatchDocument patchDocumentProtoBuf = PatchDocumentProtos.PatchDocument.parseFrom(in);
        PatchDocument patchDocument = fromProtoBuf(patchDocumentProtoBuf);
        return patchDocument;
    }

//    @SuppressWarnings("resource")
//    @Override
//    public PatchDocumentImpl getPatch(MultiPart resource) throws IOException
//    {
//        Integer blockSize = null;
//        Integer matchCount = null;
//
//        List<Integer> matchedBlocks = null;
//        List<Patch> patches = new LinkedList<>();
//
//        // This will iterate the individual parts of the multipart response
//        for (BodyPart bodyPart : resource.getBodyParts())
//        {
//            if (bodyPart instanceof FormDataMultiPart)
//            {
//                System.out.printf("Multipart Body Part [Mime Type: %s]\n",
//                        bodyPart.getMediaType());
//
//                InputStream is = null;
//                Integer size = null;
//                Integer lastMatchIndex = null;
//
//                FormDataMultiPart mp = (FormDataMultiPart) bodyPart;
//                for (BodyPart bodyPart1 : mp.getBodyParts())
//                {
//                    ContentDisposition contentDisposition = bodyPart1
//                            .getContentDisposition();
//                    if (contentDisposition instanceof FormDataContentDisposition)
//                    {
//                        FormDataContentDisposition cd = (FormDataContentDisposition) contentDisposition;
//                        String name = cd.getName();
//
//                        if (name.equals("p_size"))
//                        {
//                            size = Integer.parseInt((String) bodyPart1
//                                    .getEntity());
//                        }
//                        else if (name.equals("p_last_match_idx"))
//                        {
//                            lastMatchIndex = Integer
//                                    .parseInt((String) bodyPart1.getEntity());
//                        }
//                        else if (name.equals("p_stream"))
//                        {
//                            is = (InputStream) bodyPart1.getEntity();
//                        }
//                    }
//                }
//
//                ByteBuffer bb = ByteBuffer.allocate(1024 * 20); // TODO
//                ReadableByteChannel channel = Channels.newChannel(is);
//                channel.read(bb);
//                bb.flip();
//                byte[] buffer = new byte[bb.limit()];
//                bb.get(buffer);
//                Patch patch = new Patch(lastMatchIndex, size, buffer);
//                patches.add(patch);
//            }
//            else
//            {
//                System.out.printf(
//                        "Embedded Body Part [Mime Type: %s, Length: %s]\n",
//                        bodyPart.getMediaType(), bodyPart
//                                .getContentDisposition().getSize());
//
//                ContentDisposition contentDisposition = bodyPart
//                        .getContentDisposition();
//                if (contentDisposition instanceof FormDataContentDisposition)
//                {
//                    FormDataContentDisposition cd = (FormDataContentDisposition) contentDisposition;
//                    String name = cd.getName();
//
//                    if (name.equals("p_block_size"))
//                    {
//                        blockSize = Integer.parseInt((String) bodyPart
//                                .getEntity());
//                    }
//                    else if (name.equals("p_match_count"))
//                    {
//                        matchCount = Integer.parseInt((String) bodyPart
//                                .getEntity());
//                    }
//                    else if (name.equals("p_matched_blocks"))
//                    {
//                        String matchedBlocksStr = (String) bodyPart.getEntity();
//                        List<String> l = Arrays.asList(matchedBlocksStr
//                                .split(","));
//                        matchedBlocks = l.stream()
//                                .filter(s -> s != null && !s.equals(""))
//                                .map(s -> Integer.parseInt(s))
//                                .collect(Collectors.toList());
//                    }
//                }
//            }
//        }
//
//        PatchDocumentImpl patchDocument = new PatchDocumentImpl(blockSize,
//                matchedBlocks, patches);
//        return patchDocument;
//    }

    @Override
    public void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, InputStream in) throws IOException
    {
        Reader reader = new InputStreamAsReader(in);
        updatePatchDocument(patchDocument, checksums, reader);
    }

    @Override
    public void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ReadableByteChannel channel) throws IOException
    {
        Reader reader = new ReadableByteChannelAsReader(channel);
        updatePatchDocument(patchDocument, checksums, reader);
    }

    private void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, Reader reader) throws IOException
    {
        ByteBuffer data = ByteBuffer.allocate(blockSize * 20);

        int blockSize = checksums.getBlockSize();

        int i = 0;

        Adler32 adlerInfo = new Adler32(hasher);
        int lastMatchIndex = 1; // starts at 1
        ByteBuffer currentPatch = ByteBuffer.allocate(5000000); // TODO

        int x = 0;

        for (;;)
        {
            if(x == 0 || i >= data.limit())
            {
                data.clear();
                i = 0;
                int numRead = reader.read(data);
                if(numRead <= 0)
                {
                    break;
                }
                data.flip();
                x += numRead;
            }

            int chunkSize = 0;
            // determine the size of the next data chuck to evaluate. Default to
            // blockSize, but clamp to end of data
            if ((i + blockSize) > data.limit())
            {
                chunkSize = data.limit() - i;
                adlerInfo.reset(); // need to reset this because the rolling
                                  // checksum doesn't work correctly on a final
                                  // non-aligned block
            }
            else
            {
                chunkSize = blockSize;
            }

            int end = i + chunkSize - 1;

            int matchedBlockIndex = adlerInfo.checkMatch(lastMatchIndex, checksums, data, i, end);
            if (matchedBlockIndex != -1)
            {
//                try
//                {
//                    String y = hasher.md5(data, i, end);
//                    System.out.println("y = " + y + ", x = " + x + ", i = " + i + ", end = " + end);
//                }
//                catch (NoSuchAlgorithmException e)
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }

                // if we have a match, do the following:
                // 1) add the matched block index to our tracking buffer
                // 2) check to see if there's a current patch. If so, add it to
                // the patch document.
                // 3) jump forward blockSize bytes and continue
                patchDocument.addMatchedBlock(matchedBlockIndex);

                if (currentPatch.position() > 0)
                {
                    // there are outstanding patches, add them to the list
                    // create the patch and append it to the patches buffer
                    currentPatch.flip();
                    int size = currentPatch.limit();
                    byte[] dst = new byte[size];
                    currentPatch.get(dst, 0, size);
                    Patch patch = new Patch(lastMatchIndex, size, dst);
                    patchDocument.addPatch(patch);
                    currentPatch.clear();
                }

                lastMatchIndex = matchedBlockIndex;

                i += chunkSize;

                adlerInfo.reset();
            }
            else
            {
                // while we don't have a block match, append bytes to the
                // current patch
                if(currentPatch.position() >= currentPatch.limit())
                {
//                    System.out.println("count=" + (x + i));
//                    System.out.println("count1=" + currentPatch.position() + ", " + currentPatch.limit());
//                    System.out.println(matchedBlockIndexes);
//                    System.out.println(patches);
                }
                currentPatch.put(data.get(i));
                i++;
            }
        } // end for each byte in the data

        if (currentPatch.position() > 0)
        {
            currentPatch.flip();
            int size = currentPatch.limit();
            byte[] dst = new byte[size];
            currentPatch.get(dst, 0, size);
            Patch patch = new Patch(lastMatchIndex, size, dst);
            patchDocument.addPatch(patch);
        }
    }

    @Override
    public void updatePatchDocument(PatchDocument patchDocument, NodeChecksums checksums, ByteBuffer data)
    {
        int blockSize = checksums.getBlockSize();

        patchDocument.setBlockSize(blockSize);

        int i = 0;

        Adler32 adlerInfo = new Adler32(hasher);
        int lastMatchIndex = 0;
        ByteBuffer currentPatch = ByteBuffer.allocate(600000); // TODO

        int currentPatchSize = 0;

        for (;;)
        {
            int chunkSize = 0;
            // determine the size of the next data chuck to evaluate. Default to
            // blockSize, but clamp to end of data
            if ((i + blockSize) > data.limit())
            {
                chunkSize = data.limit() - i;
                adlerInfo.reset(); // need to reset this because the rolling
                                  // checksum doesn't work correctly on a final
                                  // non-aligned block
            }
            else
            {
                chunkSize = blockSize;
            }

            int matchedBlock = adlerInfo.checkMatch(lastMatchIndex, checksums, data, i, i + chunkSize - 1);
            if (matchedBlock != -1)
            {
//                try
//                {
//                    String y = hasher.md5(data, i, i + chunkSize - 1);
//                    System.out.println("y = " + y);
//                }
//                catch (NoSuchAlgorithmException e)
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
                // if we have a match, do the following:
                // 1) add the matched block index to our tracking buffer
                // 2) check to see if there's a current patch. If so, add it to
                // the patch document.
                // 3) jump forward blockSize bytes and continue
                patchDocument.addMatchedBlock(matchedBlock);

                if (currentPatchSize > 0)
                {
                    // there are outstanding patches, add them to the list
                    // create the patch and append it to the patches buffer
                    currentPatch.flip();
                    int size = currentPatch.limit();
                    byte[] dst = new byte[size];
                    currentPatch.get(dst, 0, size);
                    Patch patch = new Patch(lastMatchIndex, size, dst);
                    patchDocument.addPatch(patch);
                    currentPatch.clear();
                }

                lastMatchIndex = matchedBlock;

                i += chunkSize;

                adlerInfo.reset();

                continue;
            }
            else
            {
                // while we don't have a block match, append bytes to the
                // current patch
                logger.debug("limit = " + currentPatch.limit()
                        + ", position = " + currentPatch.position());
                currentPatch.put(data.get(i));
                currentPatchSize++;
            }
            if (i >= data.limit() - 1)
            {
                break;
            }
            i++;
        } // end for each byte in the data

        if (currentPatchSize > 0)
        {
            currentPatch.flip();
            int size = currentPatch.limit();
            byte[] dst = new byte[size];
            currentPatch.get(dst, 0, size);
            Patch patch = new Patch(lastMatchIndex, size, dst);
            patchDocument.addPatch(patch);
        }
    }

    private interface Reader
    {
        int read(ByteBuffer bb) throws IOException;
    }

    private class InputStreamAsReader implements Reader
    {
        private InputStream in;

        InputStreamAsReader(InputStream in)
        {
            this.in = in;
        }

        @Override
        public int read(ByteBuffer bb) throws IOException
        {
            byte[] bytes = new byte[bb.remaining()];
            int numRead = in.read(bytes);
            bb.put(bytes);
            return numRead;
        }
    }

    private class ReadableByteChannelAsReader implements Reader
    {
        private ReadableByteChannel channel;

        ReadableByteChannelAsReader(ReadableByteChannel channel)
        {
            this.channel = channel;
        }

        @Override
        public int read(ByteBuffer bb) throws IOException
        {
            return channel.read(bb);
        }
    }
}
