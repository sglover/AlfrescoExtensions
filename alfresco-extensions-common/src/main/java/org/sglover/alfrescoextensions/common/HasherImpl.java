/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

/**
 * 
 * @author sglover
 *
 */
@Component
public class HasherImpl implements Hasher
{
    private MessageDigest md5;

    public HasherImpl() throws NoSuchAlgorithmException
    {
        md5 = MessageDigest.getInstance("MD5");
    }

    private String getHash(ByteBuffer bytes, int start, int end, MessageDigest digest)
            throws NoSuchAlgorithmException
    {
        int saveLimit = bytes.limit();
        bytes.limit(end + 1);

        bytes.mark();
        bytes.position(start);

        digest.reset();
        digest.update(bytes);
        byte[] array = digest.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i)
        {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
                    1, 3));
        }

        bytes.limit(saveLimit);
        bytes.reset();

        return sb.toString();
    }

    @Override
    public String md5(ByteBuffer bytes, int start, int end) throws NoSuchAlgorithmException
    {
        return getHash(bytes, start, end, md5);
    }
}
