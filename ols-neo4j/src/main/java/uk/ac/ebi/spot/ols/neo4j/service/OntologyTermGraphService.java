package uk.ac.ebi.spot.ols.neo4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyTermRepository;

import java.io.IOException;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Service
public class OntologyTermGraphService {

    @Autowired(required = false)
    OntologyTermRepository termRepository;

    @Autowired(required = false)
    GraphDatabaseService graphDatabaseService;


    String parentGraphQuery =     "MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(parent)\n"+
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND nodes(path) as p\n" +
            "UNWIND rels(path) as r1\n" +
            "RETURN {nodes: collect( distinct {iri: p.iri, label: p.label}), edges: collect (distinct {source: startNode(r1).iri, target: endNode(r1).iri, label: r1.label, uri: r1.uri}  )} as result";

    String parentTreeQuery = "MATCH path = (n:Class)-[r:SUBCLASSOF*]->(parent)\n"+
            "USING INDEX n:Class(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, collect( distinct id(endNode(r1)) ) as parents";

    String parentSiblingTreeQuery = "MATCH path = (n:Class)-[r:SUBCLASSOF*]->(parent)<-[r2:SUBCLASSOF]-(n1:Class)\n"+
            "USING INDEX n:Class(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, collect( distinct id(endNode(r1)) ) as parents";

    @Transactional
    public Object getJsTree(String ontologyName, String iri) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
        Result res = graphDatabaseService.execute(parentTreeQuery, paramt);

        JsTreeBuilder builder = new JsTreeBuilder("Thing");
        return builder.getJsTreeObject(ontologyName, iri, res);

    }



    public Page<Term> findAllByOntology(String ontologyId, Pageable pageable) {
        return termRepository.findAllByOntology(ontologyId, pageable);
    }

    public Term findByOntologyAndIri(String ontologyname, String iri) {
        return termRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<Term> getParents(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getParents(ontologyName, iri, pageable);
    }

    public Page<Term> getChildren(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getChildren(ontologyName, iri, pageable);
    }

    public Page<Term> getDescendants(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getDescendants(ontologyName, iri, pageable);
    }

    public Page<Term> getAncestors(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getAncestors(ontologyName, iri, pageable);
    }

    public Page<Term> getRelated(String ontologyId, String iri, String relation, Pageable pageable) {
        return termRepository.getRelated(ontologyId, iri, relation, pageable);
    }

    public Term findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return termRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public Term findByOntologyAndOboId(String ontologyId, String oboId) {
        return termRepository.findByOntologyAndOboId(ontologyId, oboId);
    }


}
