package uk.ac.ebi.spot.ols;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.aspects.config.Neo4jAspectConfiguration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.config.PropertyBasedLoadingService;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyTermRepository;
import uk.ac.ebi.spot.ols.neo4j.rest.CustomBackendIdConverter;

import javax.xml.soap.Node;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
//@Configuration
@SpringBootApplication
public class Neo4JOlsApp implements CommandLineRunner {

    @Import(RepositoryRestMvcConfiguration.class)
//    @EnableScheduling
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {"uk.ac.ebi.spot.ols"})
    @Configuration
    @EnableTransactionManagement (mode = AdviceMode.ASPECTJ)
    @EnableAspectJAutoProxy
    @EnableNeo4jRepositories(basePackages = "uk.ac.ebi.spot.ols.neo4j.repository")
    @EnableMongoRepositories(basePackages = "uk.ac.ebi.spot.ols.repository.mongo")
    static class ApplicationConfig extends Neo4jAspectConfiguration {

        public ApplicationConfig() {
            setBasePackage("uk.ac.ebi.spot.ols");
        }

        @Bean
        static GraphDatabaseService graphDatabaseService() {
//            return new SpringRestGraphDatabase("http://localhost:7474/db/data");

            return new GraphDatabaseFactory().newEmbeddedDatabase("target/batchinserter-example");
//            return new SpringCypherRestGraphDatabase("http://localhost:7474/db/data", "dba", "m1lest0nes");
        }


//        @Bean
//        static BackendIdConverter getBackendIdConverter() {
//            return  new CustomBackendIdConverter();
//        }
//
//    @Override
//    public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy() throws Exception {
//        return new IndexBasedRelationshipTypeRepresentationStrategy(
//                                            graphDatabase(),
//                graphDatabase().getIndex("")
//                                            );
//    }

    }

//    @Configuration
//    static class AspectJNeoConfiguration extends Neo4jAspectConfiguration {
//        public AspectJNeoConfiguration() {
//            setBasePackage("uk.ac.ebi.spot.ols");
//        }
//
//        @Bean
//        static GraphDatabaseService graphDatabaseService() {
////            return new SpringRestGraphDatabase("http://localhost:7474/db/data");
//
//            return new GraphDatabaseFactory().newEmbeddedDatabase("target/batchinserter-example");
////            return new SpringCypherRestGraphDatabase("http://localhost:7474/db/data", "dba", "m1lest0nes");
//        }
//    }


//    @Autowired
//    OntologyTermRepository ontologyTermRepository;

//    @Autowired
//    PropertyBasedLoadingService propertyBasedLoadingService;
//
//    @Autowired
//    GraphDatabase graphDatabase;

//    @Autowired
//    Neo4jTemplate neo4jTemplate;

    @Override
//    @Transactional
    public void run(String... args) throws Exception {
//        System.setProperty("entityExpansionLimit", "10000000");
//        Transaction tx = graphDatabase.beginTx();
//
//        Map<IRI, Term> termMap = new HashMap<>();
//        OntologyLoader loader = propertyBasedLoadingService.getLoader();
//
//        for (IRI iri : loader.getAllClasses()) {
//
//            Term current =  null;
//            if (!termMap.containsKey(iri)) {
//                current = new Term();
//                current.setIri(iri.toString());
//                current.setLabel(loader.getTermLabels().get(iri));
//
////                current = ontologyTermRepository.save(current);
//                termMap.put(iri, current);
//            }
//            current = termMap.get(iri);
//            System.out.println(current.getLabel());
//
//            Set<Term> parents = new HashSet<>();
//            for (IRI parentIri : loader.getDirectParentTerms(iri)) {
//                if (parentIri != iri) {
//                    if (!termMap.containsKey(parentIri)) {
//                        Term parent = new Term();
//                        parent.setIri(parentIri.toString());
//                        parent.setLabel(loader.getTermLabels().get(parentIri));
////                        parent = ontologyTermRepository.save(parent);
//                        termMap.put(parentIri, parent);
//                    }
//                    parents.add(termMap.get(parentIri));
//                }
//            }
//
//            current.setSubclassOf(parents);
////            ontologyTermRepository.save(current);
//            ((NodeBacked)current).persist();
//
//        }

//        Term node1 = new Term();
//        node1.setLabel("test");
//
//        Term node2 = new Term();
//        node2.setLabel("test2");
//
//        Term node3 = new Term();
//        node3.setLabel("test3");
//        Term node4 = new Term();
//        node4.setLabel("node4");
//        Term node5 = new Term();
//        node5.setLabel("node5");
//
//
//        node1.setSubclassOf(Collections.singleton(node2));
//        Set<Term> nodes = new HashSet<Term>();
//        nodes.add(node3);
//        nodes.add(node4);
//
//        node2.setSubclassOf(nodes);
//        node4.setSubclassOf(Collections.singleton(node5));
//
//
//        ontologyTermRepository.save(node5);
//        ontologyTermRepository.save(node4);
//        ontologyTermRepository.save(node3);
//        ontologyTermRepository.save(node2);
//        ontologyTermRepository.save(node1);


//        tx.success();
//        System.out.println("am i here?");
//        for (Term node : ontologyTermRepository.findAll()) {
//
//            System.out.println(node.getLabel());
//            for (Term parent  : node.getSubclassOf() ) {
//                System.out.println("\t" + parent.getLabel());
//
//            }
////            System.out.println(node.getAnnotation().keySet());
//        }
//        tx.finish();



    }


    public static void main(String[] args) {
        SpringApplication.run(Neo4JOlsApp.class, args);

    }

}
