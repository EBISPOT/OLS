#!/bin/bash

#  Initialise OLS container and change/increment MongoDB and Solr port
#  nos. (so as not to interfere e.g. with local-OS-native equivalents,
#  if running). If you don't want to specify a commit id, ensure that
#  you enter parameter 2 as the empty string (""), and the run should
#  default to the latest image having the name part corresponding to
#  parameter 1. You need to enter a value or at least the empty string
#  for parameter 2, because parameter 3 (integer increment for port
#  nos.) is _required_; without it this script will exit on error.
#
#  Parameter 4 is "tomcat" if you have already loaded the OLS data and
#  are ready to run the full OLS web application. Otherwise leave blank.

[ -z "$3" ] && exit 1
branch=$1
commit=$2
port_inc=$3
entrypoint_p1=$4

HOST_TOMCAT_PORT=$(( 8080 + port_inc ))
HOST_SOLR_PORT=$(( 8983 + port_inc ))
HOST_MONGO_PORT=$(( 27017 + port_inc ))

#  Admittedly, these functions don't do the pop themselves, not yet anyway
pop_and_join_volume () { local IFS="_"; echo "${*}"; }
named_volume="`pop_and_join_volume ${branch} ${commit}`"
pop_and_join_image () { local IFS=":"; echo "${*}"; }
named_image="`pop_and_join_image ${branch} ${commit}`"

docker run -it \
           -p ${HOST_TOMCAT_PORT}:8080 \
           -p ${HOST_SOLR_PORT}:8983 \
           -p ${HOST_MONGO_PORT}:27017 \
           -v ols_${named_volume}_mongo_data:/data/db \
           -v ols_${named_volume}_neo_data:/home/ols/.ols/neo4j \
           -v ols_${named_volume}_autosuggest:/home/ols/.ols/solr/autosuggest \
           -v ols_${named_volume}_ontology:/home/ols/.ols/solr/ontology \
           ols-branch-${named_image} ${entrypoint_p1}
