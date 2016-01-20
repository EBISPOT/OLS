package uk.ac.ebi.spot.ols.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.util.ReasonerType;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Simon Jupp
 * @date 02/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class PropertyBasedLoadingService extends AbstractLoadingService {

    @Value("${ontology_uri:}")
    String id;

    @Value("${title:}")
    String title;

    @Value("${namespace:}")
    String namespace;

    @Value("${location:}")
    String location;

    @Autowired
    Environment environment;


    private OntologyResourceConfig config;

    public PropertyBasedLoadingService() {

    }

    public OntologyResourceConfig getConfiguration() {
        String preferredPrefix = namespace.toUpperCase();
        if (environment.containsProperty("preferred_prefix")) {
            preferredPrefix = environment.getProperty("preferred_prefix");
            namespace = preferredPrefix.toLowerCase();
        }

        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(id, title, namespace, URI.create(location));

        builder.setPreferredPrefix(preferredPrefix);


        if (environment.containsProperty("label_property")) {
            builder.setLabelProperty(URI.create(environment.getProperty("label_property")));
        }

        if (environment.containsProperty("definition_property")) {
            Collection<URI> uris = new HashSet<>();
            for (String uri : environment.getProperty("definition_property").split(",")) {
                uris.add(URI.create(uri));
            }
            builder.setDefinitionProperties(uris);
        }

        if (environment.containsProperty("synonym_property")) {
            Collection<URI> uris = new HashSet<>();
            for (String uri : environment.getProperty("synonym_property").split(",")) {
                uris.add(URI.create(uri));
            }
            builder.setSynonymProperties(uris);
        }

        if (environment.containsProperty("hierarchical_property")) {
            Collection<URI> uris = new HashSet<>();
            for (String uri : environment.getProperty("hierarchical_property").split(",")) {
                uris.add(URI.create(uri));
            }
            builder.setHierarchicalProperties(uris);
        }

        if (environment.containsProperty("hidden_property")) {
            Collection<URI> uris = new HashSet<>();
            for (String uri : environment.getProperty("hidden_property").split(",")) {
                uris.add(URI.create(uri));
            }
            builder.setHiddenProperties(uris);
        }

        if (environment.containsProperty("base_uri")) {
            Collection<String> uris = new HashSet<>();
            Collections.addAll(uris, environment.getProperty("base_uri").split(","));
            builder.setBaseUris(uris);
        }


        if (environment.containsProperty("reasoner")) {
            String reasoner = environment.getProperty("reasoner");
            ReasonerType reasonerType = ReasonerType.valueOf(reasoner.toUpperCase());
            if (reasonerType != null) {
                builder.setReasonerType(reasonerType);
            }
        }

        if (environment.containsProperty("oboSlims")) {
            builder.setOboSlims(Boolean.parseBoolean(environment.getProperty("oboSlims")));
        }

        if (environment.containsProperty("description")) {
            builder.setDescription(environment.getProperty("description"));
        }

        if (environment.containsProperty("homepage")) {
            builder.setDescription(environment.getProperty("homepage"));
        }

        if (environment.containsProperty("mailing_list")) {
            builder.setDescription(environment.getProperty("mailing_list"));
        }

        this.config = builder.build();
        return config;

    }

}
