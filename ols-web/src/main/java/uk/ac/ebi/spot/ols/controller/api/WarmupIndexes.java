package uk.ac.ebi.spot.ols.controller.api;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyTermRepository;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;

/**
 * @author Simon Jupp
 * @date 08/11/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
public class WarmupIndexes {

//    @Autowired GraphDatabaseService db;


    @Autowired
    OntologyTermGraphService ontologyTermGraphService;


    @RequestMapping(path = "warmup", produces = {MediaType.TEXT_PLAIN_VALUE}, method = RequestMethod.GET)
    public HttpEntity<String> warmUp() {
        PageRequest pageRequest = new PageRequest(0,5);

        for (Term t : ontologyTermGraphService.findAll(pageRequest)) {
            ontologyTermGraphService.findAllByIri(t.getIri(), pageRequest);
            ontologyTermGraphService.findAllByOboId(t.getOboId(), pageRequest);
            ontologyTermGraphService.findAllByShortForm(t.getShortForm(), pageRequest);
            ontologyTermGraphService.findAllByOntology(t.getOntologyName(), pageRequest);

            ontologyTermGraphService.getAncestors(t.getOntologyName(), t.getIri(), pageRequest);
            ontologyTermGraphService.getParents(t.getOntologyName(), t.getIri(), pageRequest);
            ontologyTermGraphService.getChildren(t.getOntologyName(), t.getIri(), pageRequest);
            ontologyTermGraphService.getDescendants(t.getOntologyName(), t.getIri(), pageRequest);
            ontologyTermGraphService.getRelatedFrom(t.getOntologyName(), t.getIri());

            ontologyTermGraphService.getHierarchicalAncestors(t.getOntologyName(), t.getIri(), pageRequest);
            ontologyTermGraphService.getHierarchicalChildren(t.getOntologyName(), t.getIri(), pageRequest);
            ontologyTermGraphService.getHierarchicalDescendants(t.getOntologyName(), t.getIri(), pageRequest);
            ontologyTermGraphService.getHierarchicalParents(t.getOntologyName(), t.getIri(), pageRequest);

            ontologyTermGraphService.getGraphJson(t.getOntologyName(), t.getIri());
            ontologyTermGraphService.getInstances(t.getOntologyName(), t.getIri(), pageRequest);

            ontologyTermGraphService.getRoots(t.getOntologyName(), false, pageRequest);

        }
//        db.execute("MATCH (n)\n" +
//                "        OPTIONAL MATCH (n)-[r]->()\n" +
//                "       RETURN count(n.iri) + count(r.iri)")
//        ;
//
        return  new HttpEntity<String>("Warmed up and ready to go!");
    }

}
