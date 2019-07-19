FROM solr:5.3.1
USER solr
RUN mkdir mydata
CMD ["solr", "-f", "-a", "-Dsolr.data.dir=/opt/solr/mydata"]
