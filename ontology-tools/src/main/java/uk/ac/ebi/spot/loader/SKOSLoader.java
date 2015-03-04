package uk.ac.ebi.spot.loader;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.ebi.spot.config.OntologyResourceConfig;
import uk.ac.ebi.spot.exception.OntologyLoadingException;
import uk.ac.ebi.spot.loader.AbstractOWLOntologyLoader;

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
