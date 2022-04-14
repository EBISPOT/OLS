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

import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.*;

/**
 * @author Simon Jupp
 * @date 17/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity
@TypeAlias(value = "Individual")
public class Individual {

    @GraphId
    @JsonIgnore
    Long id;

    @JsonIgnore
    private String olsId;

    @GraphProperty(propertyName = "iri")
    private String iri;

    @GraphProperty(propertyName = "label")
    private String label;

    @GraphProperty(propertyName = "synonym")
    private Set<String> synonym;

    @GraphProperty(propertyName = "description")
    private Set<String> description;

    @GraphProperty(propertyName = LOCALIZED_LABELS)
    @JsonProperty(value = LOCALIZED_LABELS)
    private DynamicProperties localizedLabels = new DynamicPropertiesContainer();

    @GraphProperty(propertyName = LOCALIZED_SYNONYMS)
    @JsonProperty(value = LOCALIZED_SYNONYMS)
    private DynamicProperties localizedSynonyms = new DynamicPropertiesContainer();

    @GraphProperty(propertyName = LOCALIZED_DESCRIPTIONS)
    @JsonProperty(value = LOCALIZED_DESCRIPTIONS)
    private DynamicProperties localizedDescriptions = new DynamicPropertiesContainer();

    @GraphProperty(propertyName = "ontology_name")
    @JsonProperty(value = "ontology_name")
    private String ontologyName;

    @GraphProperty(propertyName = "ontology_prefix")
    @JsonProperty(value = "ontology_prefix")
    private String ontologyPrefix;

    @GraphProperty(propertyName = "ontology_iri")
    @JsonProperty(value = "ontology_iri")
    private String ontologyIri;

    @GraphProperty(propertyName = "is_obsolete")
    @JsonProperty(value = "is_obsolete")
    private boolean isObsolete;

    @GraphProperty(propertyName = "is_defining_ontology")
    @JsonProperty(value = "is_defining_ontology")
    private boolean isLocal;

    @GraphProperty(propertyName = "short_form")
    @JsonProperty(value = "short_form")
    private String shortForm;

    @GraphProperty(propertyName = "obo_id")
    @JsonProperty(value = "obo_id")
    private String oboId;

    private DynamicProperties annotation = new DynamicPropertiesContainer();
    private DynamicProperties localizedAnnotation = new DynamicPropertiesContainer();

    @RelatedTo(type = "INSTANCEOF", direction = Direction.OUTGOING)
    @Fetch
    Set<Term> type;

    public Individual() {
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

    public String[] getDescriptionsByLang(String lang) {

        String[] localizedDescriptions = (String[])
                this.localizedDescriptions.getProperty(lang);

        if (localizedDescriptions != null && localizedDescriptions.length > 0) {
            return (String[]) localizedDescriptions;
        }

        if (description != null) {
            return description.toArray(new String[0]);
        }

        return new String[0];
    }

    public String[] getSynonymsByLang(String lang) {

        String[] localizedSynonyms = (String[])
                this.localizedSynonyms.getProperty(lang);

        if (localizedSynonyms != null) {
            return localizedSynonyms;
        }

        if (synonym != null) {
            return synonym.toArray(new String[0]);
        }

        return new String[0];
    }

    public String getLabelByLang(String lang) {
        return getLabelsByLang(lang)[0];
    }

    public String[] getLabelsByLang(String lang) {

        String[] localizedLabels = (String[]) this.localizedLabels.getProperty(lang);

        if (localizedLabels != null && localizedLabels.length > 0) {
            return localizedLabels;
        }

        if (label != null) {
            return new String[]{label};
        }

        return new String[0];
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

    public Set<Term> getType() {
        return type;
    }

    public Map<String, Object> getAnnotationByLang(String lang) {

        Map<String, Object> localizedAnnotations = localizedAnnotation.asMap();

        Map<String, Object> res = new TreeMap<>();

        if ( (lang.equals("en") || lang.startsWith("en-")) && annotation != null) {
            res.putAll(annotation.asMap());
        }

        for (String k : localizedAnnotations.keySet()) {

		int n = k.lastIndexOf('-');

		if(n != -1) {
			String annoLang = k.substring(0, n);

			if (annoLang.equalsIgnoreCase(lang)) {
				res.put(k.substring(n + 1), localizedAnnotations.get(k));
			}
		}
        }

        return res;
    }

}
