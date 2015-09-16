package uk.ac.ebi.spot.ols.repository.mongo;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;

import java.util.List;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource(itemResourceRel = "ontology", collectionResourceRel = "ontologies", exported = false)
public interface MongoOntologyRepository extends MongoRepository<OntologyDocument, String> {

    List<OntologyDocument> findByStatus(Status status, Sort sort);

    OntologyDocument findByOntologyId(String documentId);
}
