#!/bin/bash

#  Initialise OLS container; maybe change/increment mongo and solr port nos. (so
#  as not to interfere with local-OS-native equivalents, if running).

# docker run -it -p 8984:8983 -p 27018:27017 ols-debian bash

HOST_SOLR_PORT=8983
HOST_MONGO_PORT=27017

docker run -it \
           -p ${HOST_SOLR_PORT}:8983 \
           -p ${HOST_MONGO_PORT}:27017 \
           -v ols_mongo_data:/data/db \
	   -v ols_autosuggest:/home/.ols/solr/autosuggest \
           -v ols_ontology:/home/.ols/solr/ontology \
           ols-debian bash
