package uk.ac.ebi.spot.ols.config;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 09/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OboDefaults {
    public static String EXACT_SYNONYM = "http://www.geneontology.org/formats/oboInOwl#hasExactSynonym";
    public static String RELATED_SYNONYM = "http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym";
    public static String BROAD_SYNONYM = "http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym";
    public static String NARROW_SYNONYM = "http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym";
    public static String DBXREF = "http://www.geneontology.org/formats/oboInOwl#hasDbXref";
    public static String DEFINITION = "http://purl.obolibrary.org/obo/IAO_0000115";
    public static String SYNONYM_TYPE = "http://www.geneontology.org/formats/oboInOwl#hasSynonymType";


    public static Set<URI> hierarchical_relations = new HashSet<>(
            Arrays.asList(
                    URI.create("http://purl.obolibrary.org/obo/BFO_0000050")
            ));
}
