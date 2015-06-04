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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.cacheserver.contentstore.ContentStore;
import org.alfresco.transformation.api.ContentReference;
import org.alfresco.transformation.api.MimeType;
import org.alfresco.transformation.api.TransformRequest;
import org.alfresco.transformation.api.options.TransformationOptions;
import org.alfresco.transformation.client.TransformationCallback;
import org.alfresco.transformation.client.TransformationClient;
import org.alfresco.transformation.config.ClientConfig;

/**
 * 
 * @author sglover
 *
 */
public class AkkaTransformServiceImpl implements TransformService
{
	private ContentStore contentStore;
	private TransformationClient client;

	public AkkaTransformServiceImpl(ContentStore contentStore)
    {
	    super();
	    this.contentStore = contentStore;

		List<String> routers = new ArrayList<String>();
		routers.add("localhost:2551");

		ClientConfig config = new ClientConfig("localhost", null, routers);
		this.client = new TransformationClient(config);

        // construct transform module
//        List<TransformerConfig> tc = resolvedAppConfig.getTransformers();
//        if (tc != null)
//        {
//            transformModule = new AkkaTransformModule(system, tc, store);
//            transformModule.startup();
//        }
//        
//        // construct router module
//        List<MappedRouteConfig> rc = resolvedAppConfig.getRoutes();
//        if (rc != null)
//        {
//            RoutingStrategy router = RoutingStrategy.ROUNDROBIN;
//            if (serverConfig != null && serverConfig.getRouter() != null)
//            {
//                router = serverConfig.getRouter();
//            }
//            routerModule = new AkkaRouterModul(client, rc, router);
//            routerModule.startup();
//        }
    }

//  private static final String DEFAULT_ROUTERS = "localhost:2551";
  /**
   * @return  construct client config
   */
//  public ClientConfig createClientConfig()
//  {
//      ClientConfig client = new ClientConfig("localhost", 2020, null);
//      return client;
//  }
//  public ContentConfig createContentConfig()
//  {
//      File temp = TempFiles.getTempDir();
//      ContentConfig config = new ContentConfig("file", temp.getAbsolutePath(), null, null, null, null);
//      return config;
//  }
//  public ClusterConfig createClusterConfig()
//  {
//      // construct cluster config
//      List<String> serversList = null;
//      if (servers != null)
//      {
//          String[] separatedSeeds = servers.split(",");
//          serversList = Arrays.asList(separatedSeeds);
//      }
//      List<String> portsList = null;
//      if (ports != null)
//      {
//          String[] separatedPorts = ports.split(",");
//          portsList = Arrays.asList(separatedPorts);
//      }
//      List<String> groupsList = null;
//      if (groups != null)
//      {
//          String[] separatedGroups = groups.split(",");
//          groupsList = Arrays.asList(separatedGroups);
//      }
//      List<String> tagsList = null;
//      if (tags != null)
//      {
//          String[] separatedTags = tags.split(",");
//          tagsList = Arrays.asList(separatedTags);
//      }
//      List<String> zonesList = null;
//      if (zones != null)
//      {
//          String[] separatedZones = zones.split(",");
//          zonesList = Arrays.asList(separatedZones);
//      }
//      ClusterConfig cluster = new ClusterConfig(discovery, serversList, portsList, groupsList, tagsList, zonesList, ec2access, ec2secret);
//      return cluster;
//  }
//  private static final String THUMBNAIL_NAME = "thumbnail";
//  private static final String THUMBNAIL_OPTIONS = "png page_end=1 size_toThumbnail=true size_width=120 size_height=170 size_maintainAspectRatio=true image_resolution=72 image_device=png48";
//  private static final String PREVIEW_NAME = "preview";
//  private static final String PREVIEW_OPTIONS = "png page_split=10 image_resolution=216 image_device=png48 image_depth=8";

	@Override
	public String transform(String path, MimeType mimeType, TransformationCallback callback) throws IOException
	{
//		ClusterConfig clusterConfig = createClusterConfig();
//      NodeDiscovery discovery = NodeDiscoveryFactory.createNodeDiscovery(clusterConfig);
//      List<String> routers = new ArrayList<>();
//      List<String> nodes = discovery.getNodes();
//      if (nodes != null && nodes.size() > 0)
//      {
//          routers.addAll(nodes);
//      }
//      else
//      {
//          routers.add(DEFAULT_ROUTERS);
//      }

//      long startTime = System.currentTimeMillis();
      
//      ClientConfig clientConfig = createClientConfig();

//      TransformationClient client = new TransformationClient(clientConfig);
//      ContentConfig contentConfig = createContentConfig();
//      ContentStoreAdmin store = ContentStoreFactory.createContentStore(contentConfig);

		File root = contentStore.getRootDirectory();
//		File targetFile = contentStore.create();
		String targetPath = root.getAbsolutePath();

//      ClientConfig config = new ClientConfig("localhost", null, routers);
//      TransformationClient client = new TransformationClient(config);
      ContentReference source = new ContentReference(path, mimeType);
      TransformationOptions options = new TransformationOptions();
      options.setMimetype(MimeType.TEXT);
      options.setPath(targetPath);
      TransformRequest request = new TransformRequest(source, options);
      client.transform(request, callback);

      return targetPath;
	}

	public InputStream getContent(String contentPath) throws IOException
	{
		return contentStore.getContent(contentPath);
	}
}
