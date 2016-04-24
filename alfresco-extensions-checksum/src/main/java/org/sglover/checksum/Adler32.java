/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.checksum;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.sglover.alfrescoextensions.common.Hasher;

/**
 * 
 * @author sglover
 *
 *         Based on the work here https://github.com/claytongulick/bit-sync
 * 
 */
public class Adler32
{
    private boolean initialized = false;
    private int a;
    private int b;
    private int adler32;
    private int hash;
    private Hasher hasher;

    public Adler32(Hasher hasher)
    {
        this.hasher = hasher;
    }

    public Adler32(ByteBuffer data, int offset, int end, Hasher hasher)
    {
        this(hasher);
        update(data, offset, end);
    }

    public void reset()
    {
        this.initialized = false;
    }

    private void init(ByteBuffer data, int offset, int end)
    {
        a = 0;
        b = 0;

        // adjust the end to make sure we don't exceed the extents of the data.
        if (end >= data.limit())
        {
            end = data.limit() - 1;
        }

        for (int i = offset; i <= end; i++)
        {
            a += data.get(i);
            b += a;
        }

        a %= 65536; // 65536 = 2^16, used for M in the tridgell equation
        b %= 65536;

        this.adler32 = ((b << 16) | a) >>> 0;
        this.hash = hash16(adler32);
    }

    private void update(ByteBuffer data, int offset, int end)
    {
        if(initialized)
        {
            init(data, offset, end);
//            rollingChecksum(data, offset, end);
        }
        else
        {
            init(data, offset, end);
            initialized = true;
        }
    }

    public int getA()
    {
        return a;
    }

    public int getB()
    {
        return b;
    }

    public int getAdler32()
    {
        return adler32;
    }

    public int getHash()
    {
        return hash;
    }

    private int hash16(int num)
    {
        return num % 65536;
    }

    private void rollingChecksum(ByteBuffer data, int offset, int end)
    {
        byte temp = data.get(offset - 1); // this is the first byte used in the
                                          // previous iteration
        this.a = (this.a - temp + data.get(end)) % 65536;
        this.b = (this.b - ((end - offset + 1) * temp) + a) % 65536;
        this.adler32 = (b << 16) | a;
        this.hash = hash16(adler32);
    }

//    private String getHash(ByteBuffer bytes, int start, int end, String hashType)
//            throws NoSuchAlgorithmException
//    {
//        int saveLimit = bytes.limit();
//        bytes.limit(end);
//
//        bytes.mark();
//        bytes.position(start);
//
//        md.reset();
//        md.update(bytes);
//        byte[] array = md.digest();
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < array.length; ++i)
//        {
//            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
//                    1, 3));
//        }
//
//        bytes.limit(saveLimit);
//        bytes.reset();
//
//        return sb.toString();
//    }
//
//    private String md5(ByteBuffer bytes, int start, int end) throws NoSuchAlgorithmException
//    {
//        return getHash(bytes, start, end, "MD5");
//    }

    public int checkMatch(int lastMatchIndex, NodeChecksums documentChecksums, ByteBuffer data, int start, int end)
    {
        update(data, start, end);

        List<Checksum> checksums = documentChecksums.getChecksums(getHash());
        if (checksums == null)
        {
            return -1;
        }

        for (Checksum checksum : checksums)
        {
            // compare adler32sum
            if (checksum.getBlockIndex() >= lastMatchIndex && checksum.getAdler32() == getAdler32())
            {
                // do strong comparison
                try
                {
                    data.mark();
                    String md5sum1 = hasher.md5(data, start, end);
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

    @Override
    public String toString()
    {
        return "Adler32 [a=" + a + ", b=" + b + ", adler32=" + adler32
                + ", hash=" + hash + "]";
    }
}