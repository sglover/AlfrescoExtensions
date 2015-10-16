/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.alfresco.checksum.Patch;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

/**
 * 
 * @author sglover
 *
 */
public class MultipartTest
{
	@Test
	public void test1() throws Exception
	{
		byte[] b = "Hello world".getBytes();
		InputStream in = new ByteArrayInputStream(b);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder
			.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
			.addBinaryBody("p_stream", in)
			.addTextBody("p_size", String.valueOf(b.length))
			.addTextBody("p_idx", String.valueOf(10));
		HttpEntity entity = builder.build();

		entity.writeTo(System.out);
//		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
//		while ((inputLine = br.readLine()) != null) {
//			System.out.println(inputLine);
//		}
//		br.close();

//		HttpPost httpPost = new HttpPost("http://localhost:2389/TESTME_WITH_NETCAT");
//		httpPost.setEntity(entity);
	}

	private List<Patch> getPatches(String nodeId, long nodeVersion) throws MessagingException, IOException
	{
		List<Patch> patches = new LinkedList<>();

		StringBuilder sb = new StringBuilder();
		sb.append("/patch/");
		sb.append(nodeId);
		sb.append("/");
		sb.append(nodeVersion);
		String url = sb.toString();

		final ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);
		final Client client = Client.create(config);

		final WebResource resource = client.resource(url);
		final MimeMultipart response = resource.get(MimeMultipart.class);

		// This will iterate the individual parts of the multipart response
		for (int i = 0; i < response.getCount(); i++)
		{
		    final BodyPart part = response.getBodyPart(i);
		    System.out.printf(
		            "Embedded Body Part [Mime Type: %s, Length: %s]\n",
		            part.getContentType(), part.getSize());
		    InputStream in = part.getInputStream();
//		    Patch patch = new Patch();
//		    patches.add(patch);
		}

		return patches;
	}
}
