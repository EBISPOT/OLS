package uk.ac.ebi.spot.ols;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@SpringBootApplication
@EnableAutoConfiguration
public class OlsWebApp {

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
        SpringApplication.run(OlsWebApp.class, args);

    }

}
