package uk.ac.ebi.spot.ols.neo4j.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @GraphProperty(propertyName="ontology_name")
    @JsonProperty(value = "ontology_name")
    String ontologyName;

    @StartNode
    private Term relatedFrom;

    @EndNode
    private Term relatedTo;

    public Term getRelatedFrom() {
        return relatedFrom;
    }

    public Term getRelatedTo() {
        return relatedTo;
    }

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
