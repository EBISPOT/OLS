package uk.ac.ebi.spot.loader;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import uk.ac.ebi.spot.config.OntologyResourceConfig;


/**
 * Loads an ontology using the OWLAPI, and considers only axioms that are asserted in the loaded ontology when
 * generating class labels and types
 *
 * @author Tony Burdett
 * @author James Malone
 * @author Simon Jupp
 * @date 15/02/12
 */
public class ELKOWLOntologyLoader extends AbstractOWLOntologyLoader {

    OWLReasoner reasoner = null;
    public ELKOWLOntologyLoader(OntologyResourceConfig config) {
        super(config);
    }

    protected OWLReasoner getOWLReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
        if (reasoner == null) {
            getLog().debug("Trying to create a reasoner over ontology '" + getOntologyIRI() + "'");
            OWLReasonerFactory factory = new ElkReasonerFactory();
            ReasonerProgressMonitor progressMonitor = new LoggingReasonerProgressMonitor(getLog());
            OWLReasonerConfiguration owlReasonerConfiguration = new SimpleConfiguration(progressMonitor);
            reasoner = factory.createReasoner(ontology, owlReasonerConfiguration);

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
