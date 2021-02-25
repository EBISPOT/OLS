package uk.ac.ebi.spot.ols.neo4j.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.GraphProperty;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.*;

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

    @GraphProperty(propertyName=IRI)
    private String iri;

    @GraphProperty(propertyName=LABEL)
    @JsonProperty(value = LABEL)
    private String label;

    @GraphProperty(propertyName=LOCALIZED_LABELS)
    @JsonProperty(value = LOCALIZED_LABELS)
    private DynamicProperties localizedLabels = new DynamicPropertiesContainer();

    @GraphProperty(propertyName=LOCALIZED_SYNONYMS)
    @JsonProperty(value = LOCALIZED_SYNONYMS)
    private DynamicProperties localizedSynonyms = new DynamicPropertiesContainer();

    @GraphProperty(propertyName=LOCALIZED_DESCRIPTIONS)
    @JsonProperty(value = LOCALIZED_DESCRIPTIONS)
    private DynamicProperties localizedDescriptions = new DynamicPropertiesContainer();

    @GraphProperty(propertyName=ONTOLOGY_NAME)
    @JsonProperty(value = ONTOLOGY_NAME)
    private String ontologyName;

    @GraphProperty(propertyName=ONTOLOGY_PREFIX)
    @JsonProperty(value = ONTOLOGY_PREFIX)
    private String ontologyPrefix;

    @GraphProperty(propertyName=ONTOLOGY_IRI)
    @JsonProperty(value = ONTOLOGY_IRI)
    private String ontologyIri;

    @JsonIgnore
    private Set<String> superClassDescription;

    @JsonIgnore
    private Set<String> equivalentClassDescription;

    @GraphProperty(propertyName=IS_OBSOLETE)
    @JsonProperty(value = IS_OBSOLETE)
    private boolean isObsolete;

    @GraphProperty(propertyName=TERM_REPLACED_BY)
    @JsonProperty(value = TERM_REPLACED_BY)
    private String termReplacedBy;

    @GraphProperty(propertyName=IS_DEFINING_ONTOLOGY)
    @JsonProperty(value = IS_DEFINING_ONTOLOGY)
    private boolean isLocal;

    @GraphProperty(propertyName=HAS_CHILDREN)
    @JsonProperty(value = HAS_CHILDREN)
    private boolean hasChildren;

    @GraphProperty(propertyName=IS_ROOT)
    @JsonProperty(value = IS_ROOT)
    private boolean isRoot;

    @GraphProperty(propertyName=SHORT_FORM)
    @JsonProperty(value = SHORT_FORM)
    private String shortForm;

    @GraphProperty(propertyName=OBO_ID)
    @JsonProperty(value = OBO_ID)
    private String oboId;

    @GraphProperty(propertyName=IN_SUBSET)
    @JsonProperty(value = IN_SUBSET)
    private Set<String> inSubsets;

    private DynamicProperties annotation = new DynamicPropertiesContainer();
    private DynamicProperties localizedAnnotation = new DynamicPropertiesContainer();

    @GraphProperty(propertyName=OBO_DEFINITION_CITATION)
    @JsonProperty(value = OBO_DEFINITION_CITATION)
    @JsonRawValue
    private Set<String> oboDefinitionCitations;

    @GraphProperty(propertyName=OBO_XREF)
    @JsonProperty(value = OBO_XREF)
    @JsonRawValue
    private Set<String> oboXrefs;

    @GraphProperty(propertyName=OBO_SYNONYM)
    @JsonProperty(value = OBO_SYNONYM)
    @JsonRawValue
    private Set<String> oboSynonyms;

    @JsonIgnore
    @RelatedToVia
    @Fetch Set<Related> related;

    @GraphProperty(propertyName=IS_PREFERRED_ROOT)
    @JsonProperty(value = IS_PREFERRED_ROOT)
    private boolean isPreferredRoot;
    
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

//    public void setLocalizedLabels(String lang, Set<String> labels) {
//        this.labels.setProperty(lang, labels);
//    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    public String getOlsId() {
        return olsId;
    }

//    public Map<String, Object> getLocalizedLabels() {
//        return localizedLabels.asMap();
//    }
//
//    public Map<String, Object> getLocalizedSynonyms() {
//        return localizedSynonyms.asMap();
//    }
//
//    public Map<String, Object> getLocalizedDescriptions() {
//        return localizedDescriptions.asMap();
//    }

    public String getOntologyName() {
        return ontologyName;
    }

    public String getOntologyPrefix() {
        return ontologyPrefix;
    }

    public String getOntologyIri() {
        return ontologyIri;
    }

    // public void setDescription(Set<String> description) {
    //     this.description = description;
    // }

    // public void setSynonyms(Set<String> synonyms) {
    //     this.synonym = synonyms;
    // }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public void setAnnotation(DynamicProperties annotation) {
        this.annotation = annotation;
    }

    public String getTermReplacedBy() {
        return termReplacedBy;
    }

    public void setTermReplacedBy(String termReplacedBy) {
        this.termReplacedBy = termReplacedBy;
    }

    @JsonProperty(value = IS_OBSOLETE)
    public boolean isObsolete() {
        return isObsolete;
    }

    public Map getAnnotation() {
        return new TreeMap<String, Object>(annotation.asMap());
    }

    @JsonProperty(value = IS_DEFINING_ONTOLOGY)
    public boolean isLocal() {
        return isLocal;
    }

    @JsonProperty(value = IS_ROOT)
    public boolean isRoot() {
        return isRoot;
    }

    @JsonProperty(value = HAS_CHILDREN)
    public boolean hasChildren() {
        return hasChildren;
    }

    public Set<String> getInSubsets() {
        return inSubsets;
    }

    public void setInSubsets(Set<String> inSubsets) {
        this.inSubsets = inSubsets;
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
    
    @JsonProperty(value = IS_PREFERRED_ROOT)
    public boolean isPreferredRoot() {
        return isPreferredRoot;
    }    
}
