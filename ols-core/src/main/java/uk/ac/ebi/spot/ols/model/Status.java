package uk.ac.ebi.spot.ols.model;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * Represents the status of an ontology document in the OLS system
 *
 */
public enum Status {

    NOTLOADED,
    TOLOAD,
    LOADED,
    LOADING,
    DOWNLOADING,
    FAILED,
    TOREMOVE,
    REMOVED,
    SKIP // after too many failed attempts

}
