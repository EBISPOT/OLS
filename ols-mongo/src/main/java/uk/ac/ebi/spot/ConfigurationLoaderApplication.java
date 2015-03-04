package uk.ac.ebi.spot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.spot.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.repository.MongoOntologyRepository;
import uk.ac.ebi.spot.config.PropertyBasedLoadingService;
import uk.ac.ebi.spot.ols.model.OntologyDocument;

/**
 * @author Simon Jupp
 * @date 17/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@EnableAutoConfiguration
@SpringBootApplication
public class ConfigurationLoaderApplication implements CommandLineRunner {

    @Autowired
    MongoOntologyRepository mongoOntologyRepository;

    @Autowired
    PropertyBasedLoadingService propertyBasedLoadingService;

    @Override
    public void run(String... args) throws Exception {


        System.out.println("Starting mongodb config loading application");
        OntologyResourceConfig config = propertyBasedLoadingService.getConfiguration();
        System.out.println("Got config for " + config.getTitle());
        OntologyDocument document = new OntologyDocument(config.getNamespace(), config);

//        System.out.println("Clearing existing mongo db...");
//        mongoOntologyRepository.deleteAll();

        System.out.println("Saving document to mongo db...");
        mongoOntologyRepository.save(document);

        System.out.println("document loaded");

        for (OntologyDocument document1 : mongoOntologyRepository.findAll()) {
            System.out.println("mongo document id: " + document1.getOntologyId());
            System.out.println("mongo ontology config id"  + document1.getConfig().getId());
        }



    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConfigurationLoaderApplication.class, args);
    }

}
