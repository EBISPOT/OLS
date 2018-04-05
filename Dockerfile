# This docker file builds a base image for laoding OLS data, this image itself does not run the OLS server or deal with loading data
# to see and example of a Dockerfile for loading ontologies and running a local OLS server checkout https://github.com/HumanCellAtlas/ontology/blob/master/Dockerfile

FROM alpine:3.7

ENV PACKAGES bash mongodb openjdk8
ENV TERM=linux

RUN apk update && apk upgrade && \
    apk add $PACKAGES --no-cache && \
    rm -rf /var/cache/apk/*

ENV OLS_HOME /opt/ols
ENV SOLR_VERSION 5.5.3

RUN mkdir -p ${OLS_HOME}

ADD ols-web/target/ols-boot.war  ${OLS_HOME}
ADD ols-apps/ols-config-importer/target/ols-config-importer.jar ${OLS_HOME}
ADD ols-apps/ols-loading-app/target/ols-indexer.jar ${OLS_HOME}
ADD ols-solr/src/main/solr-5-config ${OLS_HOME}/solr-5-config

## Prepare configuration files
ADD ols-web/src/main/resources/application.properties ${OLS_HOME}

### Install solr
RUN mkdir -p /data/db \
  && cd /opt \
  && wget http://archive.apache.org/dist/lucene/solr/${SOLR_VERSION}/solr-${SOLR_VERSION}.tgz \
  && tar xzf solr-${SOLR_VERSION}.tgz

