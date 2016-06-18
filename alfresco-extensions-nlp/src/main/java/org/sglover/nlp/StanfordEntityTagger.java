/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.nlp;

import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * @author sglover
 *
 */
// TODO probabilities
//@Component(value = "stanfordEntityTagger")
public class StanfordEntityTagger extends AbstractEntityTagger
{
    private static final Log logger = LogFactory.getLog(StanfordEntityTagger.class);

    private StanfordCoreNLP pipeline;

    public static EntityTagger build()
    {
        EntityTagger entityTagger = new StanfordEntityTagger(2);
        return entityTagger;
    }

    public StanfordEntityTagger()
    {
        super(5);
    }

    public StanfordEntityTagger(int numThreads)
    {
        super(numThreads);
    }

    @PostConstruct
    public void init()
    {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization,
        // NER, parsing, and coreference resolution
        Properties props = new Properties();
        // props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse,
        // dcoref");
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.put("pos.maxlen", 100);
        props.put("useNGrams", true);
        props.put("maxNGramLeng", 20);
        this.pipeline = new StanfordCoreNLP(props);
    }

    @Override
    public Entities getEntitiesImpl(String text)
    {
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and
        // has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        Entities entities = Entities.empty();

        for (CoreMap sentence : sentences)
        {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
                int beginOffset = token.get(CharacterOffsetBeginAnnotation.class);
                int endOffset = token.get(CharacterOffsetEndAnnotation.class);

                switch (ne)
                {
                    case "LOCATION":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addLocation(word, location);
                        break;
                    }
                    case "DATE":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addDate(word, location);
                        break;
                    }
                    case "MONEY":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addMoney(word, location);
                        break;
                    }
                    case "PERSON":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addName(word, location);
                        break;
                    }
                    case "ORGANIZATION":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addOrg(word, location);
                        break;
                    }
                    case "MISC":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addMisc(word, location);
                        break;
                    }
                    case "TIME":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addTime(word, location);
                        break;
                    }
                    case "NUMBER":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addNumber(word, location);
                        break;
                    }
                    case "DURATION":
                    {
                        EntityLocation location = new EntityLocation(beginOffset, endOffset, 1.0, "");
                        entities.addDuration(word, location);
                        break;
                    }
                    default:
                    {
    //                    logger.warn("Can't handle type " + ne + ", " + word);
                    }
                }
            }
        }

        return entities;
    }
}
