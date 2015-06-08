package uk.ac.ebi.spot.ols.service;

import uk.ac.ebi.spot.ols.exception.OntologyIndexingException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface OntologyIndexingService {

    void indexOntologyDocument(OntologyDocument document) throws OntologyIndexingException;

}
