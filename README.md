# OLS

Ontology Lookup Service from SPOT at EBI.

* OLS is currently live at the EBI here http://www.ebi.ac.uk/ols
* A REST API for OLS is described here http://www.ebi.ac.uk/ols/docs/api
* Instructions on how to build a local OLS installation are here
  http://www.ebi.ac.uk/ols/docs/installation-guide
* Run OLS with docker here
  https://github.com/MaastrichtUniversity/ols-docker
* Further OLS documentation can be found here
  http://www.ebi.ac.uk/ols/docs

## Overview

![OLS Architecture](OLS-Architecture.png)

This is the entire codebase for the EBI OLS. OLS has been developed
around two key ontology indexes that can be built and used independently
from the core website. We provide services to build a Solr index and a
Neo4j index. The Solr index is used to provide text-based queries over
the ontologies while the Neo4j index is used to query the ontology
structure and is the primary driver of the OLS REST API.
 
OLS has been developed with the Spring Data and Spring Boot framework.
You can build this project with Maven and the following Spring Boot
applications will be available to run.
 
All of the apps are available under the ols-apps module.

* [ols-apps/ols-solr-app](ols-apps/ols-solr-app) - Spring Boot
  application for building a Solr index for one or more ontologies.
  Requires access to a Solr server.
* [ols-apps/ols-neo4j-app](ols-apps/ols-neo4j-app) - Spring Boot
  application for building a Neo4j index for one or more ontologies.
  Builds an embedded Neo4j database. You can run a Neo4j server that
  uses the generated Neo4j database.

To run a complete local OLS installation you will need a MongoDB
database. This is a lightweight database that is used to store all the
ontology configuration and application state information. See here for
more information http://www.ebi.ac.uk/ols/docs/installation-guide

* [ols-apps/ols-config-importer](ols-apps/ols-config-importer) - Spring
  Boot application for loading config files into the MongoDB database.
  This includes support for reading config files specified using the OBO
  Foundry YAML format.
* [ols-apps/ols-loading-app](ols-apps/ols-loading-app) - Spring Boot
  application for building the complete OLS indexes. This app fetches
  ontologies specified in the config files, checks whether they have
  changed from a previous download, and if they have changed, will
  create all the necessary Solr and Neo4j indexes.
* [ols-web](ols-web) - This will contain the WAR file that can be
  deployed in Tomcat to launch the OLS website and REST API.
 * [ols-apps/ols-config-importer](ols-apps/ols-config-importer) - Spring boot application for loading config files into the mongodb database. This includes support for reading config files specified using the OBO foundry YAML format. 
 * [ols-apps/ols-loading-app](ols-apps/ols-loading-app) - Spring boot application for that build the complete OLS indexes. This app fetches ontologies specified in the config files, checks if they have changed form a previous download, and if hey have changed it will create all the necessary SOLR and Neo4j indexes. 

 * [ols-web](ols-web) - This contains the WAR file that can be deployed in Tomcat to launch the OLS website and REST API. 
It depends on [ols-term-type-treeview](https://github.com/EBISPOT/ols-term-type-treeview) and [ols-tabbed-term-treeview](https://github.com/EBISPOT/ols-tabbed-term-treeview).

## Buidling OLS
To build OLS you will need to use Java 8 and Maven 3.x.

To build OLS, in the root directory of OLS, run:
`mvn clean package`. Currently this will fail with the following error:

`[ERROR] Failed to execute goal on project ols-neo4j: Could not resolve dependencies for project uk.ac.ebi.spot:ols-neo4j:jar:3.2.1-SNAPSHOT: Failed to collect dependencies at org.springframework.data:spring-data-neo4j:jar:3.4.5.RELEASE -> org.neo4j:neo4j-cypher-dsl:jar:2.0.1: Failed to read artifact descriptor for org.neo4j:neo4j-cypher-dsl:jar:2.0.1: Could not transfer artifact org.neo4j:neo4j-cypher-dsl:pom:2.0.1 from/to maven-neo4j (https://m2.neo4j.org/content/repositories/releases/): Failed to transfer file https://m2.neo4j.org/content/repositories/releases/org/neo4j/neo4j-cypher-dsl/2.0.1/neo4j-cypher-dsl-2.0.1.pom with status code 502 -> [Help 1]`

To correct this, copy the contents of the `build-fix` directory into your Maven 
repository under `~/m2/repository`.

Run `mvn clean package` again. OLS should now build successfully. 

### Other build errors
Other build errors you may come across are the following:

1. Wrong version of Java used:

`[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile (default-compile) on project ols-solr: Compilation failure: Compilation failure: 
 [ERROR] /Users/james/OLS/ols-solr/src/main/java/uk/ac/ebi/spot/ols/config/SolrContext.java:[15,24] package javax.annotation does not exist
 [ERROR] /Users/james/OLS/ols-solr/src/main/java/uk/ac/ebi/spot/ols/config/SolrContext.java:[25,4] cannot find symbol
 [ERROR]  symbol:  class Resource
 [ERROR]  location: class uk.ac.ebi.spot.ols.config.SolrContext
 [ERROR] -> [Help 1]` 
 
This is the error you get when you compile OLS with Java 11. The fix for this 
build error is to ensure your Maven installation is indeed using Java 8 for 
compilation.  

