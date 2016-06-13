package uk.ac.ebi.spot.ols.loader;

import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.util.ReasonerType;
import uk.ac.ebi.spot.ols.xrefs.Database;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;

import javax.xml.crypto.Data;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OntologyLoaderFactory {

    public static OntologyLoader getLoader(OntologyResourceConfig config) throws OntologyLoadingException {


        if (config.getReasonerType().equals(ReasonerType.OWL2)) {
            return new HermitOWLOntologyLoader(config);
        }
        else if (config.getReasonerType().equals(ReasonerType.EL)) {
            return new ELKOWLOntologyLoader(config);
        }
        else if (config.isSkos()) {
            return new SKOSLoader(config);
        }
        else {
            return new StructuralOWLOntologyLoader(config);
        }
    }

    public static OntologyLoader getLoader(OntologyResourceConfig config, DatabaseService databaseService) throws OntologyLoadingException {

        if (config.getReasonerType().equals(ReasonerType.OWL2)) {
            return new HermitOWLOntologyLoader(config, databaseService);
        }
        else if (config.getReasonerType().equals(ReasonerType.EL)) {
            return new ELKOWLOntologyLoader(config, databaseService);
        }
        else if (config.isSkos()) {
            return new SKOSLoader(config, databaseService);
        }
        else {
            return new StructuralOWLOntologyLoader(config);
        }
    }
}
