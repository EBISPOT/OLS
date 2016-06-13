package uk.ac.ebi.spot.ols.xrefs;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Simon Jupp
 * @date 11/06/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * A service interface for accessing database objects
 *
 */
public interface DatabaseService {

    Collection<Database> find();
    Optional<Database> findByName(String name);

}
