package uk.ac.ebi.spot.ols.neo4j.model;

import org.springframework.data.neo4j.annotation.*;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RelationshipEntity (type = "Related")
public class Related {

    @GraphId
    Long id;

    String uri;
    String label;
    String ontologyName;

    @StartNode
    private @Fetch Term relatedFrom;

    @EndNode
    private @Fetch Term relatedTo;

    public String getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public String getOntologyName() {
        return ontologyName;
    }
}
