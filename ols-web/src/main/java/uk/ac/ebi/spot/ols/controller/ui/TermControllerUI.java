package uk.ac.ebi.spot.ols.controller.ui;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.model.Related;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.util.OBODefinitionCitation;
import uk.ac.ebi.spot.ols.util.OBOSynonym;
import uk.ac.ebi.spot.ols.util.OBOXref;

import java.io.IOException;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 15/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/ontologies")
public class TermControllerUI {

    @Autowired
    OntologyRepositoryService repositoryService;

    @Autowired
    private OntologyTermGraphService ontologyTermGraphService;

    @RequestMapping(path = "/{onto}/terms", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    String getTerm(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String termIri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();
        Term term = null;

        if (termIri != null) {
            term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, termIri);
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

        Collection<Individual> individuals = ontologyTermGraphService.getInstances(ontologyId, termIri);
        model.addAttribute("instances", individuals);


        model.addAttribute("relatedFroms", relatedFroms);

        model.addAttribute("ontologyTerm", term);
        model.addAttribute("parentTerms", ontologyTermGraphService.getParents(ontologyId, term.getIri(), new PageRequest(0, 10)));

        String title = repositoryService.get(ontologyId).getConfig().getTitle();
        model.addAttribute("ontologyName", title);

        Collection<OBODefinitionCitation> definitionCitations = new HashSet<>();
        try {
            for (String s : term.getOboDefinitionCitations()) {
                ObjectMapper mapper = new ObjectMapper();
                OBODefinitionCitation obj = mapper.readValue(s, OBODefinitionCitation.class);
                definitionCitations.add(obj);
            }
            model.addAttribute("definitionCitations", definitionCitations);
        } catch (Exception e) {
        }

        Collection<OBOXref> xrefs = new HashSet<>();
        try {
            for (String s : term.getOboXrefs()) {
                ObjectMapper mapper = new ObjectMapper();
                OBOXref obj = mapper.readValue(s, OBOXref.class);
                xrefs.add(obj);
            }
            model.addAttribute("xrefs", xrefs);
        } catch (Exception e) {
        }

        Collection<OBOSynonym> synonyms = new HashSet<>();
        try {
            for (String s : term.getOboSynonyms()) {
                ObjectMapper mapper = new ObjectMapper();
                OBOSynonym obj = mapper.readValue(s, OBOSynonym.class);
                synonyms.add(obj);
            }
            model.addAttribute("synonyms", synonyms);
        } catch (Exception e) {
        }

        return "term";
    }
}
