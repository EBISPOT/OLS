package uk.ac.ebi.spot.ols.xrefs;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Simon Jupp
 * @date 11/06/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * This class representation a database, databases have information such as a name, description and URL.
 * Databases also have URL patterns that are used by OLS to link out to external databases, usually from a DB Xref annotation
 */
public interface Database {

    /**
     * Get the primary id of the database
     * @return the database id
     */
    String getDatabaseId();

    /**
     * Get the primary name of the database
     * @return the database name
     */
    String getDatabaseName();


    /**
     * Get the url for a given id
     * @return the database id
     */
    URL getUrlForId(String id) throws MalformedURLException;



}
