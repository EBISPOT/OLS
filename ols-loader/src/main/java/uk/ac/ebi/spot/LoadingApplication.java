package uk.ac.ebi.spot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.repository.MongoOntologyRepository;
import uk.ac.ebi.spot.ols.service.OntologyIndexer;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Autowired
    MongoOntologyRepository mongoOntologyRepository;

    @Autowired
    List<OntologyIndexer> indexers;

    @Autowired
    FileUpdater fileUpdater;

    @Override
    public void run(String... args) throws Exception {

        System.setProperty("entityExpansionLimit", "10000000");
        Collection<OntologyDocument> failedOntologies = new HashSet<OntologyDocument>();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.initialize();

        List<OntologyDocument> allDocuments = mongoOntologyRepository.findAll();
        CountDownLatch latch = new CountDownLatch(allDocuments.size());
        FileUpdatingService service = new FileUpdatingService(executor, latch);

        service.checkForUpdates(allDocuments, fileUpdater);

        failedOntologies.addAll(service.getFailedOntologies());

        try {
            // wait for ontologies to have been checked
            latch.await();

            // for the ontologies that were updated, create the new index
            for (OntologyDocument document : service.getUpdatedOntologies()) {

                document.setStatus(Status.LOADING);
                mongoOntologyRepository.save(document);

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

                    // if updated get local path, and set location to local file
                    loader.setOntologyResource(new FileSystemResource(service.getLocalFileMap().get(document.getOntologyId())));

                    // get all the available indexers
                    for (OntologyIndexer indexer : indexers) {
                        // create the new index
                        indexer.createIndex(loader);
                    }

                 document.setStatus(Status.LOADED);
                 document.setUpdated(new Date());
                 mongoOntologyRepository.save(document);

                } catch (Exception e) {
                    getLog().error("Failed to create any indexes for " + document.getOntologyId());
                    failedOntologies.add(document);
                }
            }

            for (OntologyDocument failed : failedOntologies) {
                failed.setStatus(Status.FAILED);
                failed.setUpdated(new Date());
                mongoOntologyRepository.save(failed);
                log.error("Failed to update: " + failed);
            }


        } catch (Exception e) {
            getLog().error("Failed to run file update service");
        }





    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(LoadingApplication.class, args);
    }

}
