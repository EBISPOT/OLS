package uk.ac.ebi.spot.ols.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.http.MediaType;
import uk.ac.ebi.spot.ols.model.OntologyDocument;

import java.net.URI;

/**
 * @author Simon Jupp
 * @date 10/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class OLSRestMVCConfiguration extends RepositoryRestConfigurerAdapter {


    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.getMetadataConfiguration().setAlpsEnabled(false);
        config.setBasePath("/api");
        config.exposeIdsFor(OntologyDocument.class, OntologyResourceConfig.class);

    }

}