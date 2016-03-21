package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.model.Related;
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


    String relatedGraphQuery = "MATCH path = (n:Class)-[r:SUBCLASSOF|Related]-(parent)\n"+
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND nodes(path) as p\n" +
            "UNWIND rels(path) as r1\n" +
            "RETURN {nodes: collect( distinct {iri: p.iri, label: p.label}), edges: collect (distinct {source: startNode(r1).iri, target: endNode(r1).iri, label: r1.label, uri: r1.uri}  )} as result";


    String relatedFromQuery =  "MATCH (x)-[r:Related]->(n:Class) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN r.label as relation, collect( {iri: x.iri, label: x.label}) as terms limit 100";

    String usageQuery = "MATCH (n:Resource)<-[r:REFERSTO]-(x) WHERE n.iri = {0} RETURN distinct ({name: x.ontology_name, prefix: x.ontology_prefix}) as usage";
    private Collection<Individual> instances;


    public Object getGraphJson(String ontologyName, String iri) {
        return getGraphJson(ontologyName, iri, 1);
    }


    @Transactional
    public Object getGraphJson(String ontologyName, String iri, int distance) {

        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
//        paramt.put("2",distance);
        Result res = graphDatabaseService.execute(relatedGraphQuery, paramt);

        return res.next().get("result");

    }


    public Page<Term> findAll(Pageable pageable) {
        return termRepository.findAll(pageable);
    }

    public Page<Term> findAllByIri(String iri, Pageable pageable) {
        return termRepository.findAllByIri(iri, pageable);
    }

    public Page<Term> findAllByShortForm(String shortForm, Pageable pageable) {
        return termRepository.findAllByShortForm(shortForm, pageable);
    }

    public Page<Term> findAllByOboId(String oboId, Pageable pageable) {
        return termRepository.findAllByOboId(oboId, pageable);
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

    public Page<Term> getHierarchicalChildren(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalChildren(ontologyName, iri, pageable);
    }

    public Page<Term> getHierarchicalDescendants(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalDescendants(ontologyName, iri, pageable);
    }

    public Page<Term> getHierarchicalParents(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalParents(ontologyName, iri, pageable);
    }

    public Page<Term> getHierarchicalAncestors(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalAncestors(ontologyName, iri, pageable);
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

    @Transactional
    public Map<String, Collection<Map<String, String>>> getRelatedFrom(String ontologyId, String iri) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyId);
        paramt.put("1", iri);
        Result res = graphDatabaseService.execute(relatedFromQuery, paramt);

        Map<String, Collection<Map<String, String>>> relatedFromMap = new HashMap<>();
        while (res.hasNext()) {
            Map<String, Object> r = res.next();
            String relationLabel = r.get("relation").toString();
            relatedFromMap.put(relationLabel, (Collection<Map<String, String>>) r.get("terms"));
        }

        return relatedFromMap;
    }

    @Transactional
    public Collection<Map<String, String>> getOntologyUsage (String iri) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", iri);
        Result res = graphDatabaseService.execute(usageQuery,paramt);
        Collection<Map<String, String>> usageInfo = new HashSet<>();
        while (res.hasNext()) {
            Map<String, Object> r = res.next();
            usageInfo.add((Map<String, String>) r.get("usage"));
        }

        return usageInfo;
    }

    public Term findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return termRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public Term findByOntologyAndOboId(String ontologyId, String oboId) {
        return termRepository.findByOntologyAndOboId(ontologyId, oboId);
    }


    public Page<Term> getRoots(String ontologyId, boolean includeObsoletes, Pageable pageable) {
        return termRepository.getRoots(ontologyId, includeObsoletes, pageable);
    }

    public Collection<Individual> getInstances(String ontologyId, String iri) {
        return termRepository.getInstances(ontologyId, iri);
    }
}
