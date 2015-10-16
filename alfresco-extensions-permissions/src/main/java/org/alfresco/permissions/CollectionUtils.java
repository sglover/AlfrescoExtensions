/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.permissions;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 
 * @author sglover
 *
 */
public class CollectionUtils
{
    private CollectionUtils() { }

    /**
     * Converts an {@link java.util.Iterator} to {@link java.util.stream.Stream}.
     */
    public static <T> Stream<T> iterate(Iterator<? extends T> iterator) {
        int characteristics = Spliterator.ORDERED | Spliterator.IMMUTABLE;
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), false);
    }

    /**
     * Zips the specified stream with its indices.
     */
    public static <T> Stream<Map.Entry<Integer, T>> zipWithIndex(Stream<? extends T> stream, int startIndex) {
        return iterate(new Iterator<Map.Entry<Integer, T>>() {
            private final Iterator<? extends T> streamIterator = stream.iterator();
            private int index = startIndex;

            @Override
            public boolean hasNext() {
                return streamIterator.hasNext();
            }

            @Override
            public Map.Entry<Integer, T> next() {
                return new AbstractMap.SimpleImmutableEntry<>(index++, streamIterator.next());
            }
        });
    }

    /**
     * Returns a stream consisting of the results of applying the given two-arguments function to the elements of this stream.
     * The first argument of the function is the element index and the second one - the element value. 
     */
    public static <T, R> Stream<R> mapWithIndex(Stream<? extends T> stream, BiFunction<Integer, ? super T, ? extends R> mapper,
    		int startIndex) {
        return zipWithIndex(stream, startIndex).map(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }
}