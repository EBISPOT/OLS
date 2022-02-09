
package uk.ac.ebi.spot.ols.controller.api.localization;

import java.util.Set;
import java.util.Map;
import java.util.List;

import uk.ac.ebi.spot.ols.neo4j.model.Individual;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import org.springframework.hateoas.core.Relation;

import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.*;

@Relation(collectionRelation = "individuals")
public class LocalizedIndividual {

    public static LocalizedIndividual fromIndividual(String lang, Individual individual) {
	    LocalizedIndividual lt = new LocalizedIndividual();
	    lt.iri = individual.getIri();
	    lt.lang = lang;
	    lt.label = individual.getLabelByLang(lang);
	    lt.description = individual.getDescriptionsByLang(lang);
	    lt.synonyms = individual.getSynonymsByLang(lang);
	    lt.ontologyName = individual.getOntologyName();
	    lt.ontologyPrefix = individual.getOntologyPrefix();
	    lt.ontologyIri = individual.getOntologyIri();
	    lt.isObsolete = individual.isObsolete();
	    lt.shortForm = individual.getShortForm();
	    lt.oboId = individual.getOboId();
	    lt.annotation = individual.getAnnotationByLang(lang);

	    lt.type = individual.getType().stream().map(
		    term -> LocalizedTerm.fromTerm(lang, term)).toArray(LocalizedTerm[]::new);

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

    @JsonProperty(value = IN_SUBSET)
    public Set<String> inSubsets;

    public Map<String,Object> annotation;

    public LocalizedTerm[] type;
}



