package uk.ac.ebi.spot.ols.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.task.TaskExecutor;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.exception.FileUpdateServiceException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.util.FileUpdater;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author Simon Jupp
 * @date 16/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * This service checks if an ontology document needs updating. It sets the status of the document in
 * a repository service.
 *
 */
public class FileUpdatingService {

    private OntologyRepositoryService ontologyRepositoryService;

    private CountDownLatch latch;

    private TaskExecutor taskExecutor;

    private Logger log = LoggerFactory.getLogger(getClass());

    private static int MAX_LOAD_ATTEMPTS = 3;

    public Logger getLog() {
        return log;
    }

    public FileUpdatingService(OntologyRepositoryService ontologyRepositoryService, TaskExecutor taskExecutor, CountDownLatch latch) {
        this.ontologyRepositoryService = ontologyRepositoryService;
        this.taskExecutor = taskExecutor;
        this.latch = latch;
    }

    private class FileUpdatingTask implements Runnable {

        private OntologyDocument document;
        private FileUpdater fileUpdateService;
        private boolean force = false;

        public FileUpdatingTask(OntologyDocument document, FileUpdater fileUpdateService, boolean force) {
            this.document = document;
            this.fileUpdateService = fileUpdateService;
            this.force = force;
        }

        public void run() {
            // check if document is updated
            getLog().info("Checking status of " + document.getOntologyId());
            OntologyResourceConfig config = document.getConfig();

            // check if was previously failing
            boolean wasFailing = false;
            if (document.getStatus() != null) {
                if (document.getStatus().equals(Status.FAILED) || document.getStatus().equals(Status.NOTLOADED) ||
                        document.getStatus().equals(Status.LOADING) || document.getStatus().equals(Status.DOWNLOADING)) {
                    wasFailing = true;
                    getLog().debug(document.getOntologyId() + " + failed previously with status = " + document.getStatus());
                } else {
                    document.setLoadAttempts(0);
                }
            }

            boolean skip = document.getStatus() == Status.SKIP;

            if(!skip) {
                if(wasFailing) {
                    int newLoadAttempts = document.getLoadAttempts() + 1;
                    document.setLoadAttempts(newLoadAttempts);
                    
                    getLog().info(document.getOntologyId() + " has failed " + newLoadAttempts + " times out of " + MAX_LOAD_ATTEMPTS + " max");

                    if(newLoadAttempts >= MAX_LOAD_ATTEMPTS) {
                        getLog().info(document.getOntologyId() + " failed too many times; skipping now and in future");
                        document.setStatus(Status.SKIP);
                        skip = true;
                    }
                }
            }

            FileUpdater.FileStatus status = null;

            try {
                if(!skip) {
                    document.setStatus(Status.DOWNLOADING);
                    document.setUpdated(new Date());
                    document.setMessage("");
                    ontologyRepositoryService.update(document);

                    status = fileUpdateService.getFile(config.getNamespace(), config.getFileLocation());
                    document.setLocalPath(status.getFile().getCanonicalPath());
                    String fileHash = document.getFileHash();
                    if (force || fileHash == null || !fileHash.equals(status.getLatestHash()) || wasFailing) {
                        document.setStatus(Status.TOLOAD);
                        document.setMessage("");
                    }
                    else {
                        document.setStatus(Status.LOADED);
                        document.setMessage("");
                    }
                }
            } catch (FileUpdateServiceException e) {

                if (document.getLoaded() == null) {
                    document.setStatus(Status.NOTLOADED);
                }
                else {
                    document.setStatus(Status.FAILED);
                }
                document.setMessage(e.getMessage());
                log.error("Error checking: " + config.getTitle() + e.getMessage(), e);
            } catch (IOException e) {
                if (document.getLoaded() == null) {
                    document.setStatus(Status.NOTLOADED);
                }
                else {
                    document.setStatus(Status.FAILED);
                }
                document.setMessage(e.getMessage());
                log.error("Can't get canonical path for: " + status.getFile().getPath(), e);
            }
            finally {
                getLog().info("Status of " + document.getOntologyId() + " is " + document.getStatus());

                document.setUpdated(new Date());

                if(status != null)
                    document.setFileHash(status.getLatestHash());

                ontologyRepositoryService.update(document);
                latch.countDown();
            }

        }
    }

    public void checkForUpdates(List<OntologyDocument> documents, FileUpdater fileUpdateService) {
        checkForUpdates(documents, fileUpdateService, false);
    }

    public void checkForUpdates(List<OntologyDocument> documents, FileUpdater fileUpdateService, boolean force) {
        for(OntologyDocument document : documents) {
            getLog().debug("Starting file update check for " + document.getOntologyId());
            taskExecutor.execute(new FileUpdatingTask(document, fileUpdateService, force));
        }
    }
}
