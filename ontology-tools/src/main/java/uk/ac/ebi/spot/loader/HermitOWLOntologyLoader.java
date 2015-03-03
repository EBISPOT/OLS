package uk.ac.ebi.spot.loader;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import uk.ac.ebi.spot.config.OntologyResourceConfig;

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
    public HermitOWLOntologyLoader(OntologyResourceConfig config) {
        super(config);
    }

    @Override
    protected OWLReasoner getOWLReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        if (reasoner == null) {
            getLog().debug("Trying to create a reasoner over ontology '" + getOntologyIRI() + "'");
            OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
            ReasonerProgressMonitor progressMonitor = new LoggingReasonerProgressMonitor(getLog());
            OWLReasonerConfiguration reasonerConfiguration = new SimpleConfiguration(progressMonitor);
            reasoner = factory.createReasoner(ontology, reasonerConfiguration);

            getLog().debug("Precomputing inferences...");
            reasoner.precomputeInferences();

            getLog().debug("Checking ontology consistency...");
            reasoner.isConsistent();

            getLog().debug("Checking for unsatisfiable classes...");
            if (reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size() > 0) {
                throw new OWLOntologyCreationException(
                        "Once classified, unsatisfiable classes were detected in '" + getOntologyIRI() + "'");
            }
            else {
                getLog().debug("Reasoning complete! ");
            }
        }

        return reasoner;
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
