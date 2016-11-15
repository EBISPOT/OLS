package uk.ac.ebi.spot.ols.controller.api;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Simon Jupp
 * @date 08/11/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
public class WarmupIndexes {

    @Autowired GraphDatabaseService db;
    @RequestMapping(path = "warmup", produces = {MediaType.TEXT_PLAIN_VALUE}, method = RequestMethod.GET)
    public HttpEntity<String> warmUp() {
        db.execute("MATCH (n)\n" +
                "        OPTIONAL MATCH (n)-[r]->()\n" +
                "        RETURN count(n.iri) + count(r.iri)")
        ;
        return  new HttpEntity<String>("Warmed up and ready to go!");
    }

}
