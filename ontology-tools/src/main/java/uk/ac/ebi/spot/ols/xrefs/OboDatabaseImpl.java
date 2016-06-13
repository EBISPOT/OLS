package uk.ac.ebi.spot.ols.xrefs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.xrefs.Database;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Simon Jupp
 * @date 13/06/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OboDatabaseImpl implements Database {

    private String databaseId;
    private String databaseName;
    private String urlSyntax;

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUrlSyntax() {
        return urlSyntax;
    }

    public void setUrlSyntax(String urlSyntax) {
        this.urlSyntax = urlSyntax;
    }

    @Override
    public String getDatabaseId() {
        return databaseId;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public URL getUrlForId(String id) throws MalformedURLException {
            return  new URL(urlSyntax.replace("[example_id]", id));
    }
}
