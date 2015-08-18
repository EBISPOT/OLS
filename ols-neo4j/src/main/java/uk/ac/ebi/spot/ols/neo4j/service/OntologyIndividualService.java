package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyIndividualRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Service
public class OntologyIndividualService {

    @Autowired(required = false)
    OntologyIndividualRepository individualRepository;

    @Autowired(required = false)
    GraphDatabaseService graphDatabaseService;

    String parentTreeQuery = "MATCH path = (n:Individual)-[r:INSTANCEOF|SUBCLASSOF*]->(parent)\n"+
            "USING INDEX n:Individual(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, collect( distinct id(endNode(r1)) ) as parents";

    String parentSiblingTreeQuery = "MATCH path = (n:Individual)-[r:INSTANCEOF|SUBCLASSOF*]->(parent)<-[r2:SUBCLASSOF]-(n1:Individual)\n"+
            "USING INDEX n:Individual(iri)\n" +
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



    public Page<Individual> findAllByOntology(String ontologyId, Pageable pageable) {
        return individualRepository.findAllByOntology(ontologyId, pageable);
    }

    public Individual findByOntologyAndIri(String ontologyname, String iri) {
        return individualRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<Term> getDirectTypes(String ontologyName, String iri, Pageable pageable) {
        return individualRepository.getDirectTypes(ontologyName, iri, pageable);
    }

    public Page<Term> getAllTypes(String ontologyName, String iri, Pageable pageable) {
        return individualRepository.getAllTypes(ontologyName, iri, pageable);
    }

    public Individual findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return individualRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public Individual findByOntologyAndOboId(String ontologyId, String oboId) {
        return individualRepository.findByOntologyAndOboId(ontologyId, oboId);
    }

}
