package uk.ac.ebi.spot.ols.neo4j.model;

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
    Long id;

    @Indexed(fieldName = "olsId")
    private String olsId;
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

    @RelatedTo(direction = Direction.OUTGOING, type = "SUBCLASSOF")
    private Set<Term> subclassOf;

    @RelatedTo(direction = Direction.INCOMING, type = "SUBCLASSOF")
    private Set<Term> superclassOf = new HashSet<>();


    public Set<Term> getSubclassOf() {
        return subclassOf;
    }

    public void setSubclassOf(Set<Term> subclassOf) {
        this.subclassOf = subclassOf;
    }

//    public Set<Term> getSuperclassOf() {
//        return superclassOf;
//    }
//
//    public void setSuperclassOf(Set<Term> superclassOf) {
//        this.superclassOf = superclassOf;
//    }

//    @RelatedToVia
//    @Fetch Set<Related> related;

    public Long getId() {
        return id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

//    public Set<Term> getSubclassOf() {
//        return subclassOf;
//    }
//
//    public void setSubclassOf(Set<Term> subclassOf) {
//        this.subclassOf = subclassOf;
//    }

    public Term() {
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



}
