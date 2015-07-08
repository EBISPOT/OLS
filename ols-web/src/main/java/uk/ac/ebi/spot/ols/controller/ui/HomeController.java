package uk.ac.ebi.spot.ols.controller.ui;

/**
 * @author Simon Jupp
 * @date 08/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.util.List;

@Controller
@RequestMapping("")
public class HomeController {

    @Autowired
    OntologyRepositoryService repositoryService;

    @ModelAttribute("all_ontologies")
    public List<OntologyDocument> getOntologies() {
        return repositoryService.getAllDocuments();
    }

    @ModelAttribute("all_ontologies")
    public Page<OntologyDocument> getOntologies(Pageable pageable) {
        return repositoryService.getAllDocuments(pageable);
    }


    @RequestMapping({"", "index"})
    public String showHome(Model model) {
        return "index";
    }

    @RequestMapping({"/contact"})
    public String showContact() {
        return "contact";
    }

}
