package uk.ac.ebi.spot.ols.controller.api;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
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

import io.swagger.annotations.ApiOperation;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.service.JsTreeBuilder;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyIndividualService;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/api/individuals")
@ExposesResourceFor(Individual.class)
public class IndividualController implements
        ResourceProcessor<RepositoryLinksResource> {
    @Autowired
    private OntologyIndividualService ontologyIndividualRepository;

    @Autowired
    IndividualAssembler individualAssembler;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(IndividualController.class).withRel("individuals"));
        return resource;
    }
    @ApiOperation(value = "Retrieve a particular individual.")
    @RequestMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Individual>> getAllIndividuals(
            @PathVariable("id") String termId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {
        String decoded = null;
        try {
            decoded = UriUtils.decode(termId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException("Can't decode IRI: " + termId);
        }
        return getAllIndividuals(decoded, null, null, pageable, assembler);

    }
    @ApiOperation(value = "List all individuals")
    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Individual>> getAllIndividuals(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Individual> terms = null;

        if (iri != null) {
            terms = ontologyIndividualRepository.findAllByIri(iri, pageable);
        } else if (shortForm != null) {
            terms = ontologyIndividualRepository.findAllByShortForm(shortForm, pageable);
        } else if (oboId != null) {
            terms = ontologyIndividualRepository.findAllByOboId(oboId, pageable);
        } else {
            terms = ontologyIndividualRepository.findAll(pageable);
        }

        return new ResponseEntity<>(assembler.toResource(terms, individualAssembler), HttpStatus.OK);
    }
    @ApiOperation(value = "Find individual based on defining ontology")
    @RequestMapping(path = "/findByIdAndIsDefiningOntology/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Individual>> getAllIndividualsByIdAndIsDefiningOntology(
            @PathVariable("id") String termId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {
        String decoded = null;
        try {
            decoded = UriUtils.decode(termId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException("Can't decode IRI: " + termId);
        }
        return getAllIndividualsByIdAndIsDefiningOntology(decoded, null, null, pageable, assembler);

    }    
    
    @ApiOperation(value = "List individuals based on defining ontology")
    @RequestMapping(path = "/findByIdAndIsDefiningOntology", 
    		produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, 
    		method = RequestMethod.GET)
    HttpEntity<PagedResources<Individual>> getAllIndividualsByIdAndIsDefiningOntology(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Individual> terms = null;

        if (iri != null) {
            terms = ontologyIndividualRepository.findAllByIriAndIsDefiningOntology(iri, pageable);
        } else if (shortForm != null) {
            terms = ontologyIndividualRepository.findAllByShortFormAndIsDefiningOntology(shortForm, pageable);
        } else if (oboId != null) {
            terms = ontologyIndividualRepository.findAllByOboIdAndIsDefiningOntology(oboId, pageable);
        } else {
            terms = ontologyIndividualRepository.findAllByIsDefiningOntology(pageable);
        }

        return new ResponseEntity<>(assembler.toResource(terms, individualAssembler), HttpStatus.OK);
    }
    

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }
}