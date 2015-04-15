/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * 
 * @author sglover
 *
 */
public abstract class AbstractEntityTagger implements EntityTagger
{
    private ListeningExecutorService executorService;

    protected AbstractEntityTagger(int numThreads)
    {
    	executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(numThreads)); 
    }

    protected abstract Entities getEntitiesImpl(String content);

    private void makeCall(final TaggerCall call, final EntityTaggerCallback callback)
    {
        final ListenableFuture<Entities> future = executorService.submit(call);
        Futures.addCallback(future, new FutureCallback<Entities>()
        {
            @Override
            public void onSuccess(Entities entities)
            {
            	callback.onSuccess(entities);
            }

            @Override
            public void onFailure(Throwable ex) 
            {
            	callback.onFailure(ex);
            }
        });
    }

    private class TaggerCall extends Tagger implements Callable<Entities>
    {
        public TaggerCall(String text)
        {
        	super(text);
        }

        public TaggerCall(File file)
        {
        	super(file);
        }

        public TaggerCall(URL url)
        {
        	super(url);
        }

        @Override
        public Entities call() throws Exception
        {
        	return execute();
        }
    }

    private class Tagger
    {
    	private URL url;
    	private File file;
    	private String text;

        public Tagger(String text)
        {
        	this.text = text;
        }

        public Tagger(File file)
        {
        	this.file = file;
        }

        public Tagger(URL url)
        {
    		this.url = url;
        }

        Entities execute() throws IOException
        {
        	if(text != null)
        	{
        		// ok
        	}
        	else if(url != null)
        	{
        		this.text = getText(url);
        	}
        	else if(file != null)
        	{
        		this.text = getText(file.getAbsolutePath());
        	}
        	else
        	{
        		throw new RuntimeException();
        	}

        	Entities entities = getEntitiesImpl(text);
        	return entities;
        }
    }

	// the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
            else if (name.equals("dt"))
                append("  ");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
                append("\n");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "a", "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n");
//            else if (name.equals("a"))
//                append(String.format(" <%s>", node.absUrl("href")));
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            if (text.equals(" ") &&
                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }

    private String getText(String filename) throws IOException
    {
        File input = new File(filename);
        Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(doc); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }
    
    private String getText(URL url) throws IOException
    {
        Document doc = Jsoup.parse(url, 2000);

        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(doc); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }

	@Override
    public Entities getEntities(String text) throws IOException
    {
        final Tagger tagger = new Tagger(text);
        return tagger.execute();
    }

	@Override
    public Entities getEntities(URL url) throws IOException
    {
        final Tagger tagger = new Tagger(url);
        return tagger.execute();
    }

	@Override
    public Entities getEntities(File file) throws IOException
    {
        final Tagger tagger = new Tagger(file);
        return tagger.execute();
    }

	@Override
    public void getEntities(URL url, EntityTaggerCallback callback)
    {
        final TaggerCall call = new TaggerCall(url);
        makeCall(call, callback);
    }

	@Override
    public void getEntities(File file, EntityTaggerCallback callback)
    {
        final TaggerCall call = new TaggerCall(file);
        makeCall(call, callback);
    }
	
	@Override
    public void getEntities(String text, EntityTaggerCallback callback)
    {
        final TaggerCall call = new TaggerCall(text);
        makeCall(call, callback);
    }
}
