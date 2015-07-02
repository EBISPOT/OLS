package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ebi.spot.ols.neo4j.model.Term;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource
public interface OntologyTermRepository extends GraphRepository<Term> {

    @Query(
            countQuery = "MATCH (n:Class)-[:SUBCLASSOF]->(parent) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN count(parent)",
            value = "MATCH (n:Class)-[:SUBCLASSOF]->(parent) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN parent")
    Page<Term> getParents(String ontologyName, String iri, Pageable pageable);

    @Query( countQuery = "MATCH (n:Class)<-[:SUBCLASSOF]-(child) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN count(child)",
            value = "MATCH (n:Class)<-[:SUBCLASSOF]-(child) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN child")
    Page<Term> getChildren(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Class)<-[:SUBCLASSOF*]-(child) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN count(child)",
            value = "MATCH (n:Class)<-[:SUBCLASSOF*]-(child) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN child")
    Page<Term> getDescendants(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Class)-[:SUBCLASSOF*]->(parent) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN count(parent)",
                value = "MATCH (n:Class)-[:SUBCLASSOF*]->(parent) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN parent")
    Page<Term> getAncestors(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Class)-[r:Related]->(related) WHERE n.ontologyName = {0} AND n.iri = {1} AND r.label = {2} RETURN count(related)",
                value = "MATCH (n:Class)-[r:Related]->(related) WHERE n.ontologyName = {0} AND n.iri = {1} AND r.label = {2} RETURN related")
    Page<Term> getRelated(String ontologyName, String iri, String relation, Pageable pageable);

    @Query (value = "MATCH (n:Class) WHERE n.ontologyName = {0} AND n.iri = {1} RETURN n")
    Term findByOntologyAndIri(String ontologyName, String iri);

    @Query (value = "MATCH (n:Class)  WHERE n.ontologyName = {0} RETURN n")
    Page<Term> findAllByOntology(String ontologyName, Pageable pageable);

}
