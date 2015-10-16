/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.checksum.dao.ChecksumDAO;
import org.alfresco.extensions.common.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 *         Based on the work here https://github.com/claytongulick/bit-sync
 * 
 */
public class ChecksumServiceImpl implements ChecksumService
{
    private static Log logger = LogFactory.getLog(ChecksumServiceImpl.class);

    private ChecksumDAO checksumDAO;
    private ExecutorService executors = Executors.newFixedThreadPool(10);
    private int blockSize = 1024 * 10;

    public ChecksumServiceImpl(ChecksumDAO checksumDAO, int blocksize)
    {
        this(checksumDAO);
        this.blockSize = blocksize;
    }

    public ChecksumServiceImpl(ChecksumDAO checksumDAO)
    {
        this.checksumDAO = checksumDAO;
    }

    @SuppressWarnings("resource")
    private FileChannel getChannel(String contentPath) throws IOException
    {
        File file = new File(contentPath);
        FileInputStream fin = new FileInputStream(file);
        FileChannel channel = fin.getChannel();
        return channel;
    }

    private int checkMatch(Adler32 adlerInfo, NodeChecksums documentChecksums,
            ByteBuffer data, int chunkSize)
    {
        List<Checksum> checksums = documentChecksums.getChecksums(adlerInfo
                .getHash());
        if (checksums == null)
        {
            return -1;
        }

        for (Checksum checksum : checksums)
        {
            // compare adler32sum
            if (checksum.getAdler32() == adlerInfo.getChecksum())
            {
                // do strong comparison
                try
                {
                    data.mark();
                    byte[] dst = new byte[chunkSize];
                    data.get(dst, 0, chunkSize);
                    String md5sum1 = md5(dst);
                    data.reset();
                    String md5sum2 = checksum.getMd5();
                    if (md5sum1.equals(md5sum2))
                    {
                        return checksum.getBlockIndex(); // match found, return
                                                         // the matched block
                                                         // index
                    }
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        return -1;
    }

    private String getHash(byte[] bytes, String hashType)
            throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance(hashType);
        byte[] array = md.digest(bytes);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i)
        {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
                    1, 3));
        }
        return sb.toString();
    }

    private String md5(byte[] bytes) throws NoSuchAlgorithmException
    {
        return getHash(bytes, "MD5");
    }

    private Adler32 adler32(int offset, int end, ByteBuffer data)
    {
        int i = 0;
        int a = 0;
        int b = 0;

        // adjust the end to make sure we don't exceed the extents of the data.
        if (end >= data.limit())
        {
            end = data.limit() - 1;
        }

        for (i = offset; i <= end; i++)
        {
            a += data.get(i);
            b += a;
        }

        a %= 65536; // 65536 = 2^16, used for M in the tridgell equation
        b %= 65536;

        return new Adler32(a, b, ((b << 16) | a) >>> 0);
    }

    // public int hash16(int num)
    // {
    // return num % 65536;
    // }

    @Override
    public NodeChecksums getChecksums(String nodeId, long nodeVersion)
    {
        NodeChecksums checksums = checksumDAO.getChecksums(nodeId, nodeVersion);
        return checksums;
    }

    @Override
    public PatchDocument createPatchDocument(NodeChecksums checksums, ReadableByteChannel channel) throws IOException
    {
        // Create a direct ByteBuffer; see also e158 Creating a ByteBuffer
        ByteBuffer buf = ByteBuffer.allocateDirect(500000);
        channel.read(buf);
        buf.flip();
        return createPatchDocument(checksums, buf);
    }

    @Override
    public PatchDocument createPatchDocument(NodeChecksums checksums,
            ByteBuffer data)
    {
        int blockSize = checksums.getBlockSize();

        List<Patch> patches = new LinkedList<>();
        int i = 0;

        Adler32 adlerInfo = null;
        int lastMatchIndex = 0;
        ByteBuffer currentPatch = ByteBuffer.allocate(600000); // TODO

        int currentPatchSize = 0;

        // int matchCount = 0;
        ArrayList<Integer> matchedBlocks = new ArrayList<>(10);

        for (;;)
        {
            int chunkSize = 0;
            // determine the size of the next data chuck to evaluate. Default to
            // blockSize, but clamp to end of data
            if ((i + blockSize) > data.limit())
            {
                chunkSize = data.limit() - i;
                adlerInfo = null; // need to reset this because the rolling
                                  // checksum doesn't work correctly on a final
                                  // non-aligned block
            }
            else
            {
                chunkSize = blockSize;
            }

            if (adlerInfo != null)
            {
                adlerInfo.rollingChecksum(i, i + chunkSize - 1, data);
            }
            else
            {
                adlerInfo = adler32(i, i + chunkSize - 1, data);
                logger.debug("adler32.1:" + i + "," + (i + chunkSize - 1) + ","
                        + adlerInfo.toString());
            }

            // byte[] dst = new byte[chunkSize];
            // data.mark();//position(i);
            // data.get(dst, 0, chunkSize);
            int matchedBlock = checkMatch(adlerInfo, checksums, data, chunkSize);
            if (matchedBlock != -1)
            {
                // if we have a match, do the following:
                // 1) add the matched block index to our tracking buffer
                // 2) check to see if there's a current patch. If so, add it to
                // the patch document.
                // 3) jump forward blockSize bytes and continue
                // matchedBlocksUint32[matchCount] = matchedBlock;
                // matchedBlocks.set(matchCount, matchedBlock);
                // matchedBlocks.ensureCapacity(matchCount + 100);
                matchedBlocks.add(matchedBlock);
                // matchCount++;

                if (currentPatchSize > 0)
                {
                    // there are outstanding patches, add them to the list
                    // create the patch and append it to the patches buffer
                    currentPatch.flip();
                    int size = currentPatch.limit();
                    byte[] dst = new byte[size];
                    currentPatch.get(dst, 0, size);
                    Patch patch = new Patch(lastMatchIndex, size, dst);
                    patches.add(patch);
                }

                lastMatchIndex = matchedBlock;

                i += blockSize;
                if (i >= data.limit() - 1)
                {
                    break;
                }

                adlerInfo = null;

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
            patches.add(patch);
        }

        PatchDocument patchDocument = new PatchDocument(blockSize, /*
                                                                    * matchCount,
                                                                    */
                matchedBlocks, patches);
        return patchDocument;
    }

    @Override
    public int getBlockSize()
    {
        return blockSize;
    }

    @Override
    public void saveChecksums(NodeChecksums checksums)
    {
        checksumDAO.saveChecksums(checksums);
    }

    @Override
    public void extractChecksumsAsync(final Node node, final String contentPath)
    {
        executors.submit(new Runnable()
        {
            @Override
            public void run()
            {
                extractChecksums(node, contentPath);
            }
        });
    }

    @Override
    public NodeChecksums extractChecksums(final Node node,
            final String contentPath)
    {
        try
        {
            final String nodeId = node.getNodeId();
            final Long nodeVersion = node.getNodeVersion();
            final Long nodeInternalId = node.getNodeInternalId();
            final String versionLabel = node.getVersionLabel();

            NodeChecksums documentChecksums = null;

            FileChannel fc = getChannel(contentPath);
            // int blockSize = getBlockSize();

            try
            {
                ByteBuffer data = ByteBuffer.allocate(48);
                int bytesRead = fc.read(data);
                data.flip();

                long numBlocks = data.limit() / blockSize + 1;

                documentChecksums = new NodeChecksums(nodeId, nodeInternalId,
                        nodeVersion, versionLabel, contentPath, blockSize,
                        numBlocks);

                // spin through the data and create checksums for each block
                for (int i = 0; i < numBlocks; i++)
                {
                    int start = i * blockSize;
                    int end = (i * blockSize) + blockSize;

                    // calculate the adler32 checksum
                    Adler32 adlerInfo = adler32(start, end - 1, data);
                    // System.out.println("adler32:" + start + "," + (end - 1) +
                    // "," + adlerInfo.toString());
                    // int checksum = adlerInfo.checksum;
                    // offset++;

                    // calculate the full md5 checksum
                    int chunkLength = blockSize;
                    if ((start + blockSize) > data.limit())
                    {
                        chunkLength = data.limit() - start;
                    }

                    byte[] chunk = new byte[chunkLength];
                    for (int k = 0; k < chunkLength; k++)
                    {
                        chunk[k] = data.get(k + start);
                    }
                    String md5sum = md5(chunk);
                    Checksum checksum = new Checksum(i, adlerInfo.getHash(),
                            adlerInfo.getChecksum(), md5sum);
                    documentChecksums.addChecksum(checksum);
                }
            }
            finally
            {
                if (fc != null)
                {
                    fc.close();
                }
            }

            saveChecksums(documentChecksums);

            return documentChecksums;
        }
        catch (NoSuchAlgorithmException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
