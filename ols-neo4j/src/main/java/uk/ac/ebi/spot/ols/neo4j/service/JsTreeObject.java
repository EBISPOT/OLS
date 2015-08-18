package uk.ac.ebi.spot.ols.neo4j.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class JsTreeObject {

    private String id;
    private String parent;
    private String iri;
    private String ontologyName;
    private String text;
    private boolean hasChildren;
            private Map<String, Boolean> state;
    private boolean children;


    public JsTreeObject(String id, String iri, String ontologyName, String text, boolean hasChildren, String parent) {
        this.id = id;
        this.iri = iri;
        this.ontologyName = ontologyName;
        this.text = text;
        this.hasChildren = hasChildren;
        this.parent = parent;
        this.state = new HashMap<>();
        state.put("opened", true);
        this.children = false;

    }

    public boolean isChildren() {
        return children;
    }

    public void setChildren(boolean children) {
        this.children = children;
    }


    public void setState(Map<String, Boolean> state) {
        this.state = state;
    }

    public Map<String, Boolean> getState() {
        return state;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    @JsonProperty(value = "ontology_name")
    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonIgnore
    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setIsLeaf(boolean isLeaf) {
        this.hasChildren = isLeaf;
    }
}