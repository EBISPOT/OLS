package uk.ac.ebi.spot.ols.util;

/**
 * @author Simon Jupp
 * @date 02/03/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OBOXref {

    String database;
    String id;
    String description;
    String url;

    public OBOXref() {
    }

    public String getDatabase() {

        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
