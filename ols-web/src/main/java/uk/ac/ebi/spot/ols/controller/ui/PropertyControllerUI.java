package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
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

    @RequestMapping(path = "/{onto}/properties", method = RequestMethod.GET)
    String getProperty(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String termIri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();
        Property property = null;

        if (termIri != null) {
            property = ontologyPropertyGraphService.findByOntologyAndIri(ontologyId, termIri);
        }
        else if (shortForm != null) {
            property = ontologyPropertyGraphService.findByOntologyAndShortForm(ontologyId, shortForm);
        }
        else if (oboId != null) {
            property = ontologyPropertyGraphService.findByOntologyAndOboId(ontologyId, oboId);
        }

        if (termIri == null & shortForm == null & oboId == null) {

            if (pageable.getSort() == null) {
                pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(new Sort.Order(Sort.Direction.ASC, "n.label")));
            }

            Page<Property> termsPage = ontologyPropertyGraphService.findAllByOntology(ontologyId, pageable);

            OntologyDocument document = repositoryService.get(ontologyId);
            model.addAttribute("ontologyName", document.getOntologyId());
            model.addAttribute("ontologyTitle", document.getConfig().getTitle());
            model.addAttribute("ontologyPrefix", document.getConfig().getPreferredPrefix());
            model.addAttribute("pageable", pageable);
            model.addAttribute("allproperties", termsPage);
            model.addAttribute("allpropertiessize", termsPage.getTotalElements());
            return "allproperties";
        }

        if (property == null) {
            throw new ResourceNotFoundException("Can't find any property with that id");
        }

        model.addAttribute("ontologyProperty", property);
        model.addAttribute("parentProperties", ontologyPropertyGraphService.getParents(ontologyId, property.getIri(), new PageRequest(0, 10)));

        String title = repositoryService.get(ontologyId).getConfig().getTitle();
        model.addAttribute("ontologyName", title);

        return "property";
    }
}
