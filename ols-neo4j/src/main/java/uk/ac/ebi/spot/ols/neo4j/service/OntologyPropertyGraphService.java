package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyPropertyRepository;

import java.util.*;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Service
public class OntologyPropertyGraphService {

    @Autowired(required = false)
    OntologyPropertyRepository ontologyPropertyRepository;

    @Autowired(required = false)
    GraphDatabaseService graphDatabaseService;


    String parentTreeQuery = "MATCH path = (n:Property)-[r:SUBPROPERTYOF*]->(parent)\n"+
            "USING INDEX n:Property(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, collect( distinct id(endNode(r1)) ) as parents";

    String parentSiblingTreeQuery = "MATCH path = (n:Property)-[r:SUBPROPERTYOF*]->(parent)<-[r2:SUBPROPERTYOF]-(n1:Property)\n"+
            "USING INDEX n:Property(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, collect( distinct id(endNode(r1)) ) as parents";

    @Transactional
    public Object getJsTree(String ontologyName, String iri) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
        Result res = graphDatabaseService.execute(parentTreeQuery, paramt);

        JsTreeBuilder builder = new JsTreeBuilder("TopObjectProperty");
        return builder.getJsTreeObject(ontologyName, iri, res);

    }

    public Page<Property> findAllByOntology(String ontologyId, Pageable pageable) {
        return ontologyPropertyRepository.findAllByOntology(ontologyId, pageable);
    }

    public Property findByOntologyAndIri(String ontologyname, String iri) {
        return ontologyPropertyRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<Property> getParents(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getParents(ontologyName, iri, pageable);
    }

    public Page<Property> getChildren(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getChildren(ontologyName, iri, pageable);
    }

    public Page<Property> getDescendants(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getDescendants(ontologyName, iri, pageable);
    }

    public Page<Property> getAncestors(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getAncestors(ontologyName, iri, pageable);
    }

    public Property findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return ontologyPropertyRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public Property findByOntologyAndOboId(String ontologyId, String oboId) {
        return ontologyPropertyRepository.findByOntologyAndOboId(ontologyId, oboId);
    }

}
