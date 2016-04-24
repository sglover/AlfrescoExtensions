/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.nlp;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sglover.nlp.ClasspathModelLoader;
import org.sglover.nlp.CoreNLPEntityTagger;
import org.sglover.nlp.Entities;
import org.sglover.nlp.EntityTagger;
import org.sglover.nlp.ModelLoader;
import org.sglover.nlp.SynchronousEntityTaggerCallback;

/**
 * 
 * @author sglover
 *
 */
public class CoreNLPTest
{
	private EntityTagger tagger;

    @Before
    public void before() throws Exception
    {
//		String namesModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-ner-person.bin";
//		String datesModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-ner-date.bin";
//		String locationsModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-ner-location.bin";
//		String orgsModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-ner-organization.bin";
//		String moneyModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-ner-money.bin";
//		String tokensModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-token.bin";
//		String posModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-pos-maxent.bin";
//		String chunkerModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-chunker.bin";
//		String sentenceModelFilePath = "/Users/sglover/dev/elasticsearch-1.4.0/models/en-sent.bin";	

//    	ModelLoader modelLoader = new DefaultModelLoader();
    	ModelLoader modelLoader = new ClasspathModelLoader();
		this.tagger = new CoreNLPEntityTagger(modelLoader, 2);
    }

	@Test
	public void test1() throws Exception
	{
//    	String text = getText("/Users/sglover/Documents/P. G. Wodehouse - Wikipedia, the free encyclopedia.html");
//    	System.out.println(text);

    	long start = System.currentTimeMillis();

    	File file = new File("/Users/sglover/Documents/P. G. Wodehouse - Wikipedia, the free encyclopedia.html");
    	SynchronousEntityTaggerCallback callback = new SynchronousEntityTaggerCallback();
    	tagger.getEntities(file, callback);
    	Entities entities = callback.getEntities(30000);
    	
    	long end = System.currentTimeMillis();

	    System.out.println(entities != null ? entities.toString() : "null entities");
    	System.out.println((end - start) + "ms");
	}
}
