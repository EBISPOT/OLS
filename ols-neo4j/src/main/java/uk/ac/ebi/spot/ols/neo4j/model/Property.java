package uk.ac.ebi.spot.ols.neo4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Simon Jupp
 * @date 17/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity
@TypeAlias(value = "Property")
public class Property {

    @GraphId
    @JsonIgnore
    Long id;

    @JsonIgnore
    private String olsId;

    @GraphProperty(propertyName="iri")
    @JsonProperty(value = "iri")
    private String iri;

    @GraphProperty(propertyName="label")
    @JsonProperty(value = "label")
    private String label;

    @GraphProperty(propertyName="synonym")
    @JsonProperty(value = "synonym")
    private Set<String> synonym;

    @GraphProperty(propertyName="description")
    @JsonProperty(value = "description")
    private Set<String> description;

    @GraphProperty(propertyName="ontology_name")
    @JsonProperty(value = "ontology_name")
    private String ontologyName;

    @GraphProperty(propertyName="ontology_prefix")
    @JsonProperty(value = "ontology_prefix")
    private String ontologyPrefix;

    @GraphProperty(propertyName="ontology_iri")
    @JsonProperty(value = "ontology_iri")
    private String ontologyIri;

    @GraphProperty(propertyName="is_obsolete")
    @JsonProperty(value = "is_obsolete")
    private boolean isObsolete;

    @GraphProperty(propertyName="is_defining_ontology")
    @JsonProperty(value = "is_defining_ontology")
    private boolean isLocal;

    @GraphProperty(propertyName="has_children")
    @JsonProperty(value = "has_children")
    private boolean hasChildren;

    @GraphProperty(propertyName="is_root")
    @JsonProperty(value = "is_root")
    private boolean isRoot;

    @GraphProperty(propertyName="short_form")
    @JsonProperty(value = "short_form")
    private String shortForm;

    @GraphProperty(propertyName="obo_id")
    @JsonProperty(value = "obo_id")
    private String oboId;

    private DynamicProperties annotation = new DynamicPropertiesContainer();

    @RelatedTo(type="SUBPROPERTYOF", direction = Direction.OUTGOING)
    @Fetch Set<Property> parent;


    public String getIri() {
        return iri;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getSynonyms() {
        return synonym;
    }

    public Set<String> getDescription() {
        return description;
    }

    @JsonProperty(value = "ontology_name")
    public String getOntologyName() {
        return ontologyName;
    }

    public String getOntologyPrefix() {
        return ontologyPrefix;
    }

    public String getOntologyIri() {
        return ontologyIri;
    }

    @JsonProperty(value = "is_obsolete")
    public boolean isObsolete() {
        return isObsolete;
    }

    @JsonProperty(value = "is_defining_ontology")
    public boolean isLocal() {
        return isLocal;
    }

    @JsonProperty(value = "has_children")
    public boolean hasChildren() {
        return hasChildren;
    }

    @JsonProperty(value = "is_root")
    public boolean isRoot() {
        return isRoot;
    }

    public String getShortForm() {
        return shortForm;
    }

    public String getOboId() {
        return oboId;
    }

    public Map getAnnotation() {
        return new TreeMap<String, Object>(annotation.asMap());
    }


}
