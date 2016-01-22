package uk.ac.ebi.spot.ols.indexer;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.ols.model.SuggestDocument;
import uk.ac.ebi.spot.ols.model.TermDocument;

/**
 * @author Simon Jupp
 * @date 21/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource(exported = false)
public interface OntologySuggestRepository  extends SolrCrudRepository<SuggestDocument, String> {
}