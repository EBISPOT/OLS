package uk.ac.ebi.spot.ols.synchroniser;

import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by catherineleroy on 15/06/2015.
 */
public interface OntologyRessourceConfigsGetter {

    /**
     * Extract the information from the given yamlPath to return a collection of
     * @return
     * @throws IOException
     */
    public Collection<OntologyResourceConfig> getOntologyResourceConfigs() throws IOException ;

    }
