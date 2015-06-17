package uk.ac.ebi.spot.ols.neo4j.model;

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

import java.io.Serializable;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity
@TypeAlias(value = "Class")
public class TermNode {

    @GraphId
    Long id;

    @Indexed(fieldName = "ols_id")
    private String ols_id;
    private String iri;
    private String label;
    private Set<String> synonyms;
    private Set<String> description;
    private String ontologyName;
    private String ontologyIri;
    private boolean isObsolete;

//    private List<String> subsets;
//    private Set<String> shortForm;

    private DynamicProperties annotation = new DynamicPropertiesContainer();

//    @RelatedTo(type = "PARENT")
//    private @Fetch Set<TermNode> parents;


    public Long getId() {
        return id;
    }

    @RelatedToVia (type = "Parent")
    @Fetch Set<Parent> parents = new HashSet<Parent>();

//    @RelatedToVia (direction= Direction.INCOMING, type = "CHILD")
//    Set<Child> children = new HashSet<Child>();
//
//    @RelatedToVia (direction= Direction.OUTGOING, type = "RELATED")
//    Set<Related> related = new HashSet<Related>();

    public void setLabel(String label) {
        this.label = label;
    }

    public TermNode() {
    }

    public String getIri() {
        return iri;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public Set<String> getDescription() {
        return description;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public String getOntologyIri() {
        return ontologyIri;
    }

    public boolean isObsolete() {
        return isObsolete;
    }

    public Map getAnnotation() {
        return annotation.asMap();
    }

//    public void setParents(Set<Parent> parents) {
//        this.parents = parents;
//    }

//    public Set<Parent> getParents() {
//        return parents;
//    }

//    public Set<Child> getChildren() {
//        return children;
//    }
//
//    public Set<Related> getRelated() {
//        return related;
//    }
}
