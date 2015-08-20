package uk.ac.ebi.spot.ols.service;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoaderFactory;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

import java.util.Collection;
import java.util.Collections;
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
    public void indexOntologyDocument(OntologyDocument document) throws IndexingException {

        OntologyLoader loader = null;
        Collection<IRI> classes;
        Collection<IRI> properties;
        Collection<IRI> individuals;
        String message = "";
        Status status = Status.FAILED;

        try {
            loader = OntologyLoaderFactory.getLoader(document.getConfig());
            if (document.getLocalPath() != null) {
                // if updated get local path, and set location to local file
                loader.setOntologyResource(new FileSystemResource(document.getLocalPath()));
            }
            classes = loader.getAllClasses();
            properties = loader.getAllObjectPropertyIRIs();
            individuals = loader.getAllIndividualIRIs();

            if (classes.size() + properties.size() + individuals.size() < 10) {
                getLog().error("A suspiciously small or zero classes or properties found in latest version of " + loader.getOntologyName() + ": Won't index!");
                message = "Failed to load - last update had no classes or properties so was rejected";
                document.setStatus(Status.FAILED);
                document.setMessage(message);
                ontologyRepositoryService.update(document);
                // don't try to index, just return
                return;
            }

        } catch (Exception e) {
            message = "Problem loading file so didn't proceed to index";
            getLog().error(message, e);
            document.setStatus(Status.FAILED);
            document.setMessage(message);
            ontologyRepositoryService.update(document);
            throw new IndexingException("Problem loading file so didn't proceed to index", e);
        }

        document.setStatus(Status.LOADING);
        ontologyRepositoryService.update(document);
        // if we get to here, we should have at least loaded the ontology
        try {

            // get all the available indexers
            for (OntologyIndexer indexer : indexers) {
                // create the new index
                indexer.dropIndex(loader);
                indexer.createIndex(loader);
            }

            // update any ontology meta data
            OntologyResourceConfig config = document.getConfig();

            if (loader.getTitle() != null) {
                config.setTitle(loader.getTitle());
            }
            if (loader.getOntologyDescription() != null) {
                config.setDescription(loader.getOntologyDescription());
            }
            if (loader.getHomePage() != null) {
                config.setHomepage(loader.getHomePage());
            }
            if (loader.getMailingList() != null) {
                config.setMailingList(loader.getMailingList());
            }
            if (!loader.getCreators().isEmpty()) {
                config.setCreators(loader.getCreators());
            }
            if (!loader.getOntologyAnnotations().keySet().isEmpty()) {
                config.setAnnotations(loader.getOntologyAnnotations());
            }
            document.setConfig(config);
            document.setNumberOfTerms(classes.size());
            status = Status.LOADED;

        } catch (Exception e) {
            getLog().error("Error indexing " + document.getOntologyId(), e);
            status = Status.FAILED;
            message = e.getMessage();
            throw new IndexingException("Index for " + document.getOntologyId() + " failed: ", e);
        }
        finally {

            document.setStatus(status);
            document.setUpdated(new Date());
            document.setMessage(message);
            ontologyRepositoryService.update(document);
        }
    }
}
