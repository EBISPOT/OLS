package uk.ac.ebi.spot.ols.model;

import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;

import java.util.Collection;

/**
 * @author Simon Jupp
 * @date 30/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * This is an interface for an ontology indexing services. Implementations are required
 * to create an index based on the ontology loader provided.
 *
 */
public interface OntologyIndexer {

    void createIndex (Collection<OntologyLoader> loader) throws IndexingException;

    void createIndex (OntologyLoader loader) throws IndexingException;

    void dropIndex (String ontologyName) throws IndexingException;

    void dropIndex (OntologyLoader loader) throws IndexingException;

}
