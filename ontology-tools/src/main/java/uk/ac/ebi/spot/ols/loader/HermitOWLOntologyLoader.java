package uk.ac.ebi.spot.ols.loader;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;

import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;

/**
 * Loads an ontology using the OWLAPI and a HermiT reasoner to classify the ontology.  This allows for richer typing
 * information on each class to be provided
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 03/06/13
 */
public class HermitOWLOntologyLoader extends AbstractOWLOntologyLoader {
    OWLReasoner reasoner = null;
    public HermitOWLOntologyLoader(OntologyResourceConfig config, DatabaseService databaseService,
    		OntologyLoadingConfiguration ontologyLoadingConfiguration) 
    		throws OntologyLoadingException {
        super(config, databaseService, ontologyLoadingConfiguration);
    }
    public HermitOWLOntologyLoader(OntologyResourceConfig config) throws OntologyLoadingException {
        super(config);
    }
    @Override
    protected OWLReasoner getOWLReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        if (reasoner == null) {
            getLogger().debug("Trying to create a reasoner over ontology '" + getOntologyIRI() + "'");
            OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
            ReasonerProgressMonitor progressMonitor = new LoggingReasonerProgressMonitor(getLogger());
            OWLReasonerConfiguration reasonerConfiguration = new SimpleConfiguration(progressMonitor);
            reasoner = factory.createReasoner(ontology, reasonerConfiguration);

            getLogger().debug("Precomputing inferences...");
            reasoner.precomputeInferences();

            getLogger().debug("Checking ontology consistency...");
            if (!reasoner.isConsistent()) {
                getLogger().warn("Inconsistent ontology " + getOntologyIRI() + ", reverting to pseudo reasoner");
                reasoner.dispose();
                OWLReasonerFactory structuralReasonerFactory = new StructuralReasonerFactory();
                reasoner = structuralReasonerFactory.createReasoner(ontology);
                return reasoner;
            }

            getLogger().debug("Checking for unsatisfiable classes...");
            if (reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size() > 0) {
                getLogger().warn(
                        "Once classified, unsatisfiable classes were detected in '" + getOntologyIRI() + "'");
                reasoner.dispose();
                OWLReasonerFactory structuralReasonerFactory = new StructuralReasonerFactory();
                reasoner = structuralReasonerFactory.createReasoner(ontology);;
                return reasoner;
            }
            else {
                getLogger().debug("Reasoning complete! ");
            }
        }

        return reasoner;
    }
    @Override
    protected void discardReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        reasoner = null;
        System.gc();
    }
    protected class LoggingReasonerProgressMonitor implements ReasonerProgressMonitor {
        private final Logger log;
        private int lastPercent = 0;

        public LoggingReasonerProgressMonitor(Logger log) {
            this.log = log;
        }

        protected Logger getLog() {
            return log;
        }

        @Override public void reasonerTaskStarted(String s) {
            getLog().debug(s);
        }

        @Override public void reasonerTaskStopped() {
            getLog().debug("100% done!");
            lastPercent = 0;
        }

        @Override public void reasonerTaskProgressChanged(int value, int max) {
            if (max > 0) {
                int percent = value * 100 / max;
                if (lastPercent != percent) {
                    if (percent % 25 == 0) {
                        getLog().debug("" + percent + "% done...");
                    }
                    lastPercent = percent;
                }
            }
        }

        @Override public void reasonerTaskBusy() {

        }
    }
}
