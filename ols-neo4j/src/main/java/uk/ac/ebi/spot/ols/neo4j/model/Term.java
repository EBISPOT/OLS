package uk.ac.ebi.spot.ols.neo4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.spot.ols.util.OBODefinitionCitation;
import uk.ac.ebi.spot.ols.util.OBOSynonym;
import uk.ac.ebi.spot.ols.util.OBOXref;

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
    private String olsId;

    @GraphProperty(propertyName="iri")
    private String iri;

    @GraphProperty(propertyName="label")
    private String label;

    @GraphProperty(propertyName="synonym")
    private Set<String> synonym;

    @GraphProperty(propertyName="description")
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

    @JsonIgnore
    private Set<String> superClassDescription;

    @JsonIgnore
    private Set<String> equivalentClassDescription;

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
//    private List<String> subsets;

    private DynamicProperties annotation = new DynamicPropertiesContainer();

    @GraphProperty(propertyName="obo_definition_citation")
    @JsonProperty(value = "obo_definition_citation")
    @JsonRawValue
    private Set<String> oboDefinitionCitations;

    @GraphProperty(propertyName="obo_xref")
    @JsonProperty(value = "obo_xref")
    @JsonRawValue
    private Set<String> oboXrefs;

    @GraphProperty(propertyName="obo_synonym")
    @JsonProperty(value = "obo_synonym")
    @JsonRawValue
    private Set<String> oboSynonyms;

    @JsonIgnore
    @RelatedToVia
    @Fetch Set<Related> related;

    public Term() {
    }

    /**
     * This method gets the distinct set of relations by relation label
     * @return
     */
    public Set<Related> getRelated() {
        Set<Related> unique = new HashSet<>();
        Set<String> seen = new HashSet<>();
        for (Related related1 : related) {
            if (!seen.contains(related1.getLabel())){
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

    public String getOntologyPrefix() {
        return ontologyPrefix;
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

    @JsonProperty(value = "is_obsolete")
    public boolean isObsolete() {
        return isObsolete;
    }

    public Map getAnnotation() {
        return new TreeMap<String, Object>(annotation.asMap());
    }

    @JsonProperty(value = "is_defining_ontology")
    public boolean isLocal() {
        return isLocal;
    }

    @JsonProperty(value = "is_root")
    public boolean isRoot() {
        return isRoot;
    }

    @JsonProperty(value = "has_children")
    public boolean hasChildren() {
        return hasChildren;
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

    public Set<String> getOboDefinitionCitations() {
        return oboDefinitionCitations;
    }

    public Set<String> getOboXrefs() {
        return oboXrefs;
    }

    public Set<String> getOboSynonyms() {
        return oboSynonyms;
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
