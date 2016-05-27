# OLS
Ontology Lookup Service from SPOT at EBI. 

* OLS is currently live at the EBI here http://www.ebi.ac.uk/ols
* A REST API for OLS is described here http://www.ebi.ac.uk/ols/docs/api
* Instruction on how to build a local OLS installation are here http://www.ebi.ac.uk/ols/docs/installation-guide
* Further OLS documentation can be found here http://www.ebi.ac.uk/ols/docs

## Overview

This is the entire codebase for the EBI OLS. OLS has been developed around two key ontology indexes that can be built and used
 independently from the core website. We provide service to build a SOLR index and and a Neo4j index. The SOLR index is used to provide text based queries over the ontologies while the neo4j index is used to query the ontology structure and is the primary driver of the OLS REST API. 
 
 OLS has been developed with the Spring data and Spring boot framework. You can build this project with Maven and the following Spring Boot applications will be available to run. 
 
All of the apps are available under the ols-apps module. 

 * [ols-apps/ols-solr-app](ols-apps/ols-solr-app) - Spring boot application for building a SOLR index for one of more ontologies. Requires access to a SOLR server. 
 * [ols-apps/ols-neo4j-app](ols-apps/ols-neo4j-app) - Spring boot application for building a Neo4j index for one of more ontologies. Builds an embedded neo4j database. You can run a Neo4j server that uses the generated neo4j database. 

To run a complete local OLS installation you will need a mongodb database. This is a lightweight database that used to store all the ontology configuration and application state information. See here for more information http://www.ebi.ac.uk/ols/docs/installation-guide 

 * [ols-apps/ols-config-app](ols-apps/ols-config-app) - Spring boot application for loading config files into the mongodb database. This includes support for reading config files specified using the OBO foundry YAML format. 
 * [ols-apps/ols-loading-app](ols-apps/ols-loading-app) - Spring boot application for that build the complete OLS indexes. This app fetches ontologies specified in the config files, checks if they have changed form a previous download, and if hey have changed it will create all the necessary SOLR and Neo4j indexes. 

 * [ols-web](ols-web) - This will contain the WAR file that can be deployed in tomcat to launch the OLS website and REST API. 
