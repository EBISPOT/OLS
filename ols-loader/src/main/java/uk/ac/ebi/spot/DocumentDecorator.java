package uk.ac.ebi.spot;

import uk.ac.ebi.spot.ols.model.OntologyDocument;

/**
 * @author Simon Jupp
 * @date 17/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class DocumentDecorator {

    private boolean hasError;
    private String message;

    private final OntologyDocument document;

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public DocumentDecorator(OntologyDocument document) {
        this.document = document;
        this.hasError = false;
        this.message = "";
    }


}
