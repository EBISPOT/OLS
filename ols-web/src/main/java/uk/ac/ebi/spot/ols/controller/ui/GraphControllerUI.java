package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Thomas Liener
 * @date 15/11/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */


@Controller
@RequestMapping("/ontologies")
public class GraphControllerUI {

    @Autowired
    OntologyRepositoryService repositoryService;

    @Autowired
    private OntologyTermGraphService ontologyTermGraphService;

        /* To DO for a trueful restful api
        @RequestMapping(path = "{onto}/terms/{id}/graph", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
        String getTermAsGraph(
        @PathVariable("onto") String ontologyId,
        @PathVariable("id") String iri,
        Model model) {

          ontologyId = ontologyId.toLowerCase();
          Term term = null;

                  if (termIri != null) {
                      term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, termIri);
                  }

                  if (term == null) {
                        throw new ResourceNotFoundException("Can't find any terms with that id");
                  }

                  Map<String, Collection<Map<String, String>>> relatedFroms = ontologyTermGraphService.getRelatedFrom(ontologyId, term.getIri());

                  model.addAttribute("relatedFroms", relatedFroms);

                  model.addAttribute("ontologyTerm", term);
                  model.addAttribute("parentTerms", ontologyTermGraphService.getParents(ontologyId, term.getIri(), new PageRequest(0, 10)));

                  String title = repositoryService.get(ontologyId).getConfig().getTitle();
                  model.addAttribute("ontologyName", title);

                  return "graph";
        }*/


    @RequestMapping(path = "{onto}/terms/graph", method = RequestMethod.GET)
    String getTerm(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String termIri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
                        Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();
        Term term = null;

        if (termIri != null) {

          if (termIri.equals("root"))
              {
                // throw new ResourceNotFoundException("in the second if");
                term=new Term();

                term.setShortForm("TestShortForm");
                term.setOntologyName("Test");
                term.setLabel("TestLabel");
                term.setIri("Testroot");
                //term.setOntologyPrefix("Testprefix"); //doesn't exist, set method has to be written

                model.addAttribute("ontologyTerm", term);
                model.addAttribute("ontologyName", "TestOntologyName");
                return "graph";
              }

          else{
            term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, termIri);
            }



        }
        else if (shortForm != null) {
            term = ontologyTermGraphService.findByOntologyAndShortForm(ontologyId, shortForm);
        }
        else if (oboId != null) {
            term = ontologyTermGraphService.findByOntologyAndOboId(ontologyId, oboId);
        }

        if (term == null) {
             throw new ResourceNotFoundException("Can't find any terms with that id");
        }

        Map<String, Collection<Map<String, String>>> relatedFroms = ontologyTermGraphService.getRelatedFrom(ontologyId, term.getIri());

        model.addAttribute("relatedFroms", relatedFroms);

        model.addAttribute("ontologyTerm", term);
        model.addAttribute("parentTerms", ontologyTermGraphService.getParents(ontologyId, term.getIri(), new PageRequest(0, 10)));

        String title = repositoryService.get(ontologyId).getConfig().getTitle();
        model.addAttribute("ontologyName", title);

        return "graph";
    }
}
