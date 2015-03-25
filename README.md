# AlfrescoExtensions

A collection of libraries for use with Alfresco, including:

Elastic Search Plugin

An ElasticSearch plugin that listens for repository events on an ActiveMQ/Camel topic and indexes metadata, content, named entities and events accordingly.

Pre-requisites
~~~~~~~~~~~~~~

5.0 out of the box
A running ActiveMQ

Configure the repository (apply the extended event generation amp)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

java -jar alfresco-mmt.jar install alfresco-extensions-events-repo-amp/target/amps/alfresco-extensions-events-repo-amp-1.0-SNAPSHOT-0.amp alfresco.war

Configure the repository (alfresco-global.properties)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

messaging.broker.url=failover:(tcp://localhost:61616)?timeout=3000
events.subsystem.autoStart=true
messaging.subsystem.autoStart=true

Install the ElasticSearch Plugin
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# Shut down ElasticSearch
bin/shutdown 

# Remove existing plugin (if installed)
bin/plugin --remove alfresco

# Install the plugin
bin/plugin -install alfresco url file:////...alfresco-elasticsearch-plugin/target/releases/alfresco-elasticsearch-plugin-1.0-SNAPSHOT.zip

# Start ElasticSearch
bin/elasticsearch -d


