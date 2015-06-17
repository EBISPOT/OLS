package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import uk.ac.ebi.spot.ols.neo4j.model.Parent;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface RelationRepository
        extends GraphRepository<Parent>
{
}
