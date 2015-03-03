package uk.ac.ebi.spot.ols.service;

import uk.ac.ebi.spot.ols.exception.OntologyRepositoryException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;

import java.util.List;

/**
 * @author Simon Jupp
 * @date 29/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface OntologyRepositoryService {


    List<OntologyDocument> getAllDocuments();

    List<OntologyDocument> getAllDocumentsByStatus(Status status);

    void delete(OntologyDocument document) throws OntologyRepositoryException;

    OntologyDocument create(OntologyDocument document) throws OntologyRepositoryException;

    OntologyDocument update(OntologyDocument document) throws OntologyRepositoryException;

    OntologyDocument get(String documentId);

}
