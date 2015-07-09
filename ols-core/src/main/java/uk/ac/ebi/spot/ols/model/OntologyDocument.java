package uk.ac.ebi.spot.ols.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;

import java.util.Date;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * This is an interface to Ontology Documents in the OLS system. An OntologyDocument contains meta
 * data for an individual ontology in OLS. This in includes basic stats on an ontology that is loaded into the
 * Ontology repository.
 *
 */
@Document(collection = "olsadmin")
public class OntologyDocument {

    @Id
    private String ontologyId;

    private Date updated;

    private Status status;

    private String message;

    private String localPath;

    private int numberOfTerms;

    private OntologyResourceConfig config;

    public OntologyDocument(String ontologyId, Date updated, Status status, String message, String localPath, int numberOfTerms, OntologyResourceConfig config) {
        this.ontologyId = ontologyId;
        this.updated = updated;
        this.status = status;
        this.message = message;
        this.localPath = localPath;
        this.numberOfTerms = numberOfTerms;
        this.config = config;
    }

    public OntologyDocument() {
    }

    public OntologyDocument(String ontologyId, OntologyResourceConfig config) {
        this.ontologyId = ontologyId;
        this.config = config;
        this.status = Status.NOTLOADED;
        this.message = "No ontology loaded";
        this.localPath = null;
        this.updated = new Date();
        this.numberOfTerms = 0;
    }

    public String getOntologyId() {
        return ontologyId;
    }

    public void setOntologyId(String ontologyId) {
        this.ontologyId = ontologyId;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumberOfTerms() {
        return numberOfTerms;
    }

    public void setNumberOfTerms(int numberOfTerms) {
        this.numberOfTerms = numberOfTerms;
    }

    public OntologyResourceConfig getConfig() {
        return config;
    }

    public void setConfig(OntologyResourceConfig config) {
        this.config = config;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
