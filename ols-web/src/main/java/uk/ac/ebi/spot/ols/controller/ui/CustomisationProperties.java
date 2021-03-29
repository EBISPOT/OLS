
package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.Model;

@ConfigurationProperties(prefix = "ols.customisation")
@Configuration("customisationProperties")
public class CustomisationProperties {

    @Value("${ols.customisation.debrand:false}")
    private boolean debrand;
    
    @Value("${ols.customisation.logo:/img/OLS_logo_2017.png}")
    private String logo;

    @Value("${ols.customisation.title:Ontology Lookup Service}")
    private String title;

    @Value("${ols.customisation.short-title:OLS}")
    private String shortTitle;

    @Value("${ols.customisation.description:}")
    private String description;

    @Value("${ols.customisation.org:EMBL-EBI}")
    private String org;

    @Value("${ols.customisation.hideGraphView:false}")
    private boolean hideGraphView;

    @Value("${ols.customisation.errorMessage:Something went wrong! Please contact ols-support@ebi.ac.uk to report any bugs or give feedback.}")
    private String errorMessage;

    @Value("${ols.customisation.ontologyAlias:Ontology}")
    private String ontologyAlias;

    @Value("${ols.customisation.ontologyAliasPlural:Ontologies}")
    private String ontologyAliasPlural;

    @Value("${ols.customisation.oxoUrl:https://www.ebi.ac.uk/spot/oxo/}")
    private String oxoUrl;

    public void setCustomisationModelAttributes(Model model) {
        model.addAttribute("debrand", debrand);
        model.addAttribute("logo", logo);
        model.addAttribute("title", title);
        model.addAttribute("shortTitle", shortTitle);
        model.addAttribute("description", description);
        model.addAttribute("org", org);
        model.addAttribute("hideGraphView", hideGraphView);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("ontologyAlias", ontologyAlias);
        model.addAttribute("ontologyAliasPlural", ontologyAliasPlural);
        model.addAttribute("oxoUrl", oxoUrl);
    }

    public boolean getDebrand() {
        return debrand;
    }
}
