/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp.models;

import opennlp.tools.util.Span;

public class TextAnnotation implements Comparable<TextAnnotation> {
    private Span span;
    private String type;
    private double prob;

    public TextAnnotation(String type, Span span, double prob) {
        this.span = span;
        this.type = type;
        this.prob = prob;
    }

    public Span getSpan() {
        return span;
    }

    public String getType() {
        return type;
    }

    public double getProb() {
        return prob;
    }

    public int compareTo(TextAnnotation a) {
        int c = span.compareTo(a.span);
        if (c == 0) {
            c = Double.compare(prob, a.prob);
            if (c == 0) {
                c = type.compareTo(a.type);
            }
        }
        return c;
    }

    public String toString() {
        return type + " " + span + " " + prob;
    }
}