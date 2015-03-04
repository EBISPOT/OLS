package uk.ac.ebi.spot.ols.exception;

/**
 * @author Simon Jupp
 * @date 16/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class FileUpdateServiceException extends Exception {
    public FileUpdateServiceException(String s, Exception e) {
        super(s, e);
    }
    public FileUpdateServiceException(String s) {
        super(s);
    }
}
