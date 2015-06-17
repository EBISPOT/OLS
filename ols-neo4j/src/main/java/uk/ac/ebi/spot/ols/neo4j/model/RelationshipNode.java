package uk.ac.ebi.spot.ols.neo4j.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

/**
 * @author Simon Jupp
 * @date 15/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RelationshipEntity
public class RelationshipNode {

    @GraphId
    Long id;

    String uri;
    String label;
    String ontology_name;

    @StartNode
    private TermNode lhs;

    @EndNode
    private TermNode rhs;

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

    public RelationshipNode() {

    }

    public TermNode getLhs() {
        return lhs;
    }

    public void setLhs(TermNode lhs) {
        this.lhs = lhs;
    }

    public TermNode getRhs() {
        return rhs;
    }

    public void setRhs(TermNode rhs) {
        this.rhs = rhs;
    }
}
