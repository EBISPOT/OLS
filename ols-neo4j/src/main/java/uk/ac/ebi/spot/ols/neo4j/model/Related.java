package uk.ac.ebi.spot.ols.neo4j.model;

import org.springframework.data.rest.core.annotation.RestResource;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RestResource(rel = "related")
public class Related extends  RelationshipNode {
}
