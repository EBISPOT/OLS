package uk.ac.ebi.spot.ols.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.List;

/**
 * @author Simon Jupp
 * @date 21/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@SolrDocument(solrCoreName = "autosuggest")
public class SuggestDocument {

    @Field("id")
    private String id;

    @Field("autosuggest")
    private String autosuggest;

    @Field("ontology_name")
    private String ontology;

    @Field("language")
    private String language;

    public SuggestDocument(String autosuggest, String ontology, String language) {
        this.id = autosuggest + ontology;
        this.autosuggest = autosuggest;
        this.ontology = ontology;
        this.language = language;
    }

    public SuggestDocument() {
    }

    public String getAutosuggest() {

        return autosuggest;
    }

    public void setAutosuggest(String autosuggest) {
        this.autosuggest = autosuggest;
    }
}
