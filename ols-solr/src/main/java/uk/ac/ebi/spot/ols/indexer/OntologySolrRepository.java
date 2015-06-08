package uk.ac.ebi.spot.ols.indexer;

import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.ols.model.TermDocument;

/**
 * @author Simon Jupp
 * @date 30/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface OntologySolrRepository extends SolrCrudRepository<TermDocument, String> {

    public TermDocument findByOntologyName(String ontologyName);
}