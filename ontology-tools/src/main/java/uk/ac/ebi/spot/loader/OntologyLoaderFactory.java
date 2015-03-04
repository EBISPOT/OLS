package uk.ac.ebi.spot.loader;

import uk.ac.ebi.spot.config.OntologyResourceConfig;
import uk.ac.ebi.spot.exception.OntologyLoadingException;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OntologyLoaderFactory {

    public static OntologyLoader getLoader(OntologyResourceConfig config) throws OntologyLoadingException {
        if (config.isClassify()) {
            return new HermitOWLOntologyLoader(config);
        }
        else if (config.isSkos()) {
            return new SKOSLoader(config);
        }
        else {
            return new ELKOWLOntologyLoader(config);
        }
    }
}
