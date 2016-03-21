/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.extensions.common.Node;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class ContentGetterImpl implements ContentGetter
{
    private static final Log logger = LogFactory.getLog(ContentGetterImpl.class);

    private String repoUserName;
    private String repoPassword;
    private SessionFactoryImpl cmisFactory;
    private Session cmisSession;

    public ContentGetterImpl(String repoHost, int repoPort, int repoSSLPort,
            String repoUserName, String repoPassword)
    {
        this(repoHost, repoPort, repoUserName, repoPassword);
    }

    public ContentGetterImpl(String repoHost, int repoPort,
            String repoUserName, String repoPassword)
    {
        this.repoUserName = repoUserName;
        this.repoPassword = repoPassword;
        this.cmisFactory = SessionFactoryImpl.newInstance();
    }

    private Session getCMISSession()
    {
        if (this.cmisSession == null)
        {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(SessionParameter.USER, repoUserName);
            parameters.put(SessionParameter.PASSWORD, repoPassword);
            parameters
                    .put(SessionParameter.BROWSER_URL,
                            "http://localhost:8080/alfresco/api/-default-/public/cmis/versions/1.1/browser");
            parameters.put(SessionParameter.BINDING_TYPE,
                    BindingType.BROWSER.value());
            parameters.put(SessionParameter.REPOSITORY_ID, "-default-");

            this.cmisSession = cmisFactory.createSession(parameters);
        }

        return this.cmisSession;
    }

    @Override
    public Content getContentByNodeId(String nodeId, Long nodeVersion)
    {
        Content content = null;

        StringBuilder sb = new StringBuilder(nodeId);
        if (nodeVersion != null)
        {
            sb.append(";");
            sb.append(nodeVersion);
        }
        ObjectId objectId = new ObjectIdImpl(sb.toString());

        Session session = getCMISSession();
        try
        {
            Document document = (Document) session.getObject(objectId);
            if (document != null)
            {
                if (document.isLatestVersion())
                {
                    String mimeType = (String) document.getProperty(
                            PropertyIds.CONTENT_STREAM_MIME_TYPE)
                            .getFirstValue();
                    BigInteger size = (BigInteger) document.getProperty(
                            PropertyIds.CONTENT_STREAM_LENGTH).getFirstValue();
                    ContentStream stream = document.getContentStream();
                    if (stream != null)
                    {
                        InputStream is = stream.getStream();
                        ReadableByteChannel channel = Channels.newChannel(is);
                        content = new Content(channel, size.longValue());
                    }
                }
                else
                {
                    logger.warn("Node " + nodeId + "." + nodeVersion
                            + " not latest version");
                }
            }
            else
            {
                logger.warn("Node " + nodeId + "." + nodeVersion + " not found");
            }
        }
        catch (CmisObjectNotFoundException e)
        {
            logger.warn("Node " + nodeId + "." + nodeVersion + " not found");
        }

        return content;
    }

    // @Override
    // public Content getContentByNodePath(String nodePath)
    // {
    // Content content = null;
    //
    // Session session = getCMISSession();
    // try
    // {
    // Document document = (Document)session.getObjectByPath(nodePath);
    // if(document != null)
    // {
    // if(document.isLatestVersion())
    // {
    // String mimeType =
    // (String)document.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE).getFirstValue();
    // BigInteger size =
    // (BigInteger)document.getProperty(PropertyIds.CONTENT_STREAM_LENGTH).getFirstValue();
    // ContentStream stream = document.getContentStream();
    // if(stream != null)
    // {
    // InputStream is = stream.getStream();
    // content = new Content(is, mimeType, size.longValue());
    // }
    // }
    // else
    // {
    // logger.warn("Node at path " + nodePath + " not latest version");
    // }
    // }
    // else
    // {
    // logger.warn("Node at path " + nodePath + " not found");
    // }
    // }
    // catch(CmisObjectNotFoundException e)
    // {
    // logger.warn("Node at path " + nodePath + " not found");
    // }
    //
    // return content;
    // }
}
