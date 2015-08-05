package uk.ac.ebi.spot.ols.config;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.SolrServerFactory;
import org.springframework.data.solr.server.support.MulticoreSolrServerFactory;

import java.net.MalformedURLException;

/**
 * @author Simon Jupp
 * @date 02/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = {"uk.ac.ebi.spot.ols"})
@Configuration
@EnableNeo4jRepositories(basePackages = "uk.ac.ebi.spot.ols.neo4j.repository")
@EnableMongoRepositories(basePackages = "uk.ac.ebi.spot.ols.repository.mongo")
@EnableSolrRepositories(basePackages = "uk.ac.ebi.spot.ols.indexer")
public class ApplicationConfig extends Neo4jConfiguration {

    public ApplicationConfig() {
        setBasePackage("uk.ac.ebi.spot.ols");
    }

    @Bean
    static GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory().newEmbeddedDatabase("target/batchinserter-example");
    }

    @Bean
    public SolrServer solrServer() throws MalformedURLException, IllegalStateException {
        return new HttpSolrServer("http://localhost:8983/solr");
    }

    @Bean
    public SolrServerFactory solrServerFactory() throws MalformedURLException, IllegalStateException {
        return new MulticoreSolrServerFactory(solrServer());
    }

    @Bean
    public SolrTemplate solrTemplate() throws Exception {
        SolrTemplate solrTemplate = new SolrTemplate(solrServerFactory());
        solrTemplate.setSolrCore("ontology");
        return solrTemplate;
    }
}
