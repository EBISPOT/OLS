package uk.ac.ebi.spot.ols.service;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoaderFactory;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;
import uk.ac.ebi.spot.usage.ResourceUsage;

import java.text.SimpleDateFormat;
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

    private final Logger logger = LoggerFactory.getLogger(MongoOntologyIndexingService.class);

    @Autowired
    OntologyRepositoryService ontologyRepositoryService;

    @Autowired(required=false)
    List<OntologyIndexer> indexers;

    @Autowired
    DatabaseService databaseService;
    
    @Autowired 
    OntologyLoadingConfiguration ontologyLoadingConfiguration;

    @Override
    public boolean indexOntologyDocument(OntologyDocument document) throws IndexingException {

        OntologyLoader loader = null;
        Collection<IRI> classes;
        Collection<IRI> properties;
        Collection<IRI> individuals;
        String message = "";
        Status status = Status.LOADING;
        boolean result = false;

      logger.trace("annotationproperty.preferredroot.term = " + 
		ontologyLoadingConfiguration.getPreferredRootTermAnnotationProperty());

        ResourceUsage.logUsage(logger, "#### Monitoring ", document.getOntologyId() +
                ":Before loading ontology", ":");
        try {
            loader = OntologyLoaderFactory.getLoader(document.getConfig(), databaseService,
            		ontologyLoadingConfiguration);
            if (document.getLocalPath() != null) {
                // if updated get local path, and set location to local file
                loader.setOntologyResource(new FileSystemResource(document.getLocalPath()));
            }
            classes = loader.getAllClasses();
            properties = loader.getAllObjectPropertyIRIs();
            individuals = loader.getAllIndividualIRIs();


            // this means that file parsed, but had nothing in it, which is a bit suspect - indexing should fail until we undertand why/how this could happen
            if (classes.size() + properties.size() + individuals.size()== 0) {
            	logger.error("A suspiciously small or zero classes or properties found in latest version of " + loader.getOntologyName() + ": Won't index!");
                message = "Failed to load - last update had no classes or properties so was rejected";
                document.setStatus(Status.FAILED);
                document.setMessage(message);
                ontologyRepositoryService.update(document);
                // don't try to index, just return
                throw new IndexingException("Empty ontology found", new RuntimeException());
            }

        } catch (Throwable t) {
            message = t.getMessage();
            logger.error(message, t);
            document.setStatus(Status.FAILED);
            document.setMessage(message);
            ontologyRepositoryService.update(document);
            // just set document to failed and return
            return result;
        }
        ResourceUsage.logUsage(logger, "#### Monitoring ",document.getOntologyId() +
                ":After loading ontology, before indexing ontology", ":");
        document.setStatus(Status.LOADING);
        ontologyRepositoryService.update(document);
        // if we get to here, we should have at least loaded the ontology
        try {

            // get all the available indexers
            for (OntologyIndexer indexer : indexers) {
                // create the new index
                indexer.dropIndex(loader.getOntologyName());
                ResourceUsage.logUsage(logger, "#### Monitoring ",document.getOntologyId() +
                        ":After dropping index" + indexer.toString(), ":");
                indexer.createIndex(loader);
                ResourceUsage.logUsage(logger, "#### Monitoring ", document.getOntologyId() +
                        ":After after creating index for " + indexer.toString(), ":");
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
            if (loader.getTracker() != null) {
                config.setTracker(loader.getTracker());
            }
            if (loader.getLogo() != null) {
                config.setLogo(loader.getLogo());
            }
            if (!loader.getCreators().isEmpty()) {
                config.setCreators(loader.getCreators());
            }
            if (!loader.getOntologyAnnotations().keySet().isEmpty()) {
                config.setAnnotations(loader.getOntologyAnnotations());
            }
            if (loader.getOntologyVersionIRI() != null) {
                config.setVersionIri(loader.getOntologyVersionIRI().toString());
            }
            if (!loader.getInternalMetadataProperties().isEmpty()) {
                config.setInternalMetadataProperties(loader.getInternalMetadataProperties());
            }
            if (!loader.getOntologyIRI().toString().equals(config.getId())) {
                config.setId(loader.getOntologyIRI().toString());
            }

            // check for a version number or set to today date
            if (loader.getVersionNumber() != null) {
                config.setVersion(loader.getVersionNumber());
            }
            document.setConfig(config);
            document.setNumberOfTerms(classes.size());
            document.setNumberOfProperties(properties.size());
            document.setNumberOfIndividuals(individuals.size());
            status = Status.LOADED;
            document.setLoaded(new Date());
            result = true;
        } catch (Throwable t) {
        	logger.error("Error indexing " + document.getOntologyId(), t);
            status = Status.FAILED;
            message = t.getMessage();
        }
        finally {

            document.setStatus(status);
            document.setUpdated(new Date());
            document.setMessage(message);
            ontologyRepositoryService.update(document);
            return result;
        }
    }

    @Override
    public void removeOntologyDocumentFromIndex(OntologyDocument document) throws IndexingException {
        String message = "";
        Status status = Status.FAILED;

        try {

            // get all the available indexers
            for (OntologyIndexer indexer : indexers) {
                // delete the ontology
                indexer.dropIndex(document.getOntologyId());
            }
            status = Status.REMOVED;

        } catch (Throwable t) {
        	logger.error("Error removing index for " + document.getOntologyId(), t.getMessage());
            status = Status.FAILED;
            message = t.getMessage();
            throw t;
        }
        finally {
            document.setStatus(status);
            document.setUpdated(new Date());
            document.setMessage(message);
            ontologyRepositoryService.update(document);
        }
    }
}
