
package uk.ac.ebi.spot.ols.controller.api.localization;

import java.util.Set;
import java.util.Map;
import java.util.List;

import uk.ac.ebi.spot.ols.neo4j.model.Term;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import org.springframework.hateoas.core.Relation;

import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.*;

import uk.ac.ebi.spot.ols.neo4j.model.Related;

@Relation(collectionRelation = "terms")
public class LocalizedTerm {

    public static LocalizedTerm fromTerm(String lang, Term term) {
	    LocalizedTerm lt = new LocalizedTerm();
	    lt.iri = term.getIri();
	    lt.lang = lang;
	    lt.label = term.getLabelByLang(lang);
	    lt.description = term.getDescriptionsByLang(lang);
	    lt.synonyms = term.getSynonymsByLang(lang);
	    lt.ontologyName = term.getOntologyName();
	    lt.ontologyPrefix = term.getOntologyPrefix();
	    lt.ontologyIri = term.getOntologyIri();
	    lt.isObsolete = term.isObsolete();
	    lt.termReplacedBy = term.getTermReplacedBy();
	    lt.isLocal = term.isLocal();
	    lt.hasChildren = term.hasChildren();
	    lt.isRoot = term.isRoot();
	    lt.shortForm = term.getShortForm();
	    lt.oboId = term.getOboId();
	    lt.inSubsets = term.getInSubsets();
	    lt.oboDefinitionCitations = term.getOboDefinitionCitations();
	    lt.oboXrefs = term.getOboXrefs();
	    lt.oboSynonyms = term.getOboSynonyms();
	    lt.isPreferredRoot = term.isPreferredRoot();
	    lt.annotation = term.getAnnotationByLang(lang);
	    lt.related = term.getRelated();
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

    @JsonProperty(value = TERM_REPLACED_BY)
    public String termReplacedBy;

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

    @JsonProperty(value = OBO_DEFINITION_CITATION)
    @JsonRawValue
    public Set<String> oboDefinitionCitations;

    @JsonProperty(value = OBO_XREF)
    @JsonRawValue
    public Set<String> oboXrefs;

    @JsonProperty(value = OBO_SYNONYM)
    @JsonRawValue
    public Set<String> oboSynonyms;

    @JsonProperty(value = IS_PREFERRED_ROOT)
    public boolean isPreferredRoot;

    @JsonIgnore
    public Set<Related> related;
}
