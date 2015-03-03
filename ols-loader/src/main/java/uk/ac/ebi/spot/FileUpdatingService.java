package uk.ac.ebi.spot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import uk.ac.ebi.spot.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.model.OntologyDocument;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author Simon Jupp
 * @date 16/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class FileUpdatingService {

    private Collection<OntologyDocument> updatedOntologies;

    private CountDownLatch latch;

    private TaskExecutor taskExecutor;

    private Map<String, File> localFileMap;

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    public FileUpdatingService(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        localFileMap = new HashMap<>();
        updatedOntologies = new HashSet<>();
    }
    private class FileUpdatingTask implements Runnable {

        private OntologyDocument document;
        private FileUpdater fileUpdateService;

        public FileUpdatingTask(OntologyDocument document, FileUpdater fileUpdateService) {
            this.document = document;
            this.fileUpdateService = fileUpdateService;

        }

        public void run() {
            // check if document is updated

            OntologyResourceConfig config = document.getConfig();

            FileUpdater.FileStatus status = null;
            try {
                status = fileUpdateService.getFile(config.getTitle(), config.getFileLocation());
                if (status.isNew()) {
                    updatedOntologies.add(document);
                    localFileMap.put(document.getOntologyId(), status.getFile());
                }
            } catch (FileUpdateServiceException e) {
                e.printStackTrace();
            }
            finally {
                latch.countDown();
            }

        }
    }

    public Map<String, File> getLocalFileMap() {
        return localFileMap;
    }



    public void checkForUpdates(List<OntologyDocument> documents, FileUpdater fileUpdateService) {
        latch = new CountDownLatch(documents.size());
        for(OntologyDocument document : documents) {
            getLog().info("Starting file update check for " + document.getOntologyId());
            taskExecutor.execute(new FileUpdatingTask(document, fileUpdateService));
        }
    }

    public CountDownLatch getCountdownLatch() {
        return latch;
    }

    public Collection<OntologyDocument> getUpdatedOntologies() {
        return updatedOntologies;
    }
}
