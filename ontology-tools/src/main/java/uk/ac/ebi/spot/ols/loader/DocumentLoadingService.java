package uk.ac.ebi.spot.ols.loader;

import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;

/**
 * @author Simon Jupp
 * @date 02/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * This interface takes an ontology document configuration and returns an OntologyDocument.
 * Implementations should handle how ontology meta data is used to load and transform an
 * ontology or vocabulary into an abstracted ontology document.
 *
 */
public interface DocumentLoadingService {

    OntologyLoader getLoader()  throws OntologyLoadingException;
}
