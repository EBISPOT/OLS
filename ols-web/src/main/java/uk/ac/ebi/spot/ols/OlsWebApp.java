package uk.ac.ebi.spot.ols;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableNeo4jRepositories(basePackages = "uk.ac.ebi.spot.ols.neo4j.repository")
@EnableMongoRepositories(basePackages = "uk.ac.ebi.spot.ols.repository.mongo")
//@EnableSolrRepositories(basePackages = "uk.ac.ebi.spot.ols.indexer")
public class OlsWebApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OlsWebApp.class, args);

    }


}
