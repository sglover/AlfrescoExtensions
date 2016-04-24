/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sglover.nlp.models.PooledTokenNameFinderModel;
import org.sglover.nlp.models.TextAnnotation;

import com.google.common.base.Joiner;

/**
 * 
 * @author sglover
 *
 */
public class CoreNLPEntityTagger extends AbstractEntityTagger
{
    private static final Log logger = LogFactory.getLog(CoreNLPEntityTagger.class);

    private Map<String, SentenceModel> sentenceModels = new ConcurrentHashMap<>();
    private Map<String, TokenNameFinderModel> tokenNameFinders = new ConcurrentHashMap<>();
    private Map<String, TokenizerModel> tokenizerModels = new ConcurrentHashMap<>();
    private Map<String, POSModel> posModels = new ConcurrentHashMap<>();
    private Map<String, ChunkerModel> chunkerModels = new ConcurrentHashMap<>();

    private ModelLoader modelLoader;

    public static EntityTagger defaultTagger()
    {
		ModelLoader tokenNameFinderLoader = new DefaultModelLoader();
		EntityTagger entityTagger = new CoreNLPEntityTagger(tokenNameFinderLoader, 8);
		return entityTagger;
    }

    public CoreNLPEntityTagger(ModelLoader modelLoader, int numThreads)
    {
    	super(numThreads);

    	this.modelLoader = modelLoader; 

//    	String namesModelFilePath = "/models/en-ner-person.bin";
//    	String datesModelFilePath = "/models/en-ner-date.bin";
//    	String locationsModelFilePath = "/models/en-ner-location.bin";
//    	String orgsModelFilePath = "/models/en-ner-organization.bin";
//    	String moneyModelFilePath = "/models/en-ner-money.bin";
//    	String tokenModelPath = "/models/en-token.bin";
//    	String posModelPath = "/models/en-pos-maxent.bin";
//    	String sentenceModelPath = "/models/en-sent.bin";
//		String chunkerModelPath = "/models/en-chunker.bin";
    	String namesModelFilePath = "models/en-ner-person.bin";
    	String datesModelFilePath = "models/en-ner-date.bin";
    	String locationsModelFilePath = "models/en-ner-location.bin";
    	String orgsModelFilePath = "models/en-ner-organization.bin";
    	String moneyModelFilePath = "models/en-ner-money.bin";
    	String tokenModelPath = "models/en-token.bin";
    	String posModelPath = "models/en-pos-maxent.bin";
    	String sentenceModelPath = "models/en-sent.bin";
		String chunkerModelPath = "models/en-chunker.bin";

        CountDownLatch countDownLatch = new CountDownLatch(9);

        new Thread(new SentenceModelLoader(sentenceModelPath, "en", countDownLatch)).start();

        new Thread(new ChunkerModelLoader(chunkerModelPath, "en", countDownLatch)).start();

        new Thread(new TokenizerModelLoader(tokenModelPath, "en", countDownLatch)).start();

        new Thread(new POSModelLoader(posModelPath, "en", countDownLatch)).start();

        new Thread(new TokenNameFinderLoader(moneyModelFilePath, "money", countDownLatch, tokenNameFinders)).start();
        new Thread(new TokenNameFinderLoader(namesModelFilePath, "name", countDownLatch, tokenNameFinders)).start();
        new Thread(new TokenNameFinderLoader(datesModelFilePath, "date", countDownLatch, tokenNameFinders)).start();
        new Thread(new TokenNameFinderLoader(locationsModelFilePath, "location", countDownLatch, tokenNameFinders)).start();
        new Thread(new TokenNameFinderLoader(orgsModelFilePath, "orgs", countDownLatch, tokenNameFinders)).start();

        try
        {
            countDownLatch.await(15, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        	e.printStackTrace();
        }
    }

    class TokenNameFinderLoader implements Runnable
    {
    	private String modelFilePath;
    	private String type;
    	private CountDownLatch countDownLatch;
    	private Map<String, TokenNameFinderModel> tokenNameFinders;

    	TokenNameFinderLoader(String modelFilePath, String type, CountDownLatch countDownLatch,
    		Map<String, TokenNameFinderModel> tokenNameFinders)
    	{
    		this.modelFilePath = modelFilePath;
    		this.type = type;
    		this.countDownLatch = countDownLatch;
    		this.tokenNameFinders = tokenNameFinders;
    	}

		@Override
        public void run()
        {
	        long start = System.currentTimeMillis();
	        try {
	        	InputStream in = modelLoader.load(modelFilePath);

	        	tokenNameFinders.put(type, new PooledTokenNameFinderModel(in));
	        } catch (IOException e) {
	            logger.error("Error loading model file " + modelFilePath, e);
	        }
	        long end = System.currentTimeMillis();
	        long time = end - start;
	        logger.info("Loaded file " + modelFilePath + " in " + time);
	        countDownLatch.countDown();
        }
    }

    class SentenceModelLoader implements Runnable
    {
    	private String modelFilePath;
        private String type;
        private CountDownLatch countDownLatch;

        public SentenceModelLoader(String modelFilePath, String type, CountDownLatch countDownLatch) {
            this.type = type;
            this.countDownLatch = countDownLatch;
            this.modelFilePath = modelFilePath;
        }

        @Override
        public void run()
        {
            long start = System.currentTimeMillis();

            try
            {
            	InputStream in = modelLoader.load(modelFilePath);
            	sentenceModels.put(type,  new SentenceModel(in));
                long end = System.currentTimeMillis();
                long time = end - start;
                logger.info("Loaded file " + modelFilePath + " in " + time);
            } 
            catch (IOException e)
            {
                logger.error("Error loading model file " + modelFilePath, e);
            }

            countDownLatch.countDown();
        }
    }

    class ClassPathTokenNameFinderLoader implements Runnable
    {
    	private String modelFilePath;
        private String type;
        private CountDownLatch countDownLatch;

        public ClassPathTokenNameFinderLoader(String modelFilePath, String type, CountDownLatch countDownLatch) {
            this.type = type;
            this.countDownLatch = countDownLatch;
            this.modelFilePath = modelFilePath;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try {
            	InputStream in = modelLoader.load(modelFilePath);
            	tokenNameFinders.put(type, new PooledTokenNameFinderModel(in));
            } catch (IOException e) {
                logger.error("Error loading model file " + modelFilePath, e);
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            logger.info("Loaded file " + modelFilePath + " in " + time);
            countDownLatch.countDown();
        }
    }

    class ChunkerModelLoader implements Runnable
    {
    	private String modelFilePath;
        private String type;
        private CountDownLatch countDownLatch;

        public ChunkerModelLoader(String modelFilePath, String type, CountDownLatch countDownLatch) {
            this.type = type;
            this.countDownLatch = countDownLatch;
            this.modelFilePath = modelFilePath;
        }

        @Override
        public void run()
        {
            long start = System.currentTimeMillis();
            try {
            	InputStream in = modelLoader.load(modelFilePath);
            	chunkerModels.put(type,  new ChunkerModel(in));
            } catch (IOException e) {
                logger.error("Error loading model file " + modelFilePath, e);
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            logger.info("Loaded file " + modelFilePath + " in " + time);
            countDownLatch.countDown();
        }
    }

    class POSModelLoader implements Runnable
    {
    	private String modelFilePath;
        private String type;
        private CountDownLatch countDownLatch;

        public POSModelLoader(String modelFilePath, String type, CountDownLatch countDownLatch) {
            this.type = type;
            this.countDownLatch = countDownLatch;
            this.modelFilePath = modelFilePath;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try {
            	InputStream in = modelLoader.load(modelFilePath);
            	posModels.put(type, new POSModel(in));
            } catch (IOException e) {
                logger.error("Error loading model file " + modelFilePath, e);
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            logger.info("Loaded file " + modelFilePath + " in " + time);
            countDownLatch.countDown();
        }
    }

    class TokenizerModelLoader implements Runnable
    {
    	private String tokenModelFilePath;
        private String type;
        private CountDownLatch countDownLatch;

        public TokenizerModelLoader(String tokenModelFilePath, String type, CountDownLatch countDownLatch) {
            this.type = type;
            this.countDownLatch = countDownLatch;
            this.tokenModelFilePath = tokenModelFilePath;
        }

        @Override
        public void run()
        {
            long start = System.currentTimeMillis();
            try
            {
            	InputStream in = modelLoader.load(tokenModelFilePath);
                TokenizerModel tm = new TokenizerModel(in);
            	tokenizerModels.put(type, tm);
            } catch (IOException e) {
                logger.error("Error loading model file " + tokenModelFilePath, e);
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            logger.info("Loaded file " + tokenModelFilePath + " in " + time);
            countDownLatch.countDown();
        }
    }

    public void convertTextAnnotationsToNamedEntities(String[] tokens, List<TextAnnotation> TextAnnotations,
    		Entities namedEntities) {
        for (TextAnnotation textAnnotation : TextAnnotations) {
            int beginOffset = textAnnotation.getSpan().getStart();
            int endOffset = textAnnotation.getSpan().getEnd();
            int contextStart = beginOffset - 2;
            if(contextStart < 0)
            {
            	contextStart = 0;
            }
            int contextEnd = endOffset + 2;
            if(contextEnd >= tokens.length)
            {
            	contextEnd = tokens.length - 1;
            }
            String[] contextData = Arrays.copyOfRange(tokens, contextStart, contextEnd);
            String[] textAnnotationData = Arrays.copyOfRange(tokens, beginOffset, endOffset);
            String context = Joiner.on(" ").join(contextData);
            String content = Joiner.on(" ").join(textAnnotationData);

            String type = textAnnotation.getType();
            double prob = textAnnotation.getProb();

            switch(type)
            {
            case "location":
            {
            	EntityLocation location = new EntityLocation(beginOffset, endOffset, prob, context);
                namedEntities.addLocation(content, location);
            	break;
            }
            case "name":
            {
            	EntityLocation location = new EntityLocation(beginOffset, endOffset, prob, context);
            	namedEntities.addName(content, location);
            	break;
            }
            case "date":
            {
            	EntityLocation location = new EntityLocation(beginOffset, endOffset, prob, context);
                namedEntities.addDate(content, location);
            	break;
            }
            case "orgs":
            {
            	EntityLocation location = new EntityLocation(beginOffset, endOffset, prob, context);
                namedEntities.addOrg(content, location);
            	break;
            }
            case "money":
            {
            	EntityLocation location = new EntityLocation(beginOffset, endOffset, prob, context);
                namedEntities.addMoney(content, location);
            	break;
            }
            default:
            	logger.warn("Don't know how to handle type " + type);
            }

//            namedEntities.get(type).add(content);
        }
    }

    /* Copied from https://github.com/tamingtext/book/blob/master/src/test/java/com/tamingtext/opennlp/NameFinderTest.java */
    private void removeConflicts(List<TextAnnotation> allTextAnnotations) {
        java.util.Collections.sort(allTextAnnotations);
        List<TextAnnotation> stack = new ArrayList<TextAnnotation>();
        stack.add(allTextAnnotations.get(0));
        for (int ai = 1; ai < allTextAnnotations.size(); ai++) {
            TextAnnotation curr = allTextAnnotations.get(ai);
            boolean deleteCurr = false;
            for (int ki = stack.size() - 1; ki >= 0; ki--) {
                TextAnnotation prev = stack.get(ki);
                if (prev.getSpan().equals(curr.getSpan())) {
                    if (prev.getProb() > curr.getProb()) {
                        deleteCurr = true;
                        break;
                    } else {
                        allTextAnnotations.remove(stack.remove(ki));
                        ai--;
                    }
                } else if (prev.getSpan().intersects(curr.getSpan())) {
                    if (prev.getProb() > curr.getProb()) {
                        deleteCurr = true;
                        break;
                    } else {
                        allTextAnnotations.remove(stack.remove(ki));
                        ai--;
                    }
                } else if (prev.getSpan().contains(curr.getSpan())) {
                    break;
                } else {
                    stack.remove(ki);
                }
            }
            if (deleteCurr) {
                allTextAnnotations.remove(ai);
                ai--;
                deleteCurr = false;
            } else {
                stack.add(curr);
            }
        }
    }

    private void findEntities(Entities namedEntities, List<TextAnnotation> allTextAnnotations, String[] tokens)
    {
        for (Map.Entry<String, TokenNameFinderModel> finderEntry : tokenNameFinders.entrySet())
        {
            String type = finderEntry.getKey();
            NameFinderME finder = new NameFinderME(finderEntry.getValue());
            try
            {
	            Span[] spans = finder.find(tokens);
	            double[] probs = finder.probs(spans);
	
	            for (int ni = 0; ni < spans.length; ni++)
	            {
	                allTextAnnotations.add(new TextAnnotation(type, spans[ni], probs[ni]));
	            }
            }
            finally
            {
            	finder.clearAdaptiveData();
            }
        }

        if (allTextAnnotations.size() > 0 )
        {
            removeConflicts(allTextAnnotations);
        }

        convertTextAnnotationsToNamedEntities(tokens, allTextAnnotations, namedEntities);
    }

	@Override
    protected Entities getEntitiesImpl(String content)
    {
        Entities namedEntities = Entities.empty();

		SentenceModel sentenceModel = sentenceModels.get("en");
		SentenceDetector sentenceDetector = new SentenceDetectorME(sentenceModel);
		String[] sentences = sentenceDetector.sentDetect(content);

		TokenizerModel tm = tokenizerModels.get("en");
        TokenizerME wordBreaker = new TokenizerME(tm);

        for(String sentence : sentences)
        {
	        String[] tokens = wordBreaker.tokenize(sentence);

	        List<TextAnnotation> allTextAnnotations = new LinkedList<TextAnnotation>();
	
	        POSModel posModel = posModels.get("en");
	        POSTaggerME posme = new POSTaggerME(posModel);
	        String[] posTags = posme.tag(tokens);

	        List<String> npTokens = new LinkedList<>();

	        ChunkerModel chunkerModel = chunkerModels.get("en");
	        ChunkerME chunkerME = new ChunkerME(chunkerModel);
	        Span[] chunks = chunkerME.chunkAsSpans(tokens, posTags);
	        String[] chunkStrings = Span.spansToStrings(chunks, tokens);
	        for (int i = 0; i < chunks.length; i++)
	        {
        		String chunkString = chunkStrings[i];
        		logger.info("Chunk = " + chunkString + ", type = " + chunks[i].getType());
	        	if (chunks[i].getType().equals("NP"))
	        	{
	        		npTokens.add(chunkString);
	        	}
	        }

//	        findEntities(namedEntities, allTextAnnotations, npTokens.toArray(new String[0]));
	        findEntities(namedEntities, allTextAnnotations, tokens);
        }

        return namedEntities;
    }
}
