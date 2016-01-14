package uk.ac.ebi.spot.ols.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;
import uk.ac.ebi.spot.ols.loader.*;

/**
 * @author Simon Jupp
 * @date 09/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * Abstract loading service that creates an ontology loader given an Ontology configuration document (OntologyResourceLoader)
 *
 */
public abstract class AbstractLoadingService implements DocumentLoadingService {

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Override
    public OntologyLoader getLoader() throws OntologyLoadingException {

        try {
            OntologyResourceConfig config = getConfiguration();
            getLog().info("Starting up loader with " + config.getId() + " - " + config.getTitle());

            return  OntologyLoaderFactory.getLoader(config);
        } catch (ConfigParsingException e) {
            throw new OntologyLoadingException("Can't get configuration for loader: " + e.getMessage());
        }

    }


    public abstract OntologyResourceConfig getConfiguration () throws ConfigParsingException;
}
