package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.model.Term;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource(collectionResourceRel = "properties", exported = false)
public interface OntologyPropertyRepository extends GraphRepository<Property> {

    @Query(
            countQuery = "MATCH (n:Property)-[:SUBPROPERTYOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(parent)",
            value = "MATCH (n:Property)-[:SUBPROPERTYOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN parent")
    Page<Property> getParents(String ontologyName, String iri, Pageable pageable);

    @Query( countQuery = "MATCH (n:Property)<-[:SUBPROPERTYOF]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(child)",
            value = "MATCH (n:Property)<-[:SUBPROPERTYOF]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN child")
    Page<Property> getChildren(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Property)<-[:SUBPROPERTYOF*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(child)",
            value = "MATCH (n:Property)<-[:SUBPROPERTYOF*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN child")
    Page<Property> getDescendants(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Property)-[:SUBPROPERTYOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(parent)",
                value = "MATCH (n:Property)-[:SUBPROPERTYOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN parent")
    Page<Property> getAncestors(String ontologyName, String iri, Pageable pageable);

    @Query (value = "MATCH (n:Property) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN n")
    Property findByOntologyAndIri(String ontologyName, String iri);

    @Query (value = "MATCH (n:Property {ontology_name : {0}}) RETURN n")
    Page<Property> findAllByOntology(String ontologyName, Pageable pageable);

    @Query (value = "MATCH (n:Property) WHERE n.ontology_name = {0} AND n.short_form = {1} RETURN n")
    Property findByOntologyAndShortForm(String ontologyId, String shortForm);

    @Query (value = "MATCH (n:Property) WHERE n.ontology_name = {0} AND n.obo_id = {1} RETURN n")
    Property findByOntologyAndOboId(String ontologyId, String oboId);

    @Query (value = "MATCH (n:Property)-[SUBPROPERTYOF]->(r:Root) WHERE r.ontology_name = {0} AND n.is_obsolete = {1}  RETURN n")
    Page<Property> getRoots(String ontologyId, boolean obsolete, Pageable pageable);
}
