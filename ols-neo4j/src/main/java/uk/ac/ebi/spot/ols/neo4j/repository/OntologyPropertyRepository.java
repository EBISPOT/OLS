package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.ac.ebi.spot.ols.neo4j.model.OlsProperty;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource(collectionResourceRel = "properties", exported = false)
public interface OntologyPropertyRepository extends Neo4jRepository<OlsProperty, Long> {

    @Query(
            countQuery = "MATCH (n:Property)-[:SUBPROPERTYOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(parent)",
            value = "MATCH (n:Property)-[:SUBPROPERTYOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN parent")
    Page<OlsProperty> getParents(String ontologyName, String iri, Pageable pageable);

    @Query( countQuery = "MATCH (n:Property)<-[:SUBPROPERTYOF]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(child)",
            value = "MATCH (n:Property)<-[:SUBPROPERTYOF]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN child")
    Page<OlsProperty> getChildren(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Property)<-[:SUBPROPERTYOF*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct child)",
            value = "MATCH (n:Property)<-[:SUBPROPERTYOF*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct child")
    Page<OlsProperty> getDescendants(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Property)-[:SUBPROPERTYOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct parent)",
                value = "MATCH (n:Property)-[:SUBPROPERTYOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct parent")
    Page<OlsProperty> getAncestors(String ontologyName, String iri, Pageable pageable);

    @Query (value = "MATCH (n:Property) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN n")
    OlsProperty findByOntologyAndIri(String ontologyName, String iri);

    @Query (
            countQuery = "MATCH (n:Property) WHERE n.ontology_name = {0} RETURN count(n)",
            value = "MATCH (n:Property {ontology_name : {0}}) RETURN n")
    Page<OlsProperty> findAllByOntology(String ontologyName, Pageable pageable);

    @Query (value = "MATCH (n:Property) WHERE n.ontology_name = {0} AND n.short_form = {1} RETURN n")
    OlsProperty findByOntologyAndShortForm(String ontologyId, String shortForm);

    @Query (value = "MATCH (n:Property) WHERE n.ontology_name = {0} AND n.obo_id = {1} RETURN n")
    OlsProperty findByOntologyAndOboId(String ontologyId, String oboId);

    @Query (countQuery =  "MATCH (n:Property)-[SUBPROPERTYOF]->(r:Root) WHERE r.ontology_name = {0} AND n.is_obsolete = {1}  RETURN count(n)",
            value = "MATCH (n:Property)-[SUBPROPERTYOF]->(r:Root) WHERE r.ontology_name = {0} AND n.is_obsolete = {1}  RETURN n")
    Page<OlsProperty> getRoots(String ontologyId, boolean obsolete, Pageable pageable);

    @Query (countQuery = "MATCH (n:Property) RETURN count(n)",
            value = "MATCH (n:Property) RETURN n")
    Page<OlsProperty> findAll(Pageable pageable);

    @Query (countQuery = "MATCH (n:Property) WHERE n.is_defining_ontology = true RETURN count(n)",
            value = "MATCH (n:Property) WHERE n.is_defining_ontology = true RETURN n")
    Page<OlsProperty> findAllByIsDefiningOntology(Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Property) WHERE n.iri = {0} RETURN count(n)",
            value = "MATCH (n:Property) WHERE n.iri = {0} RETURN n")
    Page<OlsProperty> findAllByIri(String iri, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Property) WHERE n.iri = {0} AND n.is_defining_ontology = true "
    				+ "RETURN count(n)",
            value = "MATCH (n:Property) WHERE n.iri = {0} AND n.is_defining_ontology = true RETURN n")
    Page<OlsProperty> findAllByIriAndIsDefiningOntology(String iri, Pageable pageable);

    @Query (countQuery = "MATCH (n:Property) WHERE n.short_form = {0} RETURN count(n)",
            value = "MATCH (n:Property) WHERE n.short_form = {0} RETURN n")
    Page<OlsProperty> findAllByShortForm(String shortForm, Pageable pageable);

    @Query (countQuery = "MATCH (n:Property) WHERE n.short_form = {0} AND "
    				+ "n.is_defining_ontology = true  RETURN count(n)",
            value = "MATCH (n:Property) WHERE n.short_form = {0} AND n.is_defining_ontology = true "
            		+ "RETURN n")
    Page<OlsProperty> findAllByShortFormAndIsDefiningOntology(String shortForm, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Property) WHERE n.obo_id = {0} RETURN count(n)",
            value = "MATCH (n:Property) WHERE n.obo_id = {0} RETURN n")
    Page<OlsProperty> findAllByOboId(String oboId, Pageable pageable);

    @Query (countQuery = "MATCH (n:Property) WHERE n.obo_id = {0} AND n.is_defining_ontology = true "
    				+ "RETURN count(n)",
            value = "MATCH (n:Property) WHERE n.obo_id = {0} AND n.is_defining_ontology = true "
            		+ "RETURN n")
    Page<OlsProperty> findAllByOboIdAndIsDefiningOntology(String oboId, Pageable pageable);
}
