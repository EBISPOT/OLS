package uk.ac.ebi.spot.ols.loader;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;

/**
 * @author Simon Jupp
 * @date 03/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class SKOSLoader extends AbstractOWLOntologyLoader {
    public SKOSLoader(OntologyResourceConfig config, DatabaseService databaseService) throws OntologyLoadingException {
        super(config, databaseService);
    }
    public SKOSLoader(OntologyResourceConfig config) throws OntologyLoadingException {
        super(config);
    }
    @Override
    protected void discardReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        System.gc();
    }
    @Override
    protected OWLReasoner getOWLReasoner(OWLOntology owlOntology) {
        return null;
    }
}
