package uk.ac.ebi.spot.ols.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * Javadocs go here!
 *
 * @author Julie McMurry adapted from Tony Burdett
 * @date 9 April 2015
 */
@Component
public class SearchConfiguration {
    @NotNull @Value("${search.server}")
    private URL server;
    @Value("${search.defaultFacet}")
    private String defaultFacet;

    public URL getOlsSearchServer() {
        return server;
    }

    public String getDefaultFacet() {
        return defaultFacet;
    }
}
