package uk.ac.ebi.spot.ols.neo4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.springframework.data.annotation.TypeAlias;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 17/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity
@TypeAlias(value = "Individual")
public class OlsIndividual {

    @Id
    @JsonIgnore
    Long id;

    @JsonIgnore
    private String olsId;

    @Property(name="iri")
    private String iri;

    @Property(name="label")
    private String label;

    @Property(name="synonym")
    private Set<String> synonym;

    @Property(name="description")
    private Set<String> description;

    @Property(name="ontology_name")
    @JsonProperty(value = "ontology_name")
    private String ontologyName;

    @Property(name="ontology_prefix")
    @JsonProperty(value = "ontology_prefix")
    private String ontologyPrefix;

    @Property(name="ontology_iri")
    @JsonProperty(value = "ontology_iri")
    private String ontologyIri;

    @Property(name="is_obsolete")
    @JsonProperty(value = "is_obsolete")
    private boolean isObsolete;

    @Property(name="is_defining_ontology")
    @JsonProperty(value = "is_defining_ontology")
    private boolean isLocal;

    @Property(name="short_form")
    @JsonProperty(value = "short_form")
    private String shortForm;

    @Property(name="obo_id")
    @JsonProperty(value = "obo_id")
    private String oboId;

    @Relationship(type="INSTANCEOF", direction = Relationship.OUTGOING)
    Set<OlsTerm> type;

    public OlsIndividual() {
    }

    public Long getId() {
        return id;
    }

    public String getOlsId() {
        return olsId;
    }

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

    public String getShortForm() {
        return shortForm;
    }

    public String getOboId() {
        return oboId;
    }

    public Set<OlsTerm> getType() {
        return type;
    }

}
