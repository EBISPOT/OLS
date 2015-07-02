package uk.ac.ebi.spot.ols.exception;

/**
 * @author Simon Jupp
 * @date 16/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class IndexingException extends RuntimeException {
    public IndexingException(String s, Exception e) {
        throw new RuntimeException(s, e);
    }
}
