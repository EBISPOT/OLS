package uk.ac.ebi.spot.ols.neo4j.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.*;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RelationshipEntity(type = "Related")
public class OlsRelated {

    @Id
    Long id;

    String uri;
    String label;

    @Property(name="ontology_name")
    @JsonProperty(value = "ontology_name")
    String ontologyName;

    @StartNode
    private OlsTerm relatedFrom;

    @EndNode
    private OlsTerm relatedTo;

    public OlsTerm getRelatedFrom() {
        return relatedFrom;
    }

    public OlsTerm getRelatedTo() {
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
