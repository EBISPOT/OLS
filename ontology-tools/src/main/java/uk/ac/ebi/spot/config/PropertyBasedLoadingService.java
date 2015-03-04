package uk.ac.ebi.spot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.exception.OntologyLoadingException;
import uk.ac.ebi.spot.loader.*;

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
public class PropertyBasedLoadingService implements DocumentLoadingService {

    @Value("${ontology_uri:}")
    String id;

    @Value("${title:}")
    String title;

    @Value("${namespace:}")
    String namespace;

    @Value("${location:}")
    String location;

    private OntologyLoader loader = null;

    @Autowired
    Environment environment;


    private OntologyResourceConfig config;

    public PropertyBasedLoadingService() {

    }

    @Override
    public OntologyLoader getLoader() throws OntologyLoadingException {

        config = getConfiguration();

        System.out.println("Starting up with " + id + " - " + title);

            if (config.isClassify()) {
                this.loader = new HermitOWLOntologyLoader(config);
            }
            else if (config.isSkos()) {
                this.loader = new SKOSLoader(config);
            }
            else {
                this.loader = new ELKOWLOntologyLoader(config);
            }

            return loader;
    }

    public OntologyResourceConfig getConfiguration() {
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(id, title, namespace, URI.create(location));


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


        if (environment.containsProperty("isInferred")) {
            builder.setInferred(Boolean.parseBoolean(environment.getProperty("isInferred")));
        }

        if (environment.containsProperty("classify")) {
            builder.setClassify(Boolean.parseBoolean(environment.getProperty("classify")));
        }

        if (environment.containsProperty("oboSlims")) {
            builder.setOboSlims(Boolean.parseBoolean(environment.getProperty("oboSlims")));
        }

        this.config = builder.build();
        return config;

    }

}
