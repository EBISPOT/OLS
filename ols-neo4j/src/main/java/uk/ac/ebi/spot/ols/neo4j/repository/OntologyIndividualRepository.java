package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.model.Term;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public interface OntologyIndividualRepository  extends GraphRepository<Individual> {

    @Query(
            countQuery = "MATCH (n:Individual)-[:INSTANCEOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(parent)",
            value = "MATCH (n:Individual)-[:INSTANCEOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN parent")
    Page<Term> getDirectTypes(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Individual)-[:INSTANCEOF|SUBCLASSOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(parent)",
                value = "MATCH (n:Individual)-[:INSTANCEOF|SUBCLASSOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN parent")
    Page<Term> getAllTypes(String ontologyName, String iri, Pageable pageable);

    @Query (value = "MATCH (n:Individual) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN n")
    Individual findByOntologyAndIri(String ontologyName, String iri);

    @Query (value = "MATCH (n:Individual {ontology_name : {0}}) RETURN n")
    Page<Individual> findAllByOntology(String ontologyName, Pageable pageable);

    @Query (value = "MATCH (n:Individual) WHERE n.ontology_name = {0} AND n.short_form = {1} RETURN n")
    Individual findByOntologyAndShortForm(String ontologyId, String shortForm);

    @Query (value = "MATCH (n:Individual) WHERE n.ontology_name = {0} AND n.obo_id = {1} RETURN n")
    Individual findByOntologyAndOboId(String ontologyId, String oboId);
}
