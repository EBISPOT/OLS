package uk.ac.ebi.spot.ols.neo4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.aspectj.lang.annotation.Aspect;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.core.EntityPath;
import org.springframework.data.neo4j.core.EntityState;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import java.util.*;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity
@TypeAlias(value = "Class")
public class Term {

    @GraphId
    @JsonIgnore
    Long id;

    @JsonIgnore
    @Indexed(fieldName = "olsId")
    private String olsId;

    private String iri;
    private String label;
    private Set<String> synonyms;
    private Set<String> description;
    private String ontologyName;
    private String ontologyIri;
    private boolean isObsolete;

    private boolean isLocal;
    private boolean isLeafNode;
    private boolean isRoot;

    private Set<String> shortForm;
//    private List<String> subsets;

    private DynamicProperties annotation = new DynamicPropertiesContainer();

    @RelatedToVia
    @Fetch Set<Related> related;

    public Term() {
    }

    public Set<Related> getRelated() {
        return related;
    }

    public Long getId() {
        return id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    public String getLabel() {
        return label;
    }

    public String getOlsId() {
        return olsId;
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

    public boolean isLocal() {
        return isLocal;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public boolean isLeafNode() {
        return isLeafNode;
    }
}
