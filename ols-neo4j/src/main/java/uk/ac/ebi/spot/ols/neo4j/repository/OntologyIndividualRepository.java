package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ebi.spot.ols.neo4j.model.OlsIndividual;
import uk.ac.ebi.spot.ols.neo4j.model.OlsProperty;
import uk.ac.ebi.spot.ols.neo4j.model.OlsTerm;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource(collectionResourceRel = "individuals", exported = false)
public interface OntologyIndividualRepository  extends Neo4jRepository<OlsIndividual, Long> {

    @Query(
            countQuery = "MATCH (n:Individual)-[:INSTANCEOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(parent)",
            value = "MATCH (n:Individual)-[:INSTANCEOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN parent")
    Page<OlsTerm> getDirectTypes(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Individual)-[:INSTANCEOF|SUBCLASSOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct parent)",
                value = "MATCH (n:Individual)-[:INSTANCEOF|SUBCLASSOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct parent")
    Page<OlsTerm> getAllTypes(String ontologyName, String iri, Pageable pageable);

    @Query (value = "MATCH (n:Individual) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN n")
    OlsIndividual findByOntologyAndIri(String ontologyName, String iri);

    @Query (countQuery = "MATCH (n:Individual {ontology_name : {0}}) RETURN count(n)",
            value = "MATCH (n:Individual {ontology_name : {0}}) RETURN n")
    Page<OlsIndividual> findAllByOntology(String ontologyName, Pageable pageable);

    @Query (value = "MATCH (n:Individual) WHERE n.ontology_name = {0} AND n.short_form = {1} RETURN n")
    OlsIndividual findByOntologyAndShortForm(String ontologyId, String shortForm);

    @Query (value = "MATCH (n:Individual) WHERE n.ontology_name = {0} AND n.obo_id = {1} RETURN n")
    OlsIndividual findByOntologyAndOboId(String ontologyId, String oboId);

    @Query (countQuery = "MATCH (n:Individual) RETURN count(n)",
            value = "MATCH (n:Individual) RETURN n")
    Page<OlsIndividual> findAll(Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Individual) WHERE n.is_defining_ontology = true RETURN count(n)",
            value = "MATCH (n:Individual) WHERE n.is_defining_ontology = true RETURN n")
    Page<OlsIndividual> findAllByIsDefiningOntology(Pageable pageable);
    

    @Query (countQuery = "MATCH (n:Individual) WHERE n.iri = {0} RETURN count(n)",
            value = "MATCH (n:Individual) WHERE n.iri = {0} RETURN n")
    Page<OlsIndividual> findAllByIri(String iri, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Individual) WHERE n.iri = {0} AND n.is_defining_ontology = true "
    		+ "RETURN count(n)",
            value = "MATCH (n:Individual) WHERE n.iri = {0} AND n.is_defining_ontology = true RETURN n")
    Page<OlsIndividual> findAllByIriAndIsDefiningOntology(String iri, Pageable pageable);

    @Query (countQuery = "MATCH (n:Individual) WHERE n.short_form = {0} RETURN count(n)",
            value = "MATCH (n:Individual) WHERE n.short_form = {0} RETURN n")
    Page<OlsIndividual> findAllByShortForm(String shortForm, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Individual) WHERE n.short_form = {0} AND "
    		+ "n.is_defining_ontology = true RETURN count(n)",
            value = "MATCH (n:Individual) WHERE n.short_form = {0} AND n.is_defining_ontology = true "
            		+ "RETURN n")
    Page<OlsIndividual> findAllByShortFormAndIsDefiningOntology(String shortForm, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Individual) WHERE n.obo_id = {0} RETURN count(n)",
            value = "MATCH (n:Individual) WHERE n.obo_id = {0} RETURN n")
    Page<OlsIndividual> findAllByOboId(String oboId, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Individual) WHERE n.obo_id = {0} AND n.is_defining_ontology = true  "
    		+ "RETURN count(n)",
            value = "MATCH (n:Individual) WHERE n.obo_id = {0} AND n.is_defining_ontology = true RETURN n")
    Page<OlsIndividual> findAllByOboIdAndIsDefiningOntology(String oboId, Pageable pageable);

}
