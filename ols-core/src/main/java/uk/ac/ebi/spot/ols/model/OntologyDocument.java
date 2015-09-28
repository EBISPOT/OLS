package uk.ac.ebi.spot.ols.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;

import java.util.Date;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * An OntologyDocument contains meta data for an individual ontology in OLS.
 * This includes basic stats on an ontology that is loaded into the Ontology repository.
 *
 */
@Document(collection = "olsadmin")
public class OntologyDocument {

    @Id
    private String ontologyId;

    private Date loaded;

    private Date updated;

    private Status status;

    private String message;

    @JsonIgnore
    private String localPath;

    private String version;


    private int numberOfTerms;
    private int numberOfProperties;
    private int numberOfIndividuals;

    private OntologyResourceConfig config;

    public OntologyDocument(String ontologyId, Date updated, Status status, String message, String localPath, int numberOfTerms, int numberOfProperties, int numberOfIndividuals, OntologyResourceConfig config) {
        this.ontologyId = ontologyId;
        this.updated = updated;
        this.status = status;
        this.message = message;
        this.localPath = localPath;
        this.numberOfTerms = numberOfTerms;
        this.numberOfProperties = numberOfProperties;
        this.numberOfIndividuals = numberOfIndividuals;
        this.loaded = null;
        this.version = null;
        this.config = config;
    }

    public OntologyDocument() {
    }

    public OntologyDocument(String ontologyId, OntologyResourceConfig config) {
        this(ontologyId, new Date(), Status.NOTLOADED, "No ontology loaded", null, 0,0,0, config);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLoaded() {
        return loaded;
    }

    public void setLoaded(Date loaded) {
        this.loaded = loaded;
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

    public int getNumberOfProperties() {
        return numberOfProperties;
    }

    public void setNumberOfProperties(int numberOfProperties) {
        this.numberOfProperties = numberOfProperties;
    }

    public int getNumberOfIndividuals() {
        return numberOfIndividuals;
    }

    public void setNumberOfIndividuals(int numberOfIndividuals) {
        this.numberOfIndividuals = numberOfIndividuals;
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
