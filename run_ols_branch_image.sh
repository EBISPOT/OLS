#!/bin/bash

#  Initialise OLS container; maybe change/increment mongo and solr port nos. (so
#  as not to interfere with local-OS-native equivalents, if running).

if [ -z "$2" ]; then exit 1; fi
branch=$1
port_inc=$2

HOST_TOMCAT_PORT=$(( 8080 + port_inc ))
HOST_SOLR_PORT=$(( 8983 + port_inc ))
HOST_MONGO_PORT=$(( 27017 + port_inc ))

docker run -it \
           -p ${HOST_TOMCAT_PORT}:8080 \
           -p ${HOST_SOLR_PORT}:8983 \
           -p ${HOST_MONGO_PORT}:27017 \
           -v ols_${branch}_mongo_data:/data/db \
	   -v ols_${branch}_autosuggest:/home/ols/.ols/solr/autosuggest \
           -v ols_${branch}_ontology:/home/ols/.ols/solr/ontology \
           ols-${branch}-debian bash
