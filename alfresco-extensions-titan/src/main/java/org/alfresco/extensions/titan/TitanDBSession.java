/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.extensions.titan;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.tinkerpop.gremlin.structure.T;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.diskstorage.BackendException;

/**
 * 
 * @author sglover
 *
 */
public interface TitanDBSession
{
    @SuppressWarnings("hiding")
    public interface TxnWork<T>
    {
        T execute(GraphTransaction gt);
    }

    public interface GraphTransaction
    {
        T execute(TxnWork<T> work);
        TitanGraph getTitanGraph();
    }

    GraphTransaction tx();

    void clear() throws BackendException, ConfigurationException, MalformedURLException, URISyntaxException;
    void close();
    TitanGraph getGraph();
}
