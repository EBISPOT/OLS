package uk.ac.ebi.spot.ols.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;

import java.util.List;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface MongoOntologyRepository extends MongoRepository<OntologyDocument, String> {

    List<OntologyDocument> findByStatus(Status status);

    OntologyDocument findByOntologyId(String documentId);
}
