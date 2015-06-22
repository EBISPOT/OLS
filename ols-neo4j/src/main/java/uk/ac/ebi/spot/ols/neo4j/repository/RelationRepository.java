package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Related;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource
public interface RelationRepository
        extends GraphRepository<Related>
{

}
