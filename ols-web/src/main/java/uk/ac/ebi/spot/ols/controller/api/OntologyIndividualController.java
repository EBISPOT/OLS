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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.OlsIndividual;
import uk.ac.ebi.spot.ols.neo4j.model.OlsProperty;
import uk.ac.ebi.spot.ols.neo4j.model.OlsTerm;
import uk.ac.ebi.spot.ols.neo4j.service.IndividualJsTreeBuilder;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyIndividualService;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
    HttpEntity<PagedModel<OlsIndividual>> getAllIndividualsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<OlsIndividual> terms = null;

        ontologyId = ontologyId.toLowerCase();
        if (iri != null) {
            OlsIndividual term = ontologyIndividualRepository.findByOntologyAndIri(ontologyId, iri);
            if (term != null) {
                terms = new PageImpl<OlsIndividual>(Arrays.asList(term));
            }
        } else if (shortForm != null) {
            OlsIndividual term = ontologyIndividualRepository.findByOntologyAndShortForm(ontologyId, shortForm);
            if (term != null) {
                terms = new PageImpl<OlsIndividual>(Arrays.asList(term));
            }
        } else if (oboId != null) {
            OlsIndividual term = ontologyIndividualRepository.findByOntologyAndOboId(ontologyId, oboId);
            if (term != null) {
                terms = new PageImpl<OlsIndividual>(Arrays.asList(term));
            }
        } else {
            terms = ontologyIndividualRepository.findAllByOntology(ontologyId, pageable);
        }

        return new ResponseEntity<>(assembler.toModel(terms, individualAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/individuals/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<EntityModel<OlsIndividual>> getIndividual(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        String decoded = UriUtils.decode(termId, "UTF-8");
        OlsIndividual term = ontologyIndividualRepository.findByOntologyAndIri(ontologyId, decoded);
        return new ResponseEntity<>(individualAssembler.toModel(term), HttpStatus.OK);

    }

    @RequestMapping(path = "/{onto}/individuals/{id}/types", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedModel<OlsTerm>> getDirectTypes(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                                       PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<OlsTerm> parents = ontologyIndividualRepository.getDirectTypes(ontologyId, decoded, pageable);
            return new ResponseEntity<>(assembler.toModel(parents, termAssembler), HttpStatus.OK);

    }


    @RequestMapping(path = "/{onto}/individuals/{id}/alltypes", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedModel<OlsProperty>> ancestors(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                                   PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        String decoded = UriUtils.decode(termId, "UTF-8");
        Page<OlsTerm> ancestors = ontologyIndividualRepository.getAllTypes(ontologyId, decoded, pageable);
        return new ResponseEntity<>(assembler.toModel(ancestors, termAssembler), HttpStatus.OK);

    }

    @RequestMapping(path = "/{onto}/individuals/{id}/jstree", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<String> getJsTree(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object = jsTreeBuilder.getJsTree(ontologyId, decoded, false);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }

}
