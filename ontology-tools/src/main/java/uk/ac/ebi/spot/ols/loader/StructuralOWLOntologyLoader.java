package uk.ac.ebi.spot.ols.loader;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;

/**
 * @author Simon Jupp
 * @date 14/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class StructuralOWLOntologyLoader  extends AbstractOWLOntologyLoader {

    OWLReasoner reasoner = null;
    public StructuralOWLOntologyLoader(OntologyResourceConfig config)  throws OntologyLoadingException {
        super(config);
    }
    public StructuralOWLOntologyLoader(OntologyResourceConfig config, DatabaseService databaseService)  throws OntologyLoadingException {
        super(config, databaseService);
    }

    protected OWLReasoner getOWLReasoner(OWLOntology ontology) throws OWLOntologyCreationException {


        if (reasoner == null) {
            getLog().debug("Trying to create a reasoner over ontology '" + getOntologyIRI() + "'");
            OWLReasonerFactory factory = new StructuralReasonerFactory();
            reasoner = factory.createReasoner(ontology);
        }

         return reasoner;
     }

    @Override
    protected void discardReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        reasoner = null;
        System.gc();
    }
}

