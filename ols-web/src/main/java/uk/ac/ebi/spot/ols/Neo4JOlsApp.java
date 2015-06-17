package uk.ac.ebi.spot.ols;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.TypeRepresentationStrategy;
import org.springframework.data.neo4j.support.typerepresentation.IndexBasedNodeTypeRepresentationStrategy;
import org.springframework.data.neo4j.support.typerepresentation.IndexBasedRelationshipTypeRepresentationStrategy;
import org.springframework.data.neo4j.support.typerepresentation.NoopNodeTypeRepresentationStrategy;
import org.springframework.data.neo4j.support.typerepresentation.TypeRepresentationStrategyFactory;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Parent;
import uk.ac.ebi.spot.ols.neo4j.model.TermNode;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyTermRepository;
import uk.ac.ebi.spot.ols.neo4j.rest.CustomBackendIdConverter;

import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
//@Configuration
@SpringBootApplication
public class Neo4JOlsApp implements CommandLineRunner {

    //    @Import(RepositoryRestMvcConfiguration.class)
    @EnableScheduling
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {"uk.ac.ebi.spot.ols"})
    @Configuration
    @EnableTransactionManagement
    @EnableNeo4jRepositories(basePackages = "uk.ac.ebi.spot.ols.neo4j.repository")
    @EnableMongoRepositories(basePackages = "uk.ac.ebi.spot.ols.repository.mongo")
    static class ApplicationConfig extends Neo4jConfiguration {

        public ApplicationConfig() {
            setBasePackage("uk.ac.ebi.spot.ols");
        }

        @Bean
        static GraphDatabaseService graphDatabaseService() {
//            return new SpringRestGraphDatabase("http://localhost:7474/db/data");

            return new GraphDatabaseFactory().newEmbeddedDatabase("target/batchinserter-example");
//            return new SpringCypherRestGraphDatabase("http://localhost:7474/db/data", "dba", "m1lest0nes");
        }

        @Bean
        static BackendIdConverter getBackendIdConverter() {
            return  new CustomBackendIdConverter();
        }
//
//    @Override
//    public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy() throws Exception {
//        return new IndexBasedRelationshipTypeRepresentationStrategy(
//                                            graphDatabase(),
//                graphDatabase().getIndex("")
//                                            );
//    }

    }


    @Autowired
    OntologyTermRepository ontologyTermRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {


//        TermNode node1 = new TermNode();
//        node1.setLabel("test");
//
//        TermNode node2 = new TermNode();
//        node2.setLabel("test2");
//
//        Parent parent = new Parent();
//        parent.setLabel("is a");
//        parent.setChild(node1);
//        parent.setParent(node2);
//
//        node1.setParents(Collections.singleton(parent));
//
//
//
//        ontologyTermRepository.save(node2);
//        ontologyTermRepository.save(node1);

//        for (TermNode node : ontologyTermRepository.findAll()) {
//            System.out.println(node.getLabel());
////            System.out.println(node.getAnnotation().keySet());
//        }

    }


    public static void main(String[] args) {
        SpringApplication.run(Neo4JOlsApp.class, args);

    }

}
