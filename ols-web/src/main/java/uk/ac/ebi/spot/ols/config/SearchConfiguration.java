package uk.ac.ebi.spot.ols.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * @author Simon Jupp
 * @date 07/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class SearchConfiguration {
    @NotNull
    @Value("${search.server}")
    private URL server;
//    @Value("${search.defaultFacet}")
//    private String defaultFacet;

    public URL getOlsSearchServer() {
        return server;
    }

//    public String getDefaultFacet() {
//        return defaultFacet;
//    }
}
