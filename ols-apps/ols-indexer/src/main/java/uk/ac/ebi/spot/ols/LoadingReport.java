package uk.ac.ebi.spot.ols;

import java.util.Collection;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 19/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class LoadingReport {

    private Collection<String> updatedOntologies;
    private Map<String, String> failingOntologies;
    private String expections;


    public LoadingReport(Map<String, String> failingOntologies, Collection<String> updatedOntologies, String exceptions) {

        this.failingOntologies = failingOntologies;
        this.updatedOntologies = updatedOntologies;
        this.expections = exceptions;

    }

    public Map<String, String> getFailingOntologies() {
        return failingOntologies;
    }

    public void setFailingOntologies(Map<String, String> failingOntologies) {
        this.failingOntologies = failingOntologies;
    }

    public Collection<String> getUpdatedOntologies() {
        return updatedOntologies;
    }

    public void setUpdatedOntologies(Collection<String> updatedOntologies) {
        this.updatedOntologies = updatedOntologies;
    }

    public String getExpections() {
        return expections;
    }
}
