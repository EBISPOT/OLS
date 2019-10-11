package uk.ac.ebi.spot.ols.neo4j.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.model.Term;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RepositoryRestResource(collectionResourceRel = "terms", exported = false)
public interface OntologyTermRepository extends GraphRepository<Term> {


    @Query(
            countQuery = "MATCH (n:Class)-[:SUBCLASSOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct parent)",
            value = "MATCH (n:Class)-[:SUBCLASSOF]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct parent")
    Page<Term> getParents(String ontologyName, String iri, Pageable pageable);

    @Query(
            countQuery = "MATCH (n:Class)-[:SUBCLASSOF|RelatedTree]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct parent)",
            value = "MATCH (n:Class)-[:SUBCLASSOF|RelatedTree]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct parent")
    Page<Term> getHierarchicalParents(String ontologyName, String iri, Pageable pageable);

    @Query(
            countQuery = "MATCH (n:Class)-[:SUBCLASSOF|RelatedTree*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct parent)",
            value = "MATCH (n:Class)-[:SUBCLASSOF|RelatedTree*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct parent")
    Page<Term> getHierarchicalAncestors(String ontologyName, String iri, Pageable pageable);

    @Query( countQuery = "MATCH (n:Class)<-[:SUBCLASSOF]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct child)",
            value = "MATCH (n:Class)<-[:SUBCLASSOF]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct child")
    Page<Term> getChildren(String ontologyName, String iri, Pageable pageable);

    @Query( countQuery = "MATCH (n:Class)<-[:SUBCLASSOF|RelatedTree]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct child)",
            value = "MATCH (n:Class)<-[:SUBCLASSOF|RelatedTree]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct child")
    Page<Term> getHierarchicalChildren(String ontologyName, String iri, Pageable pageable);

    @Query( countQuery = "MATCH (n:Class)<-[:SUBCLASSOF|RelatedTree*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct child)",
            value = "MATCH (n:Class)<-[:SUBCLASSOF|RelatedTree*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct child")
    Page<Term> getHierarchicalDescendants(String ontologyName, String iri, Pageable pageable);


    @Query(countQuery = "MATCH (n:Class)<-[:SUBCLASSOF*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct child)",
            value = "MATCH (n:Class)<-[:SUBCLASSOF*]-(child) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct child")
    Page<Term> getDescendants(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Class)-[:SUBCLASSOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN count(distinct parent)",
                value = "MATCH (n:Class)-[:SUBCLASSOF*]->(parent) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN distinct parent")
    Page<Term> getAncestors(String ontologyName, String iri, Pageable pageable);

    @Query(countQuery = "MATCH (n:Class)-[r:Related]->(related) WHERE n.ontology_name = {0} AND n.iri = {1} AND r.uri = {2} RETURN count(distinct related)",
                value = "MATCH (n:Class)-[r:Related]->(related) WHERE n.ontology_name = {0} AND n.iri = {1} AND r.uri = {2} RETURN distinct related")
    Page<Term> getRelated(String ontologyName, String iri, String relation, Pageable pageable);

    @Query (value = "MATCH (n:Class) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN n")
    Term findByOntologyAndIri(String ontologyName, String iri);

    @Query (countQuery = "MATCH (n:Class {ontology_name : {0}}) RETURN count(n)",
            value = "MATCH (n:Class {ontology_name : {0}}) RETURN n")
    Page<Term> findAllByOntology(String ontologyName, Pageable pageable);

    @Query (value = "MATCH (n:Class) WHERE n.ontology_name = {0} AND n.short_form = {1} RETURN n")
    Term findByOntologyAndShortForm(String ontologyId, String shortForm);

    @Query (value = "MATCH (n:Class) WHERE n.ontology_name = {0} AND n.obo_id = {1} RETURN n")
    Term findByOntologyAndOboId(String ontologyId, String oboId);

