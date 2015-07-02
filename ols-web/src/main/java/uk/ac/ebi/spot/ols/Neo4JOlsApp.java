package uk.ac.ebi.spot.ols;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.aspects.config.Neo4jAspectConfiguration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;


import javax.xml.soap.Node;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@SpringBootApplication
public class Neo4JOlsApp {

//    @Import(RepositoryRestMvcConfiguration.class)
//    @EnableAutoConfiguration
//    @ComponentScan(basePackages = {"uk.ac.ebi.spot.ols"})
//    @Configuration
//    @EnableNeo4jRepositories(basePackages = "uk.ac.ebi.spot.ols.neo4j.repository")
//    @EnableMongoRepositories(basePackages = "uk.ac.ebi.spot.ols.repository.mongo")
//    static class ApplicationConfig extends Neo4jAspectConfiguration {
//
//        public ApplicationConfig() {
//            setBasePackage("uk.ac.ebi.spot.ols");
//        }
//
//        @Bean
//        static GraphDatabaseService graphDatabaseService() {
//            return new GraphDatabaseFactory().newEmbeddedDatabase("target/batchinserter-example");
//        }
//
//
//    }

//    @Override
//    public void run(String... args) throws Exception {
//
//    }


    public static void main(String[] args) {
        SpringApplication.run(Neo4JOlsApp.class, args);

    }

}
