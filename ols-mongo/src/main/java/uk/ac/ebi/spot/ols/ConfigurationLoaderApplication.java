package uk.ac.ebi.spot.ols;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.indexer.MongoTreeIndexer;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.config.PropertyBasedLoadingService;

/**
 * @author Simon Jupp
 * @date 17/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@EnableAutoConfiguration
@SpringBootApplication
public class ConfigurationLoaderApplication implements CommandLineRunner {

//    @Autowired
//    MongoOntologyRepository mongoOntologyRepository;

    @Autowired
    MongoTreeIndexer indexer;

    @Autowired
    PropertyBasedLoadingService propertyBasedLoadingService;

    @Override
    public void run(String... args) throws Exception {


        System.setProperty("entityExpansionLimit", "10000000");
        System.out.println("Starting mongodb config loading application");
        OntologyResourceConfig config = propertyBasedLoadingService.getConfiguration();
        System.out.println("Got config for " + config.getTitle());
//        OntologyDocument document = new OntologyDocument(config.getNamespace(), config);

//        System.out.println("Clearing existing mongo db...");
//        mongoOntologyRepository.deleteAll();

//        System.out.println("Saving document to mongo db...");
//        mongoOntologyRepository.save(document);
//
//        System.out.println("document loaded");
//
//        for (OntologyDocument document1 : mongoOntologyRepository.findAll()) {
//            System.out.println("mongo document id: " + document1.getOntologyId());
//            System.out.println("mongo ontology config id"  + document1.getConfig().getId());
//        }

        OntologyLoader loader = propertyBasedLoadingService.getLoader();
        indexer.createIndex(loader);


    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConfigurationLoaderApplication.class, args);
    }

}
