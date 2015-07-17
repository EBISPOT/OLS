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
    private Set<String> synonym;
    private Set<String> description;
    private String ontologyName;
    private String ontologyIri;


    private Set<String> superClassDescription;
    private Set<String> equivalentClassDescription;

    private boolean isObsolete;

    private boolean isLocal;
    private boolean isLeafNode;
    private boolean isRoot;

    private String shortForm;
    private String oboId;
//    private List<String> subsets;

    private DynamicProperties annotation = new DynamicPropertiesContainer();

    @JsonIgnore
    @RelatedToVia
    @Fetch Set<Related> related;

    public Term() {
    }

    public Set<Related> getRelated() {
        Set<Related> unique = new HashSet<>();
        Set<String> seen = new HashSet<>();
        for (Related related1 : related) {
            if (seen.contains(related1.getLabel())){
                unique.add(related1);
            }
            seen.add(related1.getLabel());
        }
        return unique;
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
        return synonym;
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

    public void setDescription(Set<String> description) {
        this.description = description;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonym = synonyms;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public void setAnnotation(DynamicProperties annotation) {
        this.annotation = annotation;
    }

    public boolean isObsolete() {
        return isObsolete;
    }

    public Map getAnnotation() {
        return new TreeMap<String, Object>(annotation.asMap());
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

    public String getShortForm() {
        return shortForm;
    }

    public void setShortForm(String shortForm) {
        this.shortForm = shortForm;
    }

    public String getOboId() {
        return oboId;
    }

    public void setOboId(String oboId) {
        this.oboId = oboId;
    }


    public Set<String> getSuperClassDescription() {
        return superClassDescription;
    }

    public void setSuperClassDescription(Set<String> superClassDescription) {
        this.superClassDescription = superClassDescription;
    }

    public Set<String> getEquivalentClassDescription() {
        return equivalentClassDescription;
    }

    public void setEquivalentClassDescription(Set<String> equivalentClassDescription) {
        this.equivalentClassDescription = equivalentClassDescription;
    }
}
