package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.ols.neo4j.model.Related;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 15/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/ontology")
public class TermControllerUI {

    @Autowired
    private HomeController homeController;

    @Autowired
    OntologyRepositoryService repositoryService;

    @Autowired
    private OntologyTermGraphService ontologyTermGraphService;

    @RequestMapping(path = "/{onto}/terms", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    String getTerm(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String termIri,
            Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();
        if (termIri != null) {
            Term term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, termIri);
            if (term == null) {
                throw new ResourceNotFoundException();
            }

            Map<String, Collection<Map<String, String>>> relatedFroms = ontologyTermGraphService.getRelatedFrom(ontologyId, termIri);

            model.addAttribute("relatedFroms", relatedFroms);

            model.addAttribute("ontologyTerm", term);
            model.addAttribute("parentTerms", ontologyTermGraphService.getParents(ontologyId, termIri, new PageRequest(0, 10)));

            String title = repositoryService.get(ontologyId).getConfig().getTitle();
            model.addAttribute("ontologyName", title);
        }
        else {
            return homeController.doSearch(
                    "*",
                    Collections.singleton(ontologyId),
                    null,null, null, false, null, false, false, null, 10,0,model);
        }
        return "term";
    }
}
