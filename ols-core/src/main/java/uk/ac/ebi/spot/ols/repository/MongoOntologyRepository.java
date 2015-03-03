package uk.ac.ebi.spot.ols.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.OntologyRepository;

import java.util.List;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface MongoOntologyRepository extends OntologyRepository, MongoRepository<OntologyDocument, String> {

}
