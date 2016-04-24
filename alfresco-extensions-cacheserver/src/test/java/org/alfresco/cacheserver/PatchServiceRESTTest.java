/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.sglover.checksum.Patch;
import org.sglover.checksum.PatchDocument;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;

/**
 * 
 * @author sglover
 *
 */
public class PatchServiceRESTTest
{
//	private static MongodForTestsFactory mongoFactory;

//	@BeforeClass
//	public static void beforeClass() throws Exception
//	{
//        mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
//    }
//    
//	@AfterClass
//	public static void afterClass()
//	{
//        mongoFactory.shutdown();
//	}

//	private ContentDAO contentDAO;
//	private ChecksumDAO checksumDAO;
//	private ContentStore contentStore;
//	private ChecksumService checksumService;
//	private PatchService patchService;

	@Before
	public void before() throws Exception
	{
//        final MongoDbFactory factory = new MongoDbFactory();
//        boolean useEmbeddedMongo = ("true".equals(System.getProperty("useEmbeddedMongo")) ? true : false);
//        if (useEmbeddedMongo)
//        {
//            final Mongo mongo = mongoFactory.newMongo();
//            factory.setMongo(mongo);
//        }
//        else
//        {
//            factory.setMongoURI("mongodb://127.0.0.1:27017");
//            factory.setDbName("test");
//        }
//        final DB db = factory.createInstance();
//
//        CacheServerIdentity cacheServerIdentity = new CacheServerIdentity()
//		{
//			@Override
//			public int getPort()
//			{
//				return 0;
//			}
//			
//			@Override
//			public String getId()
//			{
//				return GUID.generate();
//			}
//			
//			@Override
//			public String getHostname()
//			{
//				return GUID.generate();
//			}
//		};
//		this.contentStore = new ContentStore();
//		long time = System.currentTimeMillis();
//		this.contentDAO = new MongoContentDAO(db, "content" + time, "contentUsage" + time, cacheServerIdentity);
//		this.checksumDAO = new MongoChecksumDAO(db, "checksums" + time);
//		this.checksumService = new ChecksumServiceImpl(checksumDAO);
//		this.patchService = new PatchServiceImpl(contentDAO, checksumService, contentStore);
	}

	public String getAsString(BodyPart bodyPart) throws IOException
	{
        BodyPartEntity bpEntity = (BodyPartEntity)bodyPart.getEntity();
        InputStream in = bpEntity.getInputStream();
        String s = IOUtils.toString(in);
        return s;
	}

