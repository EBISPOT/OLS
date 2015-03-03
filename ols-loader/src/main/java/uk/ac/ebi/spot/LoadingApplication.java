package uk.ac.ebi.spot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import uk.ac.ebi.spot.config.OntologyResourceConfig;
import uk.ac.ebi.spot.loader.ELKOWLOntologyLoader;
import uk.ac.ebi.spot.loader.HermitOWLOntologyLoader;
import uk.ac.ebi.spot.loader.OntologyLoader;
import uk.ac.ebi.spot.loader.SKOSLoader;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.repository.MongoOntologyRepository;
import uk.ac.ebi.spot.ols.service.OntologyIndexer;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * This application reads from an ontology documents repository and checks if any ontologies external
 * ontologies have been updated. If they have it create indexes
 *
 */
@SpringBootApplication
public class LoadingApplication implements CommandLineRunner {

    @Autowired
    MongoOntologyRepository mongoOntologyRepository;

    @Autowired
    List<OntologyIndexer> indexers;

    @Autowired
    FileUpdater fileUpdater;

    @Override
    public void run(String... args) throws Exception {

        System.setProperty("entityExpansionLimit", "10000000");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.initialize();

        FileUpdatingService service = new FileUpdatingService(executor);

        service.checkForUpdates(mongoOntologyRepository.findAll(), fileUpdater);
        CountDownLatch latch = service.getCountdownLatch();

        try {
            latch.await();

        } catch (Exception e) {

        }

        for (OntologyDocument document : service.getUpdatedOntologies()) {

            OntologyResourceConfig config = document.getConfig();

            try {
                OntologyLoader loader = null;
                if (config.isClassify()) {
                    loader = new HermitOWLOntologyLoader(config);
                }
                else if (config.isSkos()) {
                    loader = new SKOSLoader(config);
                }
                else {
                    loader = new ELKOWLOntologyLoader(config);
                }

                loader.setOntologyResource(new FileSystemResource(service.getLocalFileMap().get(document.getOntologyId())));
                // if updated get local path, and set location to local file

                // get all the available indexers
                for (OntologyIndexer indexer : indexers) {
                    // create the new index
                    indexer.createIndex(loader);
                }
            } catch (Exception e) {

            }
        }



    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(LoadingApplication.class, args);
    }

}
