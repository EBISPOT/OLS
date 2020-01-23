#!/bin/bash

#  Make sure you build the ols-solr image before atempting to run it below;
#  by contrast, the mongo image is pulled directly from DockerHub.

#  MongoDB server
docker run -d -p 27017:27017 -v ols_mongo_data:/data/db: mongo:3.4.23-xenial

#  Solr server, including OLS-specific cores
docker run -d -p 8983:8983 \
       -v ols_solr_ontology_data:/opt/mysolrhome/ontology: \
       -v ols_solr_autosuggest_data:/opt/mysolrhome/autosuggest: \
       --name ols-solr-container \
       ols-solr:latest \
       -Dsolr.solr.home=/opt/mysolrhome \
       -Dsolr.data.dir=/opt/solr/server/solr
