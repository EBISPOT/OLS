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
    private OntologyLoader loader;

    @Override
    public OntologyLoader getLoader() throws OntologyLoadingException {
        OntologyResourceConfig config = getConfiguration();

        getLog().info("Starting up with " + config.getId() + " - " + config.getTitle());

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


    public abstract OntologyResourceConfig getConfiguration ();
}
