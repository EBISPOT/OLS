package uk.ac.ebi.spot.ols.service;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.exception.OntologyIndexingException;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoaderFactory;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

import java.util.Collection;
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

            Collection<IRI> classes = loader.getAllClasses();
            if (classes.size() + loader.getAllObjectPropertyIRIs().size() == 0) {
                getLog().warn("No classes or properties found in latest version of " + loader.getOntologyName() + ": Won't index!");
                message = "Last update had no classes or properties so was ignored";
            } else  {
                // get all the available indexers
                for (OntologyIndexer indexer : indexers) {
                    // create the new index
                    indexer.dropIndex(loader);
                    indexer.createIndex(loader);
                }
                document.setNumberOfTerms(classes.size());
            }
            status = Status.LOADED;
            ontologyRepositoryService.update(document);


        } catch (Exception e) {
            status = Status.FAILED;
            message = e.getMessage();
            throw new OntologyIndexingException("Index for " + document.getOntologyId() + " failed: " + e.getMessage());
        }
        finally {
            document.setStatus(status);
            document.setUpdated(new Date());
            document.setMessage(message);
            ontologyRepositoryService.update(document);
        }
    }
}
