package uk.ac.ebi.spot.ols.controller.api;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriUtils;

import uk.ac.ebi.spot.ols.neo4j.model.OlsIndividual;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyIndividualService;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/api/individuals")
@ExposesResourceFor(OlsIndividual.class)
public class IndividualController implements
        RepresentationModelProcessor<RepositoryLinksResource> {
    @Autowired
    private OntologyIndividualService ontologyIndividualRepository;

    @Autowired
    IndividualAssembler individualAssembler;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(IndividualController.class).withRel("individuals"));
        return resource;
    }

    @RequestMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedModel<OlsIndividual>> getAllIndividuals(
            @PathVariable("id") String termId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {
        String decoded = UriUtils.decode(termId, "UTF-8");
        return getAllIndividuals(decoded, null, null, pageable, assembler);

    }

    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedModel<OlsIndividual>> getAllIndividuals(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<OlsIndividual> terms = null;

        if (iri != null) {
            terms = ontologyIndividualRepository.findAllByIri(iri, pageable);
        } else if (shortForm != null) {
            terms = ontologyIndividualRepository.findAllByShortForm(shortForm, pageable);
        } else if (oboId != null) {
            terms = ontologyIndividualRepository.findAllByOboId(oboId, pageable);
        } else {
            terms = ontologyIndividualRepository.findAll(pageable);
        }

        return new ResponseEntity<>(assembler.toModel(terms, individualAssembler), HttpStatus.OK);
    }
    
    @RequestMapping(path = "/findByIdAndIsDefiningOntology/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedModel<OlsIndividual>> getAllIndividualsByIdAndIsDefiningOntology(
            @PathVariable("id") String termId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {
        String decoded = UriUtils.decode(termId, "UTF-8");
        return getAllIndividualsByIdAndIsDefiningOntology(decoded, null, null, pageable, assembler);

    }    
    
    
    @RequestMapping(path = "/findByIdAndIsDefiningOntology", 
    		produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, 
    		method = RequestMethod.GET)
    HttpEntity<PagedModel<OlsIndividual>> getAllIndividualsByIdAndIsDefiningOntology(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<OlsIndividual> terms = null;

        if (iri != null) {
            terms = ontologyIndividualRepository.findAllByIriAndIsDefiningOntology(iri, pageable);
        } else if (shortForm != null) {
            terms = ontologyIndividualRepository.findAllByShortFormAndIsDefiningOntology(shortForm, pageable);
        } else if (oboId != null) {
            terms = ontologyIndividualRepository.findAllByOboIdAndIsDefiningOntology(oboId, pageable);
        } else {
            terms = ontologyIndividualRepository.findAllByIsDefiningOntology(pageable);
        }

        return new ResponseEntity<>(assembler.toModel(terms, individualAssembler), HttpStatus.OK);
    }
    

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }
}