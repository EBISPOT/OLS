package uk.ac.ebi.spot.ols;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.service.FileUpdatingService;
import uk.ac.ebi.spot.ols.service.OntologyIndexingService;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;
import uk.ac.ebi.spot.ols.util.FileUpdater;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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

    private final Logger logger = LoggerFactory.getLogger(LoadingApplication.class);

    @Autowired
    OntologyRepositoryService ontologyRepositoryService;

    @Autowired
    OntologyIndexingService ontologyIndexingService;

    @Autowired
    FileUpdater fileUpdater;

    @Autowired
    MailService mailService;

    private static String [] forcedOntologies = {};

    private static String email;

    private static String [] deleteOntologies = {};


    private static boolean offline = false;

    @Override
    public void run(String... args) throws Exception {

        int parseArgs = parseArguments(args);
    

        System.setProperty("entityExpansionLimit", "10000000");
        Collection<String> updatedOntologies = new HashSet<>();
        Map<String, String> failingOntologies= new HashMap<>();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.initialize();

        List<OntologyDocument> allDocuments = new ArrayList<OntologyDocument>();

        if (forcedOntologies.length > 0) {
            // get the ontologies forced to update
          for (String ontologyName : forcedOntologies) {
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
        else if (deleteOntologies.length > 0){
            // get the ontologies requested for deletion
            for (String ontologyName : deleteOntologies) {
                OntologyDocument document = ontologyRepositoryService.get(ontologyName);

                if (document != null) {
                    document.setStatus(Status.TOREMOVE);
                    ontologyRepositoryService.update(document);
                }
                else {
                    StringBuffer errorMessage = new StringBuffer("Could not detele ontology ");
                    errorMessage.append(ontologyName);
                    errorMessage.append(" as it doesn't exist in OLS");
                    logger.warn(errorMessage.toString());
                    failingOntologies.put(document.getOntologyId(), errorMessage.toString());
                }

            }
        }
        else if (!offline){
            // get all documents and check for updates
            allDocuments = ontologyRepositoryService.getAllDocuments();
        }

        CountDownLatch latch = new CountDownLatch(allDocuments.size());
        FileUpdatingService service = new FileUpdatingService(ontologyRepositoryService, executor, latch);
        service.checkForUpdates(allDocuments, fileUpdater, forcedOntologies.length>0);

        // wait for ontologies to have been checked
        latch.await();


        // For all ontologies set to load, create the new index

        boolean haserror = false;

        // if force loading
        long start = System.currentTimeMillis();
        StringBuilder exceptions = new StringBuilder();

        if (forcedOntologies.length > 0) {
            for (String ontologyName : forcedOntologies) {
                OntologyDocument document = ontologyRepositoryService.get(ontologyName);
                if (document != null) {
                    try {
                        ontologyIndexingService.indexOntologyDocument(document);
                        updatedOntologies.add(document.getOntologyId());
                    } catch (Throwable t) {
                        logger.error("Application failed creating indexes for " + 
                        		document.getOntologyId() + ": " + t.getMessage(), t);
                        haserror = true;
                        failingOntologies.put(document.getOntologyId(), t.getMessage());
                    }
                }
            }
        }
        else if (deleteOntologies.length > 0){
            for (String ontologyName : deleteOntologies) {
                OntologyDocument document = ontologyRepositoryService.get(ontologyName);
                if (document != null) {
                    try {
                        ontologyIndexingService.removeOntologyDocumentFromIndex(document);
                        ontologyRepositoryService.delete(document);
                        updatedOntologies.add(document.getOntologyId());
                    } catch (Throwable t) {
                    	logger.error("Application failed deleting indexes for " + document.getOntologyId() + ": " +
                                t.getMessage(), t);
                        haserror = true;
                        failingOntologies.put(document.getOntologyId(), t.getMessage());
                    }
                }
            }
        }
        else {
            // otherwise load everything set TOLOAD
            for (OntologyDocument document : ontologyRepositoryService.getAllDocumentsByStatus(Status.TOLOAD)) {
                try {
                    boolean loadResult = ontologyIndexingService.indexOntologyDocument(document);
                    if (loadResult)
                        updatedOntologies.add(document.getOntologyId());
                    else {
                        haserror = true;
                        failingOntologies.put(document.getOntologyId(), "An error occurred. Check logs.");
                    }
                } catch (Throwable t) {
                	logger.error("Application failed creating indexes for " + document.getOntologyId() + ": " +
                            t.getMessage(), t);
                    exceptions.append(t.getMessage());
                    exceptions.append("\n");
                    haserror = true;
                    failingOntologies.put(document.getOntologyId(),t.getMessage());
                }
            }
        }

        for (OntologyDocument document : ontologyRepositoryService.getAllDocumentsByStatus(Status.FAILED)) {
            failingOntologies.put(document.getOntologyId(), document.getMessage());
        }

        LoadingReport loadingReport = new LoadingReport(failingOntologies, updatedOntologies, exceptions.toString());

        System.out.println(LoadingReportPrinter.getMessage(loadingReport));

        if (email != null) {
            mailService.sendEmailNotification(email, null, "OLS loading report", LoadingReportPrinter.getMessage(loadingReport));
        }


        long end = System.currentTimeMillis();
        logger.debug("Duration of indexing = " + (end - start)/1000/60 + " minutes.");
        if (haserror) {
            System.exit(1);
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
                    forcedOntologies = cl.getOptionValues("f");
                }

                offline = cl.hasOption("off");


                if (cl.hasOption("m")) {
                    InternetAddress emailAddr = new InternetAddress( cl.getOptionValue("m"));
                    emailAddr.validate();
                    email = cl.getOptionValue("m");
                }

                if (cl.hasOption("d")){
                    deleteOntologies = cl.getOptionValues("d");
                }

            }

        }
        catch (AddressException ex) {
            System.err.println("Please supply a valid e-mail address");
            parseArgs += 1;
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


        Option mail = new Option("m", "mail", true,
                "Send e-mail report");
        mail.setRequired(false);
        options.addOption(mail);

        Option delete = new Option("d", "delete", true, "List the ontologies to be deleted");
        delete.setRequired(false);
        options.addOption(delete);

        return options;

    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(LoadingApplication.class, args);
    }

}
