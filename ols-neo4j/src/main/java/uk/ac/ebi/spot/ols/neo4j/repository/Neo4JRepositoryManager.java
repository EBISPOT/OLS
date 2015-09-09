package uk.ac.ebi.spot.ols.neo4j.repository;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;

/**
 * @author Simon Jupp
 * @date 09/09/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class Neo4JRepositoryManager {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    GraphDatabaseService graphDatabaseService;

    @PreDestroy
    public void destroy() {
        log.info("Destroying Neo4jRepositoryManager...");
        shutdown();
    }

    private void shutdown() {
        graphDatabaseService.shutdown();
    }
}
