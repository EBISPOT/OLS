package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Term;

import java.util.Collection;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource
public interface OntologyTermRepository extends GraphRepository<Term> {


}