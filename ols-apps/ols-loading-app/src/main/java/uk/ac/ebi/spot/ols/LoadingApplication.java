package uk.ac.ebi.spot.ols;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.service.FileUpdatingService;
import uk.ac.ebi.spot.ols.service.OntologyIndexingService;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;
import uk.ac.ebi.spot.ols.util.FileUpdater;

import java.util.*;
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
@EnableNeo4jRepositories(basePackages = "uk.ac.ebi.spot.ols.neo4j.repository")
@EnableMongoRepositories(basePackages = "uk.ac.ebi.spot.ols.repository.mongo")
public class LoadingApplication implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Autowired
    OntologyRepositoryService ontologyRepositoryService;

    @Autowired
    OntologyIndexingService ontologyIndexingService;

    @Autowired
    FileUpdater fileUpdater;

    private static String [] ontologies = {};

    private static boolean offline = false;

    @Override
    public void run(String... args) throws Exception {

        int parseArgs = parseArguments(args);

        System.setProperty("entityExpansionLimit", "10000000");
        Map<OntologyDocument, Exception> failedOntologies = new HashMap<>();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.initialize();

        List<OntologyDocument> allDocuments = new ArrayList<OntologyDocument>();

        if (ontologies.length > 0) {
            // get the ontologies forced to update
            for (String ontologyName : ontologies) {
                OntologyDocument document = ontologyRepositoryService.get(ontologyName);
                if (document != null) {
                    if (!offline) {
                        // check these documents for updates
                        allDocuments.add(ontologyRepositoryService.get(ontologyName));
                    }
                    else  {
                        // if forced, set to load anyway
                        document.setStatus(Status.TOLOAD);
                        ontologyRepositoryService.update(document);
                    }
                }
            }
        }
        else if (!offline){
            // get all documents and check for updates
            allDocuments = ontologyRepositoryService.getAllDocuments();
        }

        CountDownLatch latch = new CountDownLatch(allDocuments.size());
        FileUpdatingService service = new FileUpdatingService(ontologyRepositoryService, executor, latch);
        service.checkForUpdates(allDocuments, fileUpdater, ontologies.length>0);

        // wait for ontologies to have been checked
        latch.await();

        // For all ontologies set to load, create the new index

        for (OntologyDocument document : ontologyRepositoryService.getAllDocumentsByStatus(Status.TOLOAD)) {
            try {
                ontologyIndexingService.indexOntologyDocument(document);
            } catch (Exception e) {
                getLog().error("Application failed creating indexes for " + document.getOntologyId() + ": " + e.getMessage());
                System.exit(1);
            }
        }


        System.exit(0);
    }


    private static int parseArguments(String[] args) {

        CommandLineParser parser = new GnuParser();
        HelpFormatter help = new HelpFormatter();
        Options options = bindOptions();

        int parseArgs = 0;
        try {
            CommandLine cl = parser.parse(options, args, true);

            // check for mode help option
            if (cl.hasOption("") || cl.hasOption("h")) {
                // print out mode help
                help.printHelp("ols-loader.sh", options, true);
                parseArgs += 1;
            }
            else {
                // find -f option to see if we are to force load
                if (cl.hasOption("f") ) {
                    ontologies = cl.getOptionValues("f");
                }

                offline = cl.hasOption("off");
            }
        }
        catch (ParseException e) {
            System.err.println("Failed to read supplied arguments");
            help.printHelp("publish", options, true);
            parseArgs += 1;
        }
        return parseArgs;
    }

    private static Options bindOptions() {
        Options options = new Options();

        // help
        Option helpOption = new Option("h", "help", false, "Print the help");
        options.addOption(helpOption);

        // force update
        Option force = new Option("f", "force", true,
                "List the ontologies to force update");
        force.setRequired(false);
        force.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(force);

        Option nodownload = new Option("off", "offline", false,
                        "Run offline - doesn't download new versions");
        nodownload.setRequired(false);
        options.addOption(nodownload);


        return options;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(LoadingApplication.class, args);
    }

}
