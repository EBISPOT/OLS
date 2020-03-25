
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

    @Value("${ols.customisation.org:EMBL-EBI}")
    private String org;

    public void setCustomisationModelAttributes(Model model) {
        model.addAttribute("debrand", debrand);
        model.addAttribute("logo", logo);
        model.addAttribute("title", title);
        model.addAttribute("shortTitle", shortTitle);
        model.addAttribute("org", org);
    }
}
