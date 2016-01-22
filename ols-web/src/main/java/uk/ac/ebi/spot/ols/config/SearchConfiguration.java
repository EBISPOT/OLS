package uk.ac.ebi.spot.ols.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Simon Jupp
 * @date 07/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class SearchConfiguration {
    @NotNull
    @Value("${spring.data.solr.host}")
    private String server;

    @NotNull
    @Value("${ols.solr.search.core}")
    private String ontologyCore = "ontology";

    @NotNull
    @Value("${ols.solr.suggest.core}")
    private String suggestCore = "autocomplete";

    public URL getOlsSearchServer() throws MalformedURLException {
        return new URL(server + "/" + ontologyCore);
    }

    public URL getOlsSuggestServer() throws MalformedURLException {
        return new URL(server + "/" + suggestCore);
    }

}
