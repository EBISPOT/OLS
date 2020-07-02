package uk.ac.ebi.spot.ols.neo4j.model;

import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.DESCRIPTION;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.HAS_CHILDREN;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IN_SUBSET;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IRI;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IS_DEFINING_ONTOLOGY;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IS_OBSOLETE;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IS_ROOT;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.LABEL;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.OBO_DEFINITION_CITATION;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.OBO_ID;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.OBO_SYNONYM;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.OBO_XREF;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.ONTOLOGY_IRI;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.ONTOLOGY_NAME;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.ONTOLOGY_PREFIX;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.SHORT_FORM;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.SYNONYM;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.TERM_REPLACED_BY;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IS_PREFERRED_ROOT;

import java.util.*;

import org.springframework.data.annotation.TypeAlias;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Properties;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * @author Simon Jupp
 * @date 30/04/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity
@TypeAlias(value = "Class")
public class OlsTerm {

    @Id
    @JsonIgnore
    Long id;

    @JsonIgnore
    private String olsId;

    @Property(name=IRI)
    private String iri;

    @Property(name=LABEL)
    private String label;

    @Property(name=SYNONYM)
    private Set<String> synonym;

    @Property(name=DESCRIPTION)
    private Set<String> description;

    @Property(name=ONTOLOGY_NAME)
    @JsonProperty(value = ONTOLOGY_NAME)
    private String ontologyName;

    @Property(name=ONTOLOGY_PREFIX)
    @JsonProperty(value = ONTOLOGY_PREFIX)
    private String ontologyPrefix;

    @Property(name=ONTOLOGY_IRI)
    @JsonProperty(value = ONTOLOGY_IRI)
    private String ontologyIri;

    @JsonIgnore
    private Set<String> superClassDescription;

    @JsonIgnore
    private Set<String> equivalentClassDescription;

    @Property(name=IS_OBSOLETE)
    @JsonProperty(value = IS_OBSOLETE)
    private boolean isObsolete;

    @Property(name=TERM_REPLACED_BY)
    @JsonProperty(value = TERM_REPLACED_BY)
    private String termReplacedBy;

    @Property(name=IS_DEFINING_ONTOLOGY)
    @JsonProperty(value = IS_DEFINING_ONTOLOGY)
    private boolean isLocal;

    @Property(name=HAS_CHILDREN)
    @JsonProperty(value = HAS_CHILDREN)
    private boolean hasChildren;

    @Property(name=IS_ROOT)
    @JsonProperty(value = IS_ROOT)
    private boolean isRoot;

    @Property(name=SHORT_FORM)
    @JsonProperty(value = SHORT_FORM)
    private String shortForm;

    @Property(name=OBO_ID)
    @JsonProperty(value = OBO_ID)
    private String oboId;

    @Property(name=IN_SUBSET)
    @JsonProperty(value = IN_SUBSET)
    private Set<String> inSubsets;

    @Properties
    private Map<String, String> annotation = new HashMap<>();

    @Property(name=OBO_DEFINITION_CITATION)
    @JsonProperty(value = OBO_DEFINITION_CITATION)
    @JsonRawValue
    private Set<String> oboDefinitionCitations;

    @Property(name=OBO_XREF)
    @JsonProperty(value = OBO_XREF)
    @JsonRawValue
    private Set<String> oboXrefs;

    @Property(name=OBO_SYNONYM)
    @JsonProperty(value = OBO_SYNONYM)
    @JsonRawValue
    private Set<String> oboSynonyms;

    @JsonIgnore
    @Relationship
    Set<OlsRelated> related;

    @Property(name=IS_PREFERRED_ROOT)
    @JsonProperty(value = IS_PREFERRED_ROOT)
    private boolean isPreferredRoot;
    
    public OlsTerm() {
    }

    /**
     * This method gets the distinct set of relations by relation label
     * @return
     */
    public Set<OlsRelated> getRelated() {
        Set<OlsRelated> unique = new HashSet<>();
        Set<String> seen = new HashSet<>();
        for (OlsRelated related1 : related) {
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
