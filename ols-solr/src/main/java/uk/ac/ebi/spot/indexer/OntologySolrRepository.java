package uk.ac.ebi.spot.indexer;

import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.TermDocument;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.util.List;

/**
 * @author Simon Jupp
 * @date 30/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface OntologySolrRepository extends SolrCrudRepository<TermDocument, String> {

    public List<TermDocument> findByOntologyName(String ontologyName);
}