package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.ui.Model;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;

public class DisplayUtils {

    public static final String PREFERRED_ROOT_TERM_DISPLAY_STYLE_ATTR = "preferredRootTermDisplayStyle";
    public static final String PREFERRED_ROOT_TERM_DISPLAY_ENABLED = "display:initial";
    public static final String PREFERRED_ROOT_TERM_DISPLAY_DISABLED = "display:none";


    public static final String PREFERRED_ROOT_TERM_ENABLED = "preferredRootTermEnabled";

    public static Model setPreferredRootTermsModelAttributes(String ontologyId,
                                                             OntologyDocument ontologyDocument,
                                                             OntologyTermGraphService ontologyTermGraphService,
                                                             Model model) {
        long preferredRootTermCount = ontologyTermGraphService.getPreferredRootTermCount(ontologyId, false);
        if (preferredRootTermCount <= 0) {
            preferredRootTermCount = ontologyDocument.getConfig().getPreferredRootTerms().size();
        }

        if (preferredRootTermCount > 0) {
            model.addAttribute(PREFERRED_ROOT_TERM_DISPLAY_STYLE_ATTR, PREFERRED_ROOT_TERM_DISPLAY_ENABLED);
            model.addAttribute(PREFERRED_ROOT_TERM_ENABLED, true);
        } else {
            model.addAttribute(PREFERRED_ROOT_TERM_DISPLAY_STYLE_ATTR, PREFERRED_ROOT_TERM_DISPLAY_DISABLED);
            model.addAttribute(PREFERRED_ROOT_TERM_ENABLED, false);
        }
        return model;
    }
}
