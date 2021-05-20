package uk.ac.ebi.spot.ols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.config.OboDefaults;
import uk.ac.ebi.spot.ols.config.OntologyDefaults;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;

import java.util.Collection;

/**
 * @author Simon Jupp
 * @date 19/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class DocumentUpdater {

    private final static Logger log = LoggerFactory.getLogger(DocumentUpdater.class);


    /**
     * This method updates fields in an ontology document with info from a new ontology document
     * @param originalDocument the original document that needs updating
     * @param newDocument the new document that contains the data that needs to be added to the original document
     * @return the original document iwth fields updated
     */
    public static OntologyDocument updateFields(OntologyDocument originalDocument, OntologyResourceConfig newDocument) {


        // if location has changed, update the info
        if (newDocument.getFileLocation() != null &&
                !originalDocument.getConfig().getFileLocation().equals(newDocument.getFileLocation())) {
            log.info("Location of " + newDocument.getNamespace() + " changed from " +
                    originalDocument.getConfig().getFileLocation() + " to " + newDocument.getFileLocation());
            originalDocument.getConfig().setFileLocation(newDocument.getFileLocation());
            originalDocument.setStatus(Status.TOLOAD);
        }

        // These fields have changed based on information in the ontology, which takes priority over anything in the config so
        // ignore any changes to these fields
        Collection<String> dontUpdate = originalDocument.getConfig().getInternalMetadataProperties();

        // check if title changed
        if (!newDocument.getTitle().equals(originalDocument.getConfig().getNamespace())
                && !newDocument.getTitle().equals(originalDocument.getConfig().getTitle())
                && !dontUpdate.contains(OntologyDefaults.TITLE)) {
            originalDocument.getConfig().setTitle(newDocument.getTitle());
        }

        // check description
        if (newDocument.getDescription() != null &&
                !newDocument.getDescription().equals(originalDocument.getConfig().getDescription()) &&
                !dontUpdate.contains(OntologyDefaults.DEFINITION)) {
            originalDocument.getConfig().setDescription(newDocument.getDescription());
        }

        // check homepage
        if (newDocument.getHomepage() != null &&
                !newDocument.getHomepage().equals(originalDocument.getConfig().getHomepage()) &&
                !dontUpdate.contains(OntologyDefaults.HOMEPAGE)) {
            originalDocument.getConfig().setHomepage(newDocument.getHomepage());
        }

        // check mailing list
        if (newDocument.getMailingList() != null &&
                !newDocument.getMailingList().equals(originalDocument.getConfig().getMailingList()) &&
                !dontUpdate.contains(OntologyDefaults.MAILINGLIST)) {
            originalDocument.getConfig().setMailingList(newDocument.getMailingList());
        }

        // check tracker
        if (newDocument.getTracker() != null &&
                !newDocument.getTracker().equals(originalDocument.getConfig().getTracker()) &&
                !dontUpdate.contains(OntologyDefaults.TRACKER)) {
            originalDocument.getConfig().setTracker(newDocument.getTracker());
        }

        // check logo
        if (newDocument.getLogo() != null &&
                !newDocument.getLogo().equals(originalDocument.getConfig().getLogo()) &&
                !dontUpdate.contains(OntologyDefaults.LOGO)) {
            originalDocument.getConfig().setLogo(newDocument.getLogo());
        }


        // always update these fields

        if (newDocument.getPreferredPrefix() != null) {
            originalDocument.getConfig().setPreferredPrefix(newDocument.getPreferredPrefix());
        }

        if (!newDocument.getReasonerType().equals(originalDocument.getConfig().getReasonerType())) {
            originalDocument.getConfig().setReasonerType(newDocument.getReasonerType());
        }
        originalDocument.getConfig().setOboSlims(newDocument.isOboSlims());

        originalDocument.getConfig().setLabelProperty(newDocument.getLabelProperty());
        originalDocument.getConfig().setDefinitionProperties(newDocument.getDefinitionProperties());
        originalDocument.getConfig().setSynonymProperties(newDocument.getSynonymProperties());
        originalDocument.getConfig().setHierarchicalProperties(newDocument.getHierarchicalProperties());
        originalDocument.getConfig().setPreferredRootTerms(newDocument.getPreferredRootTerms());

        if (!newDocument.getBaseUris().isEmpty()) {
            originalDocument.getConfig().setBaseUris(newDocument.getBaseUris());
        }

        originalDocument.getConfig().setAllowDownload(newDocument.getAllowDownload());


        // Henriette To do: Remove because this is not used.
        if (newDocument.getHiddenProperties() != null) {
            originalDocument.getConfig().setHiddenProperties(newDocument.getHiddenProperties());
        }

        return originalDocument;
    }
}
