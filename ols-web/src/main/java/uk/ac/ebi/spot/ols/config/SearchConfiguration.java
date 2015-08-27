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
    @Value("${ols.solr.core}")
    private String core = "ontology";


    public URL getOlsSearchServer() throws MalformedURLException {
        return new URL(server + "/" + core);
    }

}
