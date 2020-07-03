package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.ols.neo4j.model.OlsIndividual;
import uk.ac.ebi.spot.ols.neo4j.model.OlsTerm;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyTermRepository;

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
            "RETURN {nodes: collect( distinct {iri: p.iri, label: p.label})[0..200], " +
            "edges: collect (distinct {source: startNode(r1).iri, target: endNode(r1).iri, label: r1.label, uri: r1.uri}  )[0..200]} as result";


    String relatedFromQuery =  "MATCH (x)-[r:Related]->(n:Class) WHERE n.ontology_name = {0} AND n.iri = {1} RETURN r.label as relation, collect( {iri: x.iri, label: x.label})[0..99] as terms limit 100";

    String usageQuery = "MATCH (n:Resource)<-[r:REFERSTO]-(x) WHERE n.iri = {0} RETURN distinct ({name: x.ontology_name, prefix: x.ontology_prefix}) as usage";
    private Collection<OlsIndividual> instances;


    public Object getGraphJson(String ontologyName, String iri) {
        return getGraphJson(ontologyName, iri, 1);
    }


    public Object getGraphJson(String ontologyName, String iri, int distance) {

        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
//        paramt.put("2",distance);

        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            Result res = tx.execute(relatedGraphQuery, paramt);
            tx.commit();

            return res.next().get("result");
        }
    }


    public Page<OlsTerm> findAll(Pageable pageable) {
        return termRepository.findAll(pageable);
    }

    public Page<OlsTerm> findAllByIri(String iri, Pageable pageable) {
        return termRepository.findAllByIri(iri, pageable);
    }

    public Page<OlsTerm> findAllByShortForm(String shortForm, Pageable pageable) {
        return termRepository.findAllByShortForm(shortForm, pageable);
    }

    public Page<OlsTerm> findAllByOboId(String oboId, Pageable pageable) {
        return termRepository.findAllByOboId(oboId, pageable);
    }

    public Page<OlsTerm> findAllByIsDefiningOntology(Pageable pageable) {
        return termRepository.findAllByIsDefiningOntology(pageable);
    }

    public Page<OlsTerm> findAllByIriAndIsDefiningOntology(String iri, Pageable pageable) {
        return termRepository.findAllByIriAndIsDefiningOntology(iri, pageable);
    }

    public Page<OlsTerm> findAllByShortFormAndIsDefiningOntology(String shortForm, Pageable pageable) {
        return termRepository.findAllByShortFormAndIsDefiningOntology(shortForm, pageable);
    }

    public Page<OlsTerm> findAllByOboIdAndIsDefiningOntology(String oboId, Pageable pageable) {
        return termRepository.findAllByOboIdAndIsDefiningOntology(oboId, pageable);
    }    
    
    public Page<OlsTerm> findAllByOntology(String ontologyId, Pageable pageable) {
        return termRepository.findAllByOntology(ontologyId, pageable);
    }

    public OlsTerm findByOntologyAndIri(String ontologyname, String iri) {
        return termRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<OlsTerm> getParents(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getParents(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getChildren(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getChildren(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getHierarchicalChildren(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalChildren(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getHierarchicalDescendants(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalDescendants(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getHierarchicalParents(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalParents(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getHierarchicalAncestors(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getHierarchicalAncestors(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getDescendants(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getDescendants(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getAncestors(String ontologyName, String iri, Pageable pageable) {
        return termRepository.getAncestors(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getRelated(String ontologyId, String iri, String relation, Pageable pageable) {
        return termRepository.getRelated(ontologyId, iri, relation, pageable);
    }

    public Map<String, Collection<Map<String, String>>> getRelatedFrom(String ontologyId, String iri) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyId);
        paramt.put("1", iri);

        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            Result res = tx.execute(relatedGraphQuery, paramt);
            tx.commit();

            Map<String, Collection<Map<String, String>>> relatedFromMap = new HashMap<>();
            while (res.hasNext()) {
                Map<String, Object> r = res.next();
                String relationLabel = r.get("relation").toString();
                relatedFromMap.put(relationLabel, (Collection<Map<String, String>>) r.get("terms"));
            }

            return relatedFromMap;
        }

    }

    public Collection<Map<String, String>> getOntologyUsage (String iri) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", iri);

        try ( Transaction tx = graphDatabaseService.beginTx() ) {
            Result res = tx.execute(usageQuery, paramt);
            tx.commit();

            Collection<Map<String, String>> usageInfo = new HashSet<>();
            while (res.hasNext()) {
                Map<String, Object> r = res.next();
                usageInfo.add((Map<String, String>) r.get("usage"));
            }

            return usageInfo;
        }
    }

    public OlsTerm findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return termRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public OlsTerm findByOntologyAndOboId(String ontologyId, String oboId) {
        return termRepository.findByOntologyAndOboId(ontologyId, oboId);
    }


    public Page<OlsTerm> getRoots(String ontologyId, boolean includeObsoletes, Pageable pageable) {
        return termRepository.getRoots(ontologyId, includeObsoletes, pageable);
    }

    public Page<OlsTerm> getPreferredRootTerms(String ontologyId, boolean includeObsoletes, Pageable pageable) {
        return termRepository.getPreferredRootTerms(ontologyId, includeObsoletes, pageable);
    }

    public long getPreferredRootTermCount(String ontologyId, boolean includeObsoletes) {
        return termRepository.getPreferredRootTermCount(ontologyId, includeObsoletes);
    }

    public Page<OlsIndividual> getInstances(String ontologyId, String iri, Pageable pageable) {
        return termRepository.getInstances(ontologyId, iri, pageable);
    }
}
