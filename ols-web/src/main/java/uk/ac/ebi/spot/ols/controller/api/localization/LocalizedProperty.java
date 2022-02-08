package uk.ac.ebi.spot.ols.controller.api.localization;

import java.util.Set;
import java.util.Map;
import java.util.List;

import uk.ac.ebi.spot.ols.neo4j.model.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import org.springframework.hateoas.core.Relation;

import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.*;


@Relation(collectionRelation = "properties")
public class LocalizedProperty {

    public static LocalizedProperty fromProperty(String lang, Property property) {
	    LocalizedProperty lt = new LocalizedProperty();
	    lt.iri = property.getIri();
	    lt.lang = lang;
	    lt.label = property.getLabelByLang(lang);
	    lt.description = property.getDescriptionsByLang(lang);
	    lt.synonyms = property.getSynonymsByLang(lang);
	    lt.ontologyName = property.getOntologyName();
	    lt.ontologyPrefix = property.getOntologyPrefix();
	    lt.ontologyIri = property.getOntologyIri();
	    lt.isObsolete = property.isObsolete();
	    lt.isLocal = property.isLocal();
	    lt.hasChildren = property.hasChildren();
	    lt.isRoot = property.isRoot();
	    lt.shortForm = property.getShortForm();
	    lt.oboId = property.getOboId();
	    lt.annotation = property.getAnnotationByLang(lang);
	    return lt;
    }

    public String iri;

    public String lang;

    @JsonProperty(value = LABEL)
    public String label;

    public String[] description;
    public String[] synonyms;

    @JsonProperty(value = ONTOLOGY_NAME)
    public String ontologyName;

    @JsonProperty(value = ONTOLOGY_PREFIX)
    public String ontologyPrefix;

    @JsonProperty(value = ONTOLOGY_IRI)
    public String ontologyIri;
	
    @JsonProperty(value = IS_OBSOLETE)
    public boolean isObsolete;

    @JsonProperty(value = IS_DEFINING_ONTOLOGY)
    public boolean isLocal;

    @JsonProperty(value = HAS_CHILDREN)
    public boolean hasChildren;

    @JsonProperty(value = IS_ROOT)
    public boolean isRoot;

    @JsonProperty(value = SHORT_FORM)
    public String shortForm;

    @JsonProperty(value = OBO_ID)
    public String oboId;

    public Map<String,Object> annotation;
}
