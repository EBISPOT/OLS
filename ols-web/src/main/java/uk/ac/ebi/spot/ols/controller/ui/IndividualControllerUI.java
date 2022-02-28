package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyIndividualService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

/**
 * @author Simon Jupp
 * @date 15/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/ontologies")
public class IndividualControllerUI {

    @Autowired
    private HomeController homeController;

    @Autowired
    OntologyRepositoryService repositoryService;

    @Autowired
    private OntologyIndividualService ontologyIndividualService;

    @Autowired
    private CustomisationProperties customisationProperties;

    @RequestMapping(path = "/{onto}/individuals", method = RequestMethod.GET)
    String getIndividuals(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String individualIri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();

        Individual individual = null;

        model.addAttribute("lang", lang);

        OntologyDocument document = repositoryService.get(ontologyId);
        model.addAttribute("ontologyLanguages", document.getConfig().getLanguages());

        if (individualIri != null) {
            individual = ontologyIndividualService.findByOntologyAndIri(ontologyId, individualIri);
        } else if (shortForm != null) {
            individual = ontologyIndividualService.findByOntologyAndShortForm(ontologyId, shortForm);
        } else if (oboId != null) {
            individual = ontologyIndividualService.findByOntologyAndOboId(ontologyId, oboId);
        }

        if (individualIri == null & shortForm == null & oboId == null) {

            if (pageable.getSort() == null) {
                pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(new Sort.Order(Sort.Direction.ASC, "n.label")));
            }

            Page<Individual> individualsPage = ontologyIndividualService.findAllByOntology(ontologyId, pageable);

            model.addAttribute("ontologyName", document.getOntologyId());
            model.addAttribute("ontologyTitle", document.getConfig().getLocalizedTitle(lang));
            model.addAttribute("ontologyPrefix", document.getConfig().getPreferredPrefix());
            model.addAttribute("pageable", pageable);
            model.addAttribute("allindividuals", individualsPage);
            model.addAttribute("allindividualssize", individualsPage.getTotalElements());
            customisationProperties.setCustomisationModelAttributes(model);
            return "allindividuals";
        }


        if (individual == null) {
            throw new ResourceNotFoundException("Can't find any individual with that id");
        }

        model.addAttribute("ontologyIndividual", individual);
        model.addAttribute("individualTypes", ontologyIndividualService.getDirectTypes(ontologyId, individual.getIri(), new PageRequest(0, 10)));
        model.addAttribute("individualAnonymousTypes", individual.getAnonymousType());

        String title = repositoryService.get(ontologyId).getConfig().getLocalizedTitle(lang);
        model.addAttribute("ontologyName", title);
        customisationProperties.setCustomisationModelAttributes(model);
        return "individual";
    }
}
