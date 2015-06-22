package uk.ac.ebi.spot.ols.neo4j.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * @author Simon Jupp
 * @date 18/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity
@TypeAlias(value = "Resource")
public class Resource {

    @GraphId
    private Long id;

    private @Fetch String iri;
    private @Fetch String label;
}
