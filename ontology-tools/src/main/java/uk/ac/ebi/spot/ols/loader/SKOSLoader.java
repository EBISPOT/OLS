package uk.ac.ebi.spot.ols.loader;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;

/**
 * @author Simon Jupp
 * @date 03/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class SKOSLoader extends AbstractOWLOntologyLoader {
    public SKOSLoader(OntologyResourceConfig config) throws OntologyLoadingException {
        super(config);
    }

    @Override
    protected OWLReasoner getOWLReasoner(OWLOntology owlOntology) {
        return null;
    }
}
