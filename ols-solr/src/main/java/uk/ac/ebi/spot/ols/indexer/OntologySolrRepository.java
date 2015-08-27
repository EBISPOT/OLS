package uk.ac.ebi.spot.ols.indexer;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.ols.model.TermDocument;

import java.util.Collection;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 30/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource(exported = false)
public interface OntologySolrRepository extends SolrCrudRepository<TermDocument, String> {

    @Query("ontology_name:?0")
    Iterable<TermDocument> findByOntologyName(String ontologyName);
}