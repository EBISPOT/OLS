package uk.ac.ebi.spot.ols.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.SolrDocument;

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

    public SuggestDocument(String id, String autosuggest) {
        this.id = id;
        this.autosuggest = autosuggest;
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
