== OLS-NEO4J

This module builds an app for creating a standalone Neo4J index for one or more ontologies. Details of the OLS Neo4J schema can be found here http://www.ebi.ac.uk/ols/docs/neo4j-schema

This application will build an embedded neo4j database (currently version 2.2.2)

1. Create an ontology configuration file. There are some examples in src/main/resources/*.properties
2. In the project root build the OLS application jars using: mvn clean package
3. Assuming a properties file called application-uberon.properties you can run the application to build the index with: 

```
java -Xmx2g -jar -Dspring.profiles.active=uberon ols-apps/ols-neo4j-app/target/ols-neo4j-app.jar
```

By default this will create a neo4j database in ~/.ols/neo4j. You can overide the location of the neo4j database by supplying the -Dols.home=<path to neo4j database> property. If you want to runa  local neo4j server that uses this database set the
org.neo4j.server.database.location=<ols home>/neo4j property in the neo4j conf/neo4j-server.properties file. 

Once the Neo4J database has been created you can connect a Neo4J server to this database so you can use the Neo4J interface to query and explore the data.

1. Download Neo4J (version 2.2+) from the Neo4J website http://neo4j.com
2. Edit the conf/neo4j-server.properties file to point at the neoj directory built previously

```
e.g. org.neo4j.server.database.location =~/.ols/neo4j
```
3. Start neo4j using 'bin/neo4j start' anbd open your browser to http://localhost:7474

== An example ontology configuration file

application-uberon.properties would include:

```
# The ontology URI
ontology_uri  http://purl.obolibrary.org/obo/uberon.owl

# The full name of the ontology
title  Uber Anatomy Ontology

# The short name for this ontology
namespace UBERON

# The location to download this ontology (can also be local file path e.g. file:/tmp/uberon.owl
location http://purl.obolibrary.org/obo/uberon.owl

# primary term label property
label_property  http://www.w3.org/2000/01/rdf-schema#label

# term definition property (use , for multiple)
definition_property http://purl.obolibrary.org/obo/IAO_0000115

# term synonym property (use , for multiple)
synonym_property    http://www.geneontology.org/formats/oboInOwl#hasExactSynonym

# experimental, can ignore for now
#hierarchical_property   http://purl.obolibrary.org/obo/BFO_0000050

# list any properties where you want to ignore assertions (can be annotation or object properties)
#hidden_property

# Base URIs that are local to this ontology, used to identify terms that are defined in this ontology. 
base_uri    http://purl.obolibrary.org/obo/UBERON_,http://purl.obolibrary.org/obo/UBPROP_,http://purl.obolibrary.org/obo/uberon/core#

# if the ontology needs to be classified first, select a reasoner type. Allowed values are none, EL, OWL2
reasoner EL

# True if the ontology contains OBO style slim annotations
oboSlims true
```
