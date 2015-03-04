package uk.ac.ebi.spot.ols.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.loader.OntologyLoader;
import uk.ac.ebi.spot.loader.OntologyLoaderFactory;
import uk.ac.ebi.spot.ols.exception.OntologyIndexingException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;
import uk.ac.ebi.spot.ols.model.Status;

import java.util.Date;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class MongoOntologyIndexingService implements OntologyIndexingService{

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Autowired
    OntologyRepositoryService ontologyRepositoryService;

    @Autowired(required=false)
    List<OntologyIndexer> indexers;

    @Override
    public void indexOntologyDocument(OntologyDocument document) throws OntologyIndexingException {


        document.setStatus(Status.LOADING);
        ontologyRepositoryService.update(document);

        Status status = Status.FAILED;
        String message = "";
        try {
            OntologyLoader loader = OntologyLoaderFactory.getLoader(document.getConfig());

            if (document.getLocalPath() != null) {
                // if updated get local path, and set location to local file
                loader.setOntologyResource(new FileSystemResource(document.getLocalPath()));
            }

            // get all the available indexers
            for (OntologyIndexer indexer : indexers) {
                // create the new index
                indexer.createIndex(loader);
            }

            status = Status.LOADED;
            document.setNumberOfTerms(loader.getAllClasses().size());
            ontologyRepositoryService.update(document);

        } catch (Exception e) {
            status = Status.FAILED;
            message = e.getMessage();
            getLog().error("Failed to create any indexes for " + document.getOntologyId());
        }
        finally {
            document.setStatus(status);
            document.setUpdated(new Date());
            document.setMessage(message);
        }
    }
}
