package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyPropertyGraphService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 15/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/ontologies")
public class PropertyControllerUI {

    @Autowired
    private HomeController homeController;

    @Autowired
    OntologyRepositoryService repositoryService;

    @Autowired
    private OntologyPropertyGraphService ontologyPropertyGraphService;

    @RequestMapping(path = "/{onto}/properties", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    String getProperty(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String termIri,
            Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();
        if (termIri != null) {
            Property term = ontologyPropertyGraphService.findByOntologyAndIri(ontologyId, termIri);
            if (term == null) {
                throw new ResourceNotFoundException();
            }

            model.addAttribute("ontologyProperty", term);
            model.addAttribute("parentProperties", ontologyPropertyGraphService.getParents(ontologyId, termIri, new PageRequest(0, 10)));

            String title = repositoryService.get(ontologyId).getConfig().getTitle();
            model.addAttribute("ontologyName", title);
        }
        else {
            return homeController.doSearch(
                    "*",
                    Collections.singleton(ontologyId),
                    null,null, null, false, null, false, false, null, 10,0,model);
        }
        return "property";
    }
}
