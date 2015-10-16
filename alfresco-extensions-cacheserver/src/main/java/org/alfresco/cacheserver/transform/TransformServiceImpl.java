/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.transform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.alfresco.contentstore.AbstractContentStore;
import org.alfresco.extensions.content.ContentReference;
import org.alfresco.extensions.content.MimeType;
import org.alfresco.extensions.transformations.api.TransformRequest;
import org.alfresco.extensions.transformations.api.options.TransformationOptions;
import org.alfresco.extensions.transformations.client.LocalTransformClient;
import org.alfresco.extensions.transformations.client.TransformationCallback;
import org.alfresco.extensions.transformations.client.TransformationClient;


/**
 * 
 * @author sglover
 *
 */
public class TransformServiceImpl implements TransformService
{
	private AbstractContentStore contentStore;
	private TransformationClient client;
	private ExecutorService executor;

	public TransformServiceImpl(AbstractContentStore contentStore, ExecutorService executor)
    {
	    super();
	    this.contentStore = contentStore;
	    this.executor = executor;

		List<String> routers = new ArrayList<String>();
		routers.add("localhost:2551");

//		ClientConfig config = new ClientConfig("localhost", null, routers);
//		this.client = new LocalTransformaClient(config);
		this.client = new LocalTransformClient();
    }


	@Override
	public void transformToText(String path, MimeType mimeType, TransformationCallback callback) throws IOException
	{
		File root = contentStore.getRootDirectory();
		String targetPath = root.getAbsolutePath();

      ContentReference source = new ContentReference(path, mimeType);
      TransformationOptions options = new TransformationOptions();
      options.setMimetype(MimeType.TEXT);
      options.setPath(targetPath);
      TransformRequest request = new TransformRequest(source, options);
      client.transform(request, callback);
	}

	@Override
	public void transformToTextAsync(final String path, final MimeType mimeType,
	        final TransformationCallback callback) throws IOException
	{
	    executor.execute(new Runnable()
	    {
            @Override
            public void run()
            {
                // TODO
                try
                {
                    transformToText(path, mimeType, callback);
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
	    });
	}

//	public InputStream getContent(String contentPath) throws IOException
//	{
//		return contentStore.getContent(contentPath);
//	}
}