	public PatchDocument getPatch(MultiPart resource) throws IOException
	{
	    Integer blockSize = null;
	    Integer matchCount = null;

	    List<Integer> matchedBlocks = null;
	    List<Patch> patches = new LinkedList<>();

        int c = 0;
        InputStream is = null;
        Integer size = null;
        Integer lastMatchIndex = null;

	    // This will iterate the individual parts of the multipart response
	    for (BodyPart bodyPart : resource.getBodyParts())
	    {
//	        if(bodyPart instanceof FormDataMultiPart)
//	        {
//	            System.out.printf(
//	                    "Multipart Body Part [Mime Type: %s]\n",
//	                    bodyPart.getMediaType());

//	            FormDataMultiPart mp = (FormDataMultiPart)bodyPart;
//	            for (BodyPart bodyPart1 : mp.getBodyParts())
//	            {
	                ContentDisposition contentDisposition = bodyPart.getContentDisposition();
//	                if(contentDisposition instanceof FormDataContentDisposition)
//	                {
//	                    FormDataContentDisposition cd = (FormDataContentDisposition)contentDisposition;
	                Map<String, String> parameters = contentDisposition.getParameters();
	                String name = parameters.get("name");
	                MediaType mediaType = bodyPart.getMediaType();
//	                System.out.println("Body Part " + name);

                    if(name.equals("p_size"))
                    {
                        String s = getAsString(bodyPart);
                        size = Integer.parseInt(s);
                        c++;
                    }
                    else if(name.equals("p_last_match_idx"))
                    {
                        String s = getAsString(bodyPart);
                        lastMatchIndex = Integer.parseInt(s);
                        c++;
                    }
                    else if(name.equals("p_stream"))
                    {
                        BodyPartEntity bpEntity = (BodyPartEntity)bodyPart.getEntity();
                        is = bpEntity.getInputStream();
                        c++;
                    }
                    else if(name.equals("p_block_size"))
                    {
                        String s = getAsString(bodyPart);
                        blockSize = Integer.parseInt(s);
                    }
                    else if(name.equals("p_match_count"))
                    {
                        String s = getAsString(bodyPart);
                        matchCount = Integer.parseInt(s);
                    }

                    if(c >= 3)
                    {
                        c = 0;
                        ByteBuffer bb = ByteBuffer.allocate(1024*20); // TODO
                        ReadableByteChannel channel = Channels.newChannel(is);
                        channel.read(bb);
                        bb.flip();
                        byte[] buffer = new byte[bb.limit()];
                        bb.get(buffer);
                        Patch patch = new Patch(lastMatchIndex, size, buffer);
                        patches.add(patch);
                    }
//	                }
//	            }

//	            ByteBuffer bb = ByteBuffer.allocate(1024*20); // TODO
//	            ReadableByteChannel channel = Channels.newChannel(is);
//	            channel.read(bb);
//	            bb.flip();
//	            byte[] buffer = new byte[bb.limit()];
//	            bb.get(buffer);
//	            Patch patch = new Patch(lastMatchIndex, size, buffer);
//	            patches.add(patch);
	        }
//	        else
//	        {
//	            System.out.printf(
//	                    "Embedded Body Part [Mime Type: %s, Length: %s]\n",
//	                    bodyPart.getMediaType(), bodyPart.getContentDisposition().getSize());
//
//	            ContentDisposition contentDisposition = bodyPart.getContentDisposition();
//	            Map<String, String> parameters = contentDisposition.getParameters();
//	            String name = parameters.get("name");
////	            if(contentDisposition instanceof FormDataContentDisposition)
////	            {
////	                FormDataContentDisposition cd = (FormDataContentDisposition)contentDisposition;
////	                String name = cd.getName();
//
//	            Object entity = bodyPart.getEntity();
////	            if(entity instanceof BodyPartEntity)
////	            {
////	                BodyPartEntity bpEntity = (BodyPartEntity)entity;
////	                if(name.equals("p_block_size"))
////	                {
////	                    blockSize = Integer.parseInt((String)entity);
////	                }
////	                else if(name.equals("p_match_count"))
////	                {
////	                    matchCount = Integer.parseInt((String)bodyPart.getEntity());
////	                }
////	                else if(name.equals("p_matched_blocks"))
////	                {
////	                    String matchedBlocksStr = (String)bodyPart.getEntity();
////	                    List<String> l = Arrays.asList(matchedBlocksStr.split(","));
////	                    matchedBlocks = l.stream()
////	                            .filter(s -> s != null && !s.equals(""))
////	                            .map(s -> Integer.parseInt(s))
////	                            .collect(Collectors.toList());
////	                }
////	            }
//	        }
//	    }

	    PatchDocument patchDocument = new PatchDocument(blockSize, matchedBlocks, patches);
	    return patchDocument;
    }

    public PatchDocument getPatchDocument(String hostname, int port, String username, String password,
    		String nodeId, long nodeVersion) throws IOException
    {
		final ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);
		final Client client = Client.create(config);
		client.addFilter(new HTTPBasicAuthFilter(username, password));

		String uri = "http://"
				+ hostname
				+ ":"
				+ port
				+ "/alfresco/api/-default-/private/alfresco/versions/1/patch/"
				+ nodeId
				+ "/"
				+ nodeVersion;

//		List<Patch> patches = new LinkedList<>();

		final WebResource resource = client.resource(uri);

		final MultiPart response = resource.get(MultiPart.class);
		PatchDocument patch = getPatch(response);

//		// This will iterate the individual parts of the multipart response
//		for(BodyPart part : response.getBodyParts())
//		{
//			int lastMatchIndex = (Integer)part.getContent();
//			int size = (Integer)part.getContent();
//			InputStream is = part.getInputStream();
//			Patch patch = new Patch(lastMatchIndex, size, is);
//			patches.add(patch);
//
////		    System.out.printf(
////		            "Embedded Body Part [Mime Type: %s, Length: %s]\n",
////		            part.getContentType(), part.getSize());
//		}

		return patch;
    }

	@Test
	public void test1() throws Exception
	{
		PatchDocument patch = getPatchDocument("localhost", 9198, "admin", "admin", "1d6d409a-7940-4662-a465-82d183e75f24", 2l);
		System.out.println(patch);
	}
}
