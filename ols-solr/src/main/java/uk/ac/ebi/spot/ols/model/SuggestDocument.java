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
//
//    @Field("ontology")
//    private List<String> ontology;

//    public SuggestDocument(String id, String autosuggest, List<String> ontology) {
//        this.id = id;
//        this.autosuggest = autosuggest;
//        this.ontology = ontology;
//    }

    public SuggestDocument() {
    }

    public SuggestDocument(String id, String autosuggest) {
        this.id = id;
        this.autosuggest = autosuggest;
    }

    public String getAutosuggest() {

        return autosuggest;
    }

    public void setAutosuggest(String autosuggest) {
        this.autosuggest = autosuggest;
    }
}
