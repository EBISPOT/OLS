#!/bin/bash

ARGS="$@"

chown solr /var/solr
su solr -c "cd /opt/solr && /opt/solr/bin/solr $ARGS"


