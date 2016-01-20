# OLS-SOLR
This module is a spring boot application for creating a SOLR index from a given ontology. 

You need a local version of SOLR running, we are currently running with version 5.2.1. 

Start SOLR in the example directory with the config supplied by this module 

e.g. 

solr-5.2.1/bin/solr -Dsolr.solr.home=<OLS INSTALL DIR>/ols/ols-solr/src/main/solr-5-config 

You can also optionally set the -Dsolr.data.dir= to a location where the SOLR indexes will get created

Once the SOLR service is running (by default at http://localhost:8983/solr) you can create a new index as follows:

1. Create an ontology configuration file. There are some examples in src/main/resources/*.properties
2. In the project root build the OLS application jars using: mvn clean package
3. Assuming a properties file called application-uberon.properties you can run the application to build the index with: 

java -Xmx2g -jar -Dspring.profiles.active=uberon ols-apps/ols-solr-app/target/ols-solr-app.jar

The config for application-uberon.properties would include:

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
