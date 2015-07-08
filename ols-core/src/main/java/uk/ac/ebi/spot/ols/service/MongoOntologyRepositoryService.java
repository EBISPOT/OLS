package uk.ac.ebi.spot.ols.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.exception.OntologyRepositoryException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.repository.mongo.MongoOntologyRepository;

import java.util.List;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class MongoOntologyRepositoryService implements OntologyRepositoryService {

    @Autowired
    MongoOntologyRepository repositoryService;

    @Override
    public List<OntologyDocument> getAllDocuments() {
        return repositoryService.findAll();
    }

    @Override
    public Page<OntologyDocument> getAllDocuments(Pageable pageable) {
        return repositoryService.findAll(pageable);
    }

    @Override
    public List<OntologyDocument> getAllDocumentsByStatus(Status status) {
        return repositoryService.findByStatus(status);
    }

    @Override
    public void delete(OntologyDocument document) throws OntologyRepositoryException {
        repositoryService.delete(document);
    }

    @Override
    public OntologyDocument create(OntologyDocument document) throws OntologyRepositoryException {
        return repositoryService.save(document);
    }

    @Override
    public OntologyDocument update(OntologyDocument document) throws OntologyRepositoryException {
        return repositoryService.save(document);
    }

    @Override
    public OntologyDocument get(String documentId) {
        return repositoryService.findByOntologyId(documentId);
    }

}
