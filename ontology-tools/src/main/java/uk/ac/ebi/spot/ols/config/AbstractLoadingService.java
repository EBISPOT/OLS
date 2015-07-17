package uk.ac.ebi.spot.ols.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.*;

/**
 * @author Simon Jupp
 * @date 09/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public abstract class AbstractLoadingService implements DocumentLoadingService {

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Override
    public OntologyLoader getLoader() throws OntologyLoadingException {
        OntologyResourceConfig config = getConfiguration();

        getLog().info("Starting up with " + config.getId() + " - " + config.getTitle());

        return  OntologyLoaderFactory.getLoader(config);
    }


    public abstract OntologyResourceConfig getConfiguration ();
}
