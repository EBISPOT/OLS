package uk.ac.ebi.spot.ols;

/**
 * @author Simon Jupp
 * @date 20/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class YamlConfigParsingExpception extends RuntimeException {
    public YamlConfigParsingExpception(String message) {
        super(message);
    }
}