    @Query (countQuery = "MATCH (n:Class)-[SUBCLASSOF]->(r:Root) WHERE r.ontology_name = {0} AND n.is_obsolete = {1}  RETURN count(n)",
            value = "MATCH (n:Class)-[SUBCLASSOF]->(r:Root) WHERE r.ontology_name = {0} AND n.is_obsolete = {1}  RETURN n")
    Page<Term> getRoots(String ontologyId, boolean obsolete, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:PreferredRootTerm) WHERE n.ontology_name = {0} AND n.is_obsolete = {1} RETURN count(n)",
            value = "MATCH (n:PreferredRootTerm) WHERE n.ontology_name = {0} AND n.is_obsolete = {1} RETURN n")    
    Page<Term> getPreferredRootTerms(String ontologyId, boolean obsolete, Pageable pageable);

    @Query (value = "MATCH (n:PreferredRootTerm) WHERE n.ontology_name = {0} AND n.is_obsolete = {1} RETURN count(n)")
    long getPreferredRootTermCount(String ontologyId, boolean obsolete);

    @Query (countQuery = "MATCH (n:Class) RETURN count(n)",
            value = "MATCH (n:Class) RETURN n")
    Page<Term> findAll(Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Class) WHERE n.is_defining_ontology = true RETURN count(n)",
    		value = "MATCH (n:Class) WHERE n.is_defining_ontology = true RETURN n")
    Page<Term> findAllByIsDefiningOntology(Pageable pageable);    

    @Query (countQuery = "MATCH (n:Class) WHERE n.iri = {0} RETURN count(n)",
            value = "MATCH (n:Class) WHERE n.iri = {0} RETURN n")
    Page<Term> findAllByIri(String iri, Pageable pageable);

    @Query (countQuery = "MATCH (n:Class) WHERE n.iri = {0} AND n.is_defining_ontology = true "
    		+ "RETURN count(n)",
    		value = "MATCH (n:Class) WHERE n.iri = {0} AND n.is_defining_ontology = true RETURN n")
    Page<Term> findAllByIriAndIsDefiningOntology(String iri, Pageable pageable);
    		
    @Query (countQuery = "MATCH (n:Class) WHERE n.short_form = {0} RETURN count(n)",
            value = "MATCH (n:Class) WHERE n.short_form = {0} RETURN n")
    Page<Term> findAllByShortForm(String shortForm, Pageable pageable);

    @Query (countQuery = "MATCH (n:Class) WHERE n.short_form = {0} AND n.is_defining_ontology = true"
    		+ " RETURN count(n)",
    		value = "MATCH (n:Class) WHERE n.short_form = {0} AND n.is_defining_ontology = true RETURN n")
    Page<Term> findAllByShortFormAndIsDefiningOntology(String shortForm, Pageable pageable);
    
    @Query (countQuery = "MATCH (n:Class) WHERE n.obo_id = {0} RETURN count(n)",
            value = "MATCH (n:Class) WHERE n.obo_id = {0} RETURN n")
    Page<Term> findAllByOboId(String oboId, Pageable pageable);

    @Query (countQuery = "MATCH (n:Class) WHERE n.obo_id = {0} AND n.is_defining_ontology = true "
    		+ "RETURN count(n)",
    		value = "MATCH (n:Class) WHERE n.obo_id = {0} AND n.is_defining_ontology = true RETURN n")
    Page<Term> findAllByOboIdAndIsDefiningOntology(String oboId, Pageable pageable);
    
    @Query (countQuery = "MATCH (i:Individual)-[INSTANCEOF]->(c:Class) WHERE i.ontology_name = {0} AND c.iri = {1} RETURN count(i)",
            value = "MATCH (i:Individual)-[INSTANCEOF]->(c:Class) WHERE i.ontology_name = {0} AND c.iri = {1} RETURN i")
    Page<Individual> getInstances(String ontologyId, String iri, Pageable pageable);
}
