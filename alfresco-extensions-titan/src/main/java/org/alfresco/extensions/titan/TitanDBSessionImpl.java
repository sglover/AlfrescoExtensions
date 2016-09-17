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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.configuration.ReadConfiguration;
import com.thinkaurelius.titan.diskstorage.configuration.backend.CommonsConfiguration;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;

/**
 * 
 * @author sglover
 *
 */
@Component("tsess")
@Scope("singleton")
public class TitanDBSessionImpl implements TitanDBSession
{
    private static Log logger = LogFactory.getLog(TitanDBSessionImpl.class);

    private Configuration conf;

    private TitanGraph graph;

    @Value("${titan.clear}")
    private boolean clear = false;

//    @Value("${titan.configurationFile}")
    private String titanConfigurationFile = "application.properties";

    public class GraphTransactionImpl implements GraphTransaction
    {
        private Transaction tx;

        GraphTransactionImpl()
        {
            this.tx = getGraph().tx();
        }

        public TitanGraph getTitanGraph()
        {
            return getGraph();
        }

//      return tx.submit(new Function<Graph, T>() {
//      public T apply(Graph g)
//      {
//          return work.execute();
//      }
//  })
//  .exponentialBackoff(5);

        @Override
        public T execute(TxnWork<T> work)
        {
            try
            {
                tx.open();
                return work.execute(this);
            }
            finally
            {
                tx.commit();
            }
        }
    }

    @Override
    public GraphTransaction tx()
    {
        return new GraphTransactionImpl();
    }

//    @Override
//    public Transaction txn()
//    {
//        return graph.tx();
//    }

    public TitanDBSessionImpl(boolean clear, String titanConfigurationFile) throws Exception
    {
        this.clear = clear;
        this.titanConfigurationFile = titanConfigurationFile;
        buildTitanSession();
    }

    public TitanDBSessionImpl()
    {
    }

    @PostConstruct
    public void buildTitanSession() throws Exception
    {
        URL url = getClass().getClassLoader().
                getResource(titanConfigurationFile).toURI().toURL();

        List<Configuration> configs = new LinkedList<>();
        configs.add(new SystemPropsConfiguration());
        configs.add(new PropertiesConfiguration(url));
        this.conf = new CompositeConfiguration(configs);

        if(clear)
        {
            clear();
        }

        this.graph = TitanFactory.open(conf);

//        gremlin();
    }

    public static class SystemPropsConfiguration extends MapConfiguration
    {
        public SystemPropsConfiguration()
        {
            super(System.getProperties());
        }
    }

//    private static void configureMetrics(final Settings.ServerMetrics settings) {
//        final MetricManager metrics = MetricManager.INSTANCE;
//        settings.optionalConsoleReporter().ifPresent(config -> {
//            if (config.enabled) metrics.addConsoleReporter(config.interval);
//        });
//
//        settings.optionalCsvReporter().ifPresent(config -> {
//            if (config.enabled) metrics.addCsvReporter(config.interval, config.fileName);
//        });
//
//        settings.optionalJmxReporter().ifPresent(config -> {
//            if (config.enabled) metrics.addJmxReporter(config.domain, config.agentId);
//        });
//
//        settings.optionalSlf4jReporter().ifPresent(config -> {
//            if (config.enabled) metrics.addSlf4jReporter(config.interval, config.loggerName);
//        });
//
//        settings.optionalGangliaReporter().ifPresent(config -> {
//            if (config.enabled) {
//                try {
//                    metrics.addGangliaReporter(config.host, config.port,
//                            config.optionalAddressingMode(), config.ttl, config.protocol31, config.hostUUID, config.spoof, config.interval);
//                } catch (IOException ioe) {
//                    logger.warn("Error configuring the Ganglia Reporter.", ioe);
//                }
//            }
//        });
//
//        settings.optionalGraphiteReporter().ifPresent(config -> {
//            if (config.enabled) metrics.addGraphiteReporter(config.host, config.port, config.prefix, config.interval);
//        });
//    }

//    private void gremlin() throws Exception
//    {
//        final Settings settings;
//        try {
//            settings = Settings.read(gremlinConfig);
//        } catch (Exception ex) {
//            logger.error("Configuration file at " + gremlinConfig + " could not be found or parsed properly. " + ex.getMessage());
//            return;
//        }
//
//        logger.info("Configuring Gremlin Server from " + gremlinConfig);
//        settings.optionalMetrics().ifPresent(TitanSession::configureMetrics);
//        final GremlinServer server = new GremlinServer(settings);
//        server.start().exceptionally(t -> {
//            logger.error("Gremlin Server was unable to start and will now begin shutdown: " + t.getMessage());
//            server.stop().join();
//            return null;
//        }).join();
//    }

    public void clear() throws BackendException, ConfigurationException, MalformedURLException, URISyntaxException
    {
        ReadConfiguration readConfig = new CommonsConfiguration(conf);
        GraphDatabaseConfiguration graphConfig = new GraphDatabaseConfiguration(readConfig);
        graphConfig.getBackend().clearStorage();
    }

    @PreDestroy
    public void close()
    {
//        graph.close();
    }

    public TitanGraph getGraph()
    {
        return graph;
    }
}
