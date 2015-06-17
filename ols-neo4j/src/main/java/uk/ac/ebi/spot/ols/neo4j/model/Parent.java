package uk.ac.ebi.spot.ols.neo4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
//@RestResource(rel = "parent")
@RelationshipEntity (type = "Parent")
public class Parent {

    @GraphId
    Long id;
    String uri;
    String label;
    String ontology_name;

    @StartNode
    private @Fetch TermNode child;

    @EndNode
    private @Fetch  TermNode parent;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOntology_name() {
        return ontology_name;
    }

    public void setOntology_name(String ontology_name) {
        this.ontology_name = ontology_name;
    }

    public Parent() {

    }

    public void setChild(TermNode child) {
        this.child = child;
    }

    public void setParent(TermNode parent) {
        this.parent = parent;
    }

    public TermNode getChild() {
        return child;
    }

    public TermNode getParent() {
        return parent;
    }
}
