
package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OlsErrorController implements ErrorController  {

    @Autowired
    private CustomisationProperties customisationProperties;

    @RequestMapping("/error")
    public String handleError(Model model) {

        customisationProperties.setCustomisationModelAttributes(model);

        return "error";
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}

