/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class AlfrescoApiImpl implements AlfrescoApi
{
    private static final Log logger = LogFactory.getLog(AlfrescoApiImpl.class);

    private String repoHost = "localhost";
    private int repoPort = 8080;
    private String repoUserName = "admin";
    private String repoPassword = "admin";

    public AlfrescoApiImpl(String repoHost, int repoPort, String repoUserName, String repoPassword)
	{
    	this.repoHost = repoHost;
    	this.repoPort = repoPort;
		this.repoUserName = repoUserName;
		this.repoPassword = repoPassword;
	}

	@Override
	public NodeId getObjectIdForNodePath(String nodePath)
	{
		Session session = getCMISSession();
		CmisObject cmisObject = session.getObjectByPath(nodePath);
		String objectId = cmisObject.getId();
		int idx = objectId.indexOf(";");
		String nodeId = null;
		String nodeVersion = null;
		if(idx == -1)
		{
			nodeId = objectId;
		}
		else
		{
			nodeId = objectId.substring(0, idx - 1);
			nodeVersion = objectId.substring(idx + 1);
		}

		NodeId ret = new NodeId(nodeId, nodeVersion);
		return ret;
	}

	public String getPrimaryNodePathForNodeId(String nodeId, String nodeVersion)
	{
		ObjectId objectId = new ObjectIdImpl(nodeId + ";" + nodeVersion);
		Document document = (Document)getCMISSession().getObject(objectId);
		List<String> paths = document.getPaths();
		String nodePath = null;
		if(paths != null && paths.size() > 0)
		{
			nodePath = paths.get(0);
		}

		return nodePath;
	}

	private Session getCMISSession()
	{
		SessionFactoryImpl sessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameters = new HashMap<>();

		parameters.put(SessionParameter.USER, repoUserName);
		parameters.put(SessionParameter.PASSWORD, repoPassword);
		parameters.put(SessionParameter.BROWSER_URL, "http://" + repoHost + ":"
				+ repoPort + "/alfresco/api/-default-/public/cmis/versions/1.1/browser");
		parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
		parameters.put(SessionParameter.REPOSITORY_ID, "-default-");

		Session cmisSession = sessionFactory.createSession(parameters);
		return cmisSession;
	}
}
