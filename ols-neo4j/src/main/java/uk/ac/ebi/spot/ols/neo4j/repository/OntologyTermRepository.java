package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ebi.spot.ols.neo4j.model.TermNode;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface OntologyTermRepository extends GraphRepository<TermNode> {



}