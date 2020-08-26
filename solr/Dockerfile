FROM solr:5.3.1

USER root

ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh
COPY ./solr-5-config /mnt/solr-config
ENTRYPOINT ["/entrypoint.sh"]


