package uk.ac.ebi.spot.ols.loader;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.reasoner.PseudoReasoner;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;

public class NoReasonerOWLOntologyLoader extends AbstractOWLOntologyLoader {
    private OWLReasoner reasoner;
    public NoReasonerOWLOntologyLoader(OntologyResourceConfig config) throws OntologyLoadingException {
        super(config);
    }

    public NoReasonerOWLOntologyLoader(OntologyResourceConfig config, DatabaseService databaseService,
                                       OntologyLoadingConfiguration ontologyLoadingConfiguration)
            throws OntologyLoadingException {
        super(config, databaseService, ontologyLoadingConfiguration);
    }

    @Override
    protected OWLReasoner getOWLReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        return new PseudoReasoner(ontology);
    }

    @Override
    protected void discardReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        reasoner = null;
    }
}
