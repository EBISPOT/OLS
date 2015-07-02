package uk.ac.ebi.spot.ols;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.spot.ols.config.PropertyBasedLoadingService;
import uk.ac.ebi.spot.ols.loader.BatchOntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;

import java.io.File;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * Getting to grips with batch loading ontologies....
 *
 */

@SpringBootApplication
public class GraphLoaderApplication implements CommandLineRunner {

    @Autowired
    PropertyBasedLoadingService propertyBasedLoadingService;

    @Autowired
    BatchOntologyLoader batchOntologyLoader;

    @Override
    public void run(String... args) throws Exception {
        System.setProperty("entityExpansionLimit", "10000000");

        // get the ontology loader
        OntologyLoader loader = propertyBasedLoadingService.getLoader();
        batchOntologyLoader.dropIndex(loader);
        batchOntologyLoader.createIndex(loader);


    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(GraphLoaderApplication.class, args);
    }
}
