package uk.ac.ebi.spot.ols.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.IndividualJsTreeBuilder;
import uk.ac.ebi.spot.ols.neo4j.service.JsTreeBuilder;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyIndividualService;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import uk.ac.ebi.spot.ols.controller.api.localization.LocalizedIndividual;
import uk.ac.ebi.spot.ols.controller.api.localization.LocalizedTerm;

/**
 * @author Simon Jupp
 * @date 02/11/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/api/ontologies")
public class OntologyIndividualController {

    @Autowired
    private OntologyIndividualService ontologyIndividualRepository;

    @Autowired
    IndividualAssembler individualAssembler;

    @Autowired
    TermAssembler termAssembler;

    @Autowired
    IndividualJsTreeBuilder jsTreeBuilder;

    @RequestMapping(path = "/{onto}/individuals", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedIndividual>> getAllIndividualsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<LocalizedIndividual> terms = null;

        ontologyId = ontologyId.toLowerCase();
        if (iri != null) {
            Individual term = ontologyIndividualRepository.findByOntologyAndIri(ontologyId, iri);
            if (term != null) {
                terms = new PageImpl<LocalizedIndividual>(Arrays.asList(LocalizedIndividual.fromIndividual(lang, term)));
            }
        } else if (shortForm != null) {
            Individual term = ontologyIndividualRepository.findByOntologyAndShortForm(ontologyId, shortForm);
            if (term != null) {
                terms = new PageImpl<LocalizedIndividual>(Arrays.asList(LocalizedIndividual.fromIndividual(lang, term)));
            }
        } else if (oboId != null) {
            Individual term = ontologyIndividualRepository.findByOntologyAndOboId(ontologyId, oboId);
            if (term != null) {
                terms = new PageImpl<LocalizedIndividual>(Arrays.asList(LocalizedIndividual.fromIndividual(lang, term)));
            }
        } else {
	    Page<Individual> res = null;
            res = ontologyIndividualRepository.findAllByOntology(ontologyId, pageable);
            if (res == null) throw new ResourceNotFoundException("Ontology not found");
	    terms = res.map(term -> LocalizedIndividual.fromIndividual(lang, term));
        }

        return new ResponseEntity<>(assembler.toResource(terms, individualAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/individuals/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<LocalizedIndividual>> getIndividual(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
    @PathVariable("id") String termId) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Individual term = ontologyIndividualRepository.findByOntologyAndIri(ontologyId, decoded);
            return new ResponseEntity<>(individualAssembler.toResource(LocalizedIndividual.fromIndividual(lang, term)), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/individuals/{id}/types", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getDirectTypes(@PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
     @PathVariable("id") String termId, Pageable pageable,
                                                    PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyIndividualRepository.getDirectTypes(ontologyId, decoded, pageable);
	    Page<LocalizedTerm> localized = parents.map(term -> LocalizedTerm.fromTerm(lang, term));
            return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }


    @RequestMapping(path = "/{onto}/individuals/{id}/alltypes", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> ancestors(@PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
     @PathVariable("id") String termId, Pageable pageable,
                                                   PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> ancestors = ontologyIndividualRepository.getAllTypes(ontologyId, decoded, pageable);
	    Page<LocalizedTerm> localized = ancestors.map(term -> LocalizedTerm.fromTerm(lang, term));
            return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/individuals/{id}/jstree", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<String> getJsTree(@PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
    @PathVariable("id") String termId) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object = jsTreeBuilder.getJsTree(lang, ontologyId, decoded, false);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }

}
